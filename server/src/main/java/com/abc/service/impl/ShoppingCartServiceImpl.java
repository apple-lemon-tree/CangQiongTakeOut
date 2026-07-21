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
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

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
     * 获取 菜品/套餐 的id 和 口味，用于后期组建redis中的Key
     * @param dto
     * @return
     */
    private String getItemKey(ShoppingCartDTO dto){
        if (dto.getDishId() != null){

            return "dish_" + dto.getDishId() + "_" + dto.getDishFlavor();
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
        String cartKey = getCartKey(userId);
        String itemKey = getItemKey(shoppingCartDTO);

        //检查商品是否存在
        Boolean exists = stringRedisTemplate.opsForHash().hasKey(cartKey,itemKey);

        if (exists != null && exists){
            //如果商品在购物车中存在，就+1
            String countStr = stringRedisTemplate.opsForHash().get(cartKey,itemKey).toString();
            int newCount = Integer.parseInt(countStr) + 1;
        /*
        Redis
           shopping:cart:user:1
                dish_14 : 2    dish_菜品id : 点单的数量
                setmeal_4 : 1  setmeal_套餐id : 点单的数量
                ...
         */
            stringRedisTemplate.opsForHash().put(cartKey, itemKey, String.valueOf(newCount));
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
                stringRedisTemplate.opsForHash().put(cartKey, itemKey, "1");
            }else {
                // 新商品是个 《套餐》
                SetmealVO setmealVO = setmealMapper.selectById(shoppingCartDTO.getSetmealId());
                if (setmealVO == null){
                    log.error("用户{}添加购物车失败，菜品不存在，dishId:{}",userId,shoppingCartDTO.getDishId());
                    throw new BusinessException(ITEM_NOT_FOUND_MSG);
                }
                // 商品也存在，放入redis，设置value为1  （1份）
                stringRedisTemplate.opsForHash().put(cartKey, "setmeal_" + shoppingCartDTO.getSetmealId(), "1");
            }
        }
        // 当前用户的购物车在Redis中存放的时间刷新一下
        stringRedisTemplate.expire(cartKey, CART_TTL, TimeUnit.MINUTES);
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
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(cartKey);
        // 如果查询出来是空，那就返回一个空列表
        if (entries == null || entries.isEmpty()){
            return new ArrayList<>();
        }
        // 查出来购物车里有东西
        /*
                Redis
                   shopping:cart:user:1
                        dish_14_不辣 : 2    dish_菜品id : 点单的数量
                        setmeal_4_不要葱,重辣 : 1  setmeal_套餐id : 点单的数量
                        ...
        */
        List<ShoppingCartItem> itemList = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()){
            String itemKey = (String) entry.getKey(); // dish_14_不辣  setmeal_10_不要葱,重辣 ...
            String[] arr = itemKey.split("_",3);
            itemKey = arr[0];

            Integer count = Integer.parseInt((String) entry.getValue());// 对应菜品/套餐的数量

            ShoppingCartItem item = new ShoppingCartItem();
            item.setNumber(count);
            item.setUserId(userId);

            //分析循环查询到的商品是《菜品》还是《套餐》

            if (itemKey.equals("dish")){
                Long dishId = Long.valueOf(arr[1]);//获取菜品id
                String flavor = arr.length==3?arr[2]:"";//如果是菜品，那还有额外的一项口味数据
                DishVO dishVO = dishMapper.selectById(dishId);
                if (dishVO != null){
                    item.setName(dishVO.getName());
                    item.setDishId(dishId);
                    item.setAmount(dishVO.getPrice().multiply(BigDecimal.valueOf(count)));
                    item.setImage(dishVO.getImage());
                    item.setDishFlavor(flavor);
                }
            } else if (itemKey.equals("setmeal")) {
                Long setmealId = Long.valueOf(arr[1]);
                SetmealVO setmealVO = setmealMapper.selectById(setmealId);
                if (setmealVO != null){
                    item.setSetmealId(setmealId);
                    item.setName(setmealVO.getName());
                    item.setImage(setmealVO.getImage());
                    item.setAmount(setmealVO.getPrice().multiply(BigDecimal.valueOf(count)));
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
        String cartKey = getCartKey(userId);
        stringRedisTemplate.delete(cartKey);
    }

    /**
     * 购物车商品-1 / 删除实现
     * @param shoppingCartDTO
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
       Long userId = CurrentHolder.getCurrentId();
       String cartKey = getCartKey(userId);
       String itemKey = getItemKey(shoppingCartDTO);
       Boolean exists = stringRedisTemplate.opsForHash().hasKey(cartKey, itemKey);
       if (exists != null && exists) {
           String countStr = (String) stringRedisTemplate.opsForHash().get(cartKey, itemKey);
           int newCount = Integer.parseInt(countStr) - 1;
           //如果这个商品就下单了一份，那么就直接删除
           if (newCount <= 0) {
               stringRedisTemplate.opsForHash().delete(cartKey, itemKey);
           } else {
               stringRedisTemplate.opsForHash().put(cartKey, itemKey, String.valueOf(newCount));
           }
           //刷新一下用户购物车在Redis中的剩余时间
           stringRedisTemplate.expire(cartKey, CART_TTL, TimeUnit.MINUTES);
       }
    }
}
