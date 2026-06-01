package com.abc.service.impl;

import com.abc.constant.MessageConstant;
import com.abc.constant.StatusConstant;
import com.abc.dto.SetmealDTO;
import com.abc.dto.SetmealQueryDTO;
import com.abc.entity.Dish;
import com.abc.entity.Setmeal;
import com.abc.entity.SetmealDish;
import com.abc.exception.DeleteNotAllowedException;
import com.abc.mapper.DishMapper;
import com.abc.mapper.SetmealDishMapper;
import com.abc.mapper.SetmealMapper;
import com.abc.result.PageResult;
import com.abc.service.DishService;
import com.abc.service.SetmealService;
import com.abc.vo.DishVO;
import com.abc.vo.SetmealVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SetmealServiceImpl implements SetmealService {

    private final SetmealMapper setmealMapper;
    private final SetmealDishMapper setmealDishMapper;
    private final DishMapper dishMapper;

    @Override
    public PageResult<SetmealVO> pageQuery(SetmealQueryDTO setmealQueryDTO) {
        PageHelper.startPage(setmealQueryDTO.getPage(),setmealQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Transactional
    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        //新增套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);

        //新增套餐种具体的菜品
        Long setmealId = setmeal.getId();
        List<SetmealDish> dishes = setmealDTO.getSetmealDishes();
        if (dishes != null && !dishes.isEmpty()){
            dishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
        }
        setmealDishMapper.insertBatch(dishes);
    }

    @Override
    public SetmealVO selectById(Long id) {
        return setmealMapper.selectById(id);
    }

    @Transactional
    @Override
    public void updateSetmealInfo(SetmealDTO setmealDTO) {
        //对于基础数据，直接更新
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.updateSetmeal(setmeal);
        //对于菜品数据，先删除全部，再插入新的，以此来实现“更新”
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealDTO.getId());
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    @Override
    public void updateSetmealStatus(Integer status, Long id) {
        SetmealVO setmealVO = setmealMapper.selectById(id);
        List<SetmealDish> setmealDishes = setmealVO.getSetmealDishes();
        for (SetmealDish setmealDish: setmealDishes){
            DishVO dishVO = dishMapper.selectById(setmealDish.getDishId());
            if (dishVO.getStatus() == StatusConstant.DISABLE){
                throw new DeleteNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }

        Setmeal setmeal = Setmeal.builder()
                                 .id(id)
                                 .status(status)
                                 .build();
        setmealMapper.updateSetmeal(setmeal);
    }

    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //起售中的套餐不能删除
        for (Long id: ids){
            SetmealVO setmealVO = setmealMapper.selectById(id);
            if (setmealVO.getStatus() == StatusConstant.ENABLE){
                throw new DeleteNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

    }


}
