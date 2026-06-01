package com.abc.service;

import com.abc.dto.DishDTO;
import com.abc.dto.DishQueryDTO;
import com.abc.entity.Dish;
import com.abc.result.PageResult;
import com.abc.vo.DishVO;

import java.util.List;

public interface DishService {
    void saveWithFlavor(DishDTO dishDTO);

    PageResult<DishVO> pageQuery(DishQueryDTO dishQueryDTO);

    void deleteBatch(List<Long> ids);

    DishVO seleteById(Long id);

    void updateWithFlavor(DishDTO dishDTO);

    void startOrStop(Integer status, Long id);

    List<Dish> list(Long id);
}
