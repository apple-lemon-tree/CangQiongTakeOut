package com.abc.service.impl;

import com.abc.context.CurrentHolder;
import com.abc.dto.ShoppingCartDTO;
import com.abc.entity.ShoppingCartItem;
import com.abc.exception.BusinessException;
import com.abc.mapper.DishMapper;
import com.abc.mapper.SetmealMapper;
import com.abc.mapper.ShoppingCartMapper;
import com.abc.service.ShoppingCartService;
import com.abc.vo.DishVO;
import com.abc.vo.SetmealVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.abc.constant.MessageConstant.*;
import static com.abc.constant.RedisConstants.CART_PREFIX;
import static com.abc.constant.RedisConstants.CART_TTL;
/*
1、为什么需要分布式锁
单机应用，多线程竞争共享资源，可以通过synchrnized、ReentrantLock解决
分布式应用，多JVM进程竞争共享资源，就要通过分布式锁来解决
2、分布式锁应具备的特性
✅ 互斥性：同一时刻只能有一个客户端持有锁
✅ 可重入性：同一线程可以重复获取锁
✅ 防死锁：持有锁的客户端宕机，锁能自动释放
✅ 容错性：锁服务本身高可用
✅ 高性能：加锁解锁耗时短
✅ 公平性（可选）：按请求顺序获取锁







 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartMapper shoppingCartMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;
    private final RedissonClient redissonClient;
    private final RedisTemplate<Object, Object> redisTemplate;

    /**
     * 拿到当前用户在缓存中的前缀
     * @param userId
     * @return
     */
    private String getCartKey(Long userId){
        // shopping:cart:user:1
        return CART_PREFIX + userId;
    }

    /**
     * 拿到分布式锁
     * @param userId
     * @return
     */
    private RLock getCartLock(Long userId){
        return redissonClient.getLock(CART_PREFIX+userId);
    }

    /**
     * 获取 菜品/套餐 的id，用于后期组建redis中的Key
     * @param dto
     * @return
     */
    private String getItemKey(ShoppingCartDTO dto){
        if (dto.getDishId() != null){
            return "dish_" + dto.getDishId();
        } else {
            return "setmeal_" + dto.getSetmealId();
        }
    }

    /**
     * 往当前用户的购物车里添加商品
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long userId = CurrentHolder.getCurrentId();
        RLock lock = getCartLock(userId);
        try{
            //尝试加锁，最多等待3秒，上锁后30秒自动解锁
            if(!lock.tryLock(3,30,TimeUnit.SECONDS)){
                log.warn("用户 {} 获取购物车锁超时",userId);
                throw new BusinessException(OPERATION_FREQUENT_MSG);
            }
            String cartKey = getCartKey(userId);
            String itemKey = getItemKey(shoppingCartDTO);

            //检查商品是否存在
            Boolean exists = redisTemplate.opsForHash().hasKey(cartKey,itemKey);

            if (exists != null && exists){
                //如果商品在购物车中存在，就+1
                String countStr = (String) redisTemplate.opsForHash().get(cartKey,itemKey);
                int newCount = Integer.parseInt(countStr) + 1;
            /*
            Redis
               shopping:cart:user:1
                    dish_14 : 2    dish_菜品id : 点单的数量
                    setmeal_4 : 1  setmeal_套餐id : 点单的数量
                    ...
             */
                redisTemplate.opsForHash().put(cartKey, itemKey, String.valueOf(newCount));
            }else{
                //如果不存在，就把新商品添加到购物车
                if (shoppingCartDTO.getDishId() != null){
                    // 新商品是个《菜品》
                    DishVO dishVO = dishMapper.selectById(shoppingCartDTO.getDishId());
                    if (dishVO == null){
                        log.error("用户{}添加购物车失败，菜品不存在，dishId:{}",userId,shoppingCartDTO.getDishId());
                        throw new BusinessException(ITEM_NOT_FOUND_MSG);
                    }
                    // 菜品也存在，放入redis，设置value为1  （1份）
                    redisTemplate.opsForHash().put(cartKey, "dish_" + shoppingCartDTO.getDishId(), "1");
                }else {
                    // 新商品是个 《套餐》
                    SetmealVO setmealVO = setmealMapper.selectById(shoppingCartDTO.getSetmealId());
                    if (setmealVO == null){
                        log.error("用户{}添加购物车失败，菜品不存在，dishId:{}",userId,shoppingCartDTO.getDishId());
                        throw new BusinessException(ITEM_NOT_FOUND_MSG);
                    }
                    // 商品也存在，放入redis，设置value为1  （1份）
                    redisTemplate.opsForHash().put(cartKey, "setmeal_" + shoppingCartDTO.getSetmealId(), "1");
                }
            }
            // 当前用户的购物车在Redis中存放的时间刷新一下
            redisTemplate.expire(cartKey, CART_TTL, TimeUnit.HOURS);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new BusinessException(SYSTEM_BUSY_MSG);
        } finally {
            //释放锁
            if (lock.isLocked() && lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    /**
     * 从Redis中查询某位用户购物车里的内容
     * @return
     */
    @Override
    public List<ShoppingCartItem> showShoppingCart() {
        Long userId = CurrentHolder.getCurrentId();
        String cartKey = getCartKey(userId); // shopping:cart:user:1
        // 读操作，读取redis中对应的信购物车信息
        //<商品/菜品 名称, 数量>
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(cartKey);
        // 如果查询出来是空，那就返回一个空列表
        if (entries == null || entries.isEmpty()){
            return new ArrayList<>();
        }
        // 查出来购物车里有东西
        /*
                Redis
                   shopping:cart:user:1
                        dish_14 : 2    dish_菜品id : 点单的数量
                        setmeal_4 : 1  setmeal_套餐id : 点单的数量
                        ...
        */
        List<ShoppingCartItem> itemList = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()){
            String itemKey = (String) entry.getKey(); // dish_14  setmeal_10 ...
            Integer count = Integer.parseInt((String) entry.getValue());// 对应菜品/套餐的数量

            ShoppingCartItem item = new ShoppingCartItem();
            item.setNumber(count);
            item.setUserId(userId);

            //分析循环查询到的商品是《菜品》还是《套餐》

            if (itemKey.startsWith("dish_")){
                Long dishId = Long.parseLong(itemKey.substring(5));//截取菜品id
                DishVO dishVO = dishMapper.selectById(dishId);
                if (dishVO != null){
                    item.setName(dishVO.getName());
                    item.setDishId(dishId);
                    item.setAmount(dishVO.getPrice().multiply(BigDecimal.valueOf(count)));
                    item.setImage(dishVO.getImage());
                    item.setDishFlavor(dishVO.getFlavors().toString());
                }
            }
            itemList.add(item);
        }
        return itemList;
    }

    /**
     * 清空当前用户的购物车
     */
    @Override
    public void cleanShoppingCart() {
        Long userId = CurrentHolder.getCurrentId();
        RLock lock = getCartLock(userId);
        try{
            if (!lock.tryLock(3,30,TimeUnit.SECONDS)){
                throw new BusinessException(OPERATION_FREQUENT_MSG);
            }
            String cartKey = getCartKey(userId);
            redisTemplate.delete(cartKey);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new BusinessException(SYSTEM_BUSY_MSG);
        } finally{
            if (lock.isLocked() && lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    /**
     * 购物车商品-1 / 删除实现
     * @param shoppingCartDTO
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
       Long userId = CurrentHolder.getCurrentId();
       RLock lock = getCartLock(userId);
       try {
           if (!lock.tryLock(3, 30, TimeUnit.SECONDS)) {
               throw new BusinessException(OPERATION_FREQUENT_MSG);
           }
           String cartKey = getCartKey(userId);
           String itemKey = getItemKey(shoppingCartDTO);
           Boolean exists = redisTemplate.opsForHash().hasKey(cartKey, itemKey);
           if (exists != null && exists) {
               String countStr = (String) redisTemplate.opsForHash().get(cartKey, itemKey);
               int newCount = Integer.parseInt(countStr) - 1;
               //如果这个商品就下单了一份，那么就直接删除
               if (newCount <= 0) {
                   redisTemplate.opsForHash().delete(cartKey, itemKey);
               } else {
                   redisTemplate.opsForHash().put(cartKey, itemKey, String.valueOf(newCount));
               }
               //刷新一下用户购物车在Redis中的剩余时间
               redisTemplate.expire(cartKey, CART_TTL, TimeUnit.HOURS);
           }
       }catch (InterruptedException e){
           Thread.currentThread().interrupt();
           throw new BusinessException(SYSTEM_BUSY_MSG);
       } finally {
           if (lock.isLocked() && lock.isHeldByCurrentThread()){
               lock.unlock();
           }
       }
    }
}
