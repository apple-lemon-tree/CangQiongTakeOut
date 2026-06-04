package com.abc.service.impl;

import com.abc.context.CurrentHolder;
import com.abc.dto.ShoppingCartDTO;
import com.abc.entity.Dish;
import com.abc.entity.ShoppingCartItem;
import com.abc.mapper.DishMapper;
import com.abc.mapper.SetmealMapper;
import com.abc.mapper.ShoppingCartMapper;
import com.abc.mapper.UserMapper;
import com.abc.service.ShoppingCartService;
import com.abc.vo.DishVO;
import com.abc.vo.SetmealVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartMapper shoppingCartMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;


    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断当前要添加的产品是否已经存在了
        ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCartItem);
        Long userId = CurrentHolder.getCurrentId();
        shoppingCartItem.setUserId(userId);
        List<ShoppingCartItem> list = shoppingCartMapper.list(shoppingCartItem);
        //如果已经存在了
        if (list != null && !list.isEmpty()){
            //其实上面的查询要么返回为空，要么只返回一条数据
            ShoppingCartItem item = list.get(0);
            item.setNumber(item.getNumber() + 1);
            shoppingCartMapper.updateNumberById(item);
        }else {
            //如果不存在，需要插入一条购物车数据
            //判断本次添加到购物车的是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null){
                //说明本次添加到购物车中的是菜品
                DishVO dishvo = dishMapper.selectById(dishId);
                shoppingCartItem.setName(dishvo.getName());
                shoppingCartItem.setImage(dishvo.getImage());
                shoppingCartItem.setAmount(dishvo.getPrice());
            }else{
                //本次添加到购物车中的是套餐
                Long setmealId = shoppingCartDTO.getSetmealId();
                SetmealVO setmealVO = setmealMapper.selectById(setmealId);
                shoppingCartItem.setName(setmealVO.getName());
                shoppingCartItem.setImage(setmealVO.getImage());
                shoppingCartItem.setAmount(setmealVO.getPrice());

            }
            shoppingCartItem.setNumber(1);
            shoppingCartItem.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCartItem);
        }

    }

    @Override
    public List<ShoppingCartItem> showShoppingCart() {
        Long userId = CurrentHolder.getCurrentId();
        ShoppingCartItem shoppingCartItem = ShoppingCartItem.builder()
                .userId(userId)
                .build();
        return shoppingCartMapper.list(shoppingCartItem);
    }

    @Override
    public void cleanShoppingCart() {
        Long userId = CurrentHolder.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }

    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCartItem item = new ShoppingCartItem();
        BeanUtils.copyProperties(shoppingCartDTO,item);
        List<ShoppingCartItem> list = shoppingCartMapper.list(item);
        if (list != null && !list.isEmpty()){
            item = list.get(0);

            Integer number = item.getNumber();
            if (number == 1){
                shoppingCartMapper.deleteByItemId(item.getId());
            }else {
                item.setNumber(item.getNumber() - 1);
                shoppingCartMapper.updateNumberById(item);
            }
        }
    }
}
