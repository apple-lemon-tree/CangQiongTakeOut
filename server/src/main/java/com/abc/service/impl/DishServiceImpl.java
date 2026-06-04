package com.abc.service.impl;

import com.abc.constant.MessageConstant;
import com.abc.constant.StatusConstant;
import com.abc.context.CurrentHolder;
import com.abc.dto.DishDTO;
import com.abc.dto.DishQueryDTO;
import com.abc.entity.Dish;
import com.abc.entity.DishFlavor;
import com.abc.exception.DeleteNotAllowedException;
import com.abc.mapper.DishFlavorMapper;
import com.abc.mapper.DishMapper;
import com.abc.mapper.SetmealDishMapper;
import com.abc.result.PageResult;
import com.abc.service.DishService;
import com.abc.vo.DishVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final DishFlavorMapper dishFlavorMapper;
    private final SetmealDishMapper setmealDishMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        //这是一个菜
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);

        //这是这个菜对应的口味
        Long  dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
        }
        dishFlavorMapper.insertBatch(flavors);
    }

    @Override
    public PageResult<DishVO> pageQuery(DishQueryDTO dishQueryDTO) {
        PageHelper.startPage(dishQueryDTO.getPage(),dishQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Transactional
    @Override
    @CacheEvict(cacheNames = "dishCache",allEntries = true)
    public void deleteBatch(List<Long> ids) {
        //起售中的菜品不能删除
        for (Long id:ids){
            DishVO dishVO = dishMapper.selectById(id);
            if (dishVO.getStatus() == StatusConstant.ENABLE){
                throw new DeleteNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //被套餐关联的菜品不能删除
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && !setmealIds.isEmpty()){
            throw new DeleteNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品关联的口味
        dishFlavorMapper.deleteByDishIds(ids);
        //删除菜品表中的菜品
        dishMapper.deleteByIds(ids);
    }

    @Override
    public DishVO seleteById(Long id) {
        return dishMapper.selectById(id);
    }

    @Override
    @CacheEvict(cacheNames = "dishCache",allEntries = true)
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish  = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //修改菜品基本信息
        dishMapper.update(dish);
        //删除原有口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //重新插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
    }

    @Override
    public List<Dish> list(Long id) {
        List<Dish> dishes = dishMapper.list(id);
        return dishes;
    }

    @Cacheable(cacheNames = "dishCache", key = "#categoryId")
    @Override
    public List<DishVO> listWithFlavor(Long categoryId) {
        List<Dish> dishList = dishMapper.list(categoryId);
        List<DishVO> dishVOList = new ArrayList<>();
        for (Dish d: dishList){
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            List<DishFlavor> flavors = dishFlavorMapper.getFlavorByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }
}
