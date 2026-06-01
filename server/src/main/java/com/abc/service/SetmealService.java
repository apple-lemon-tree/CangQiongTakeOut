package com.abc.service;

import com.abc.dto.SetmealDTO;
import com.abc.dto.SetmealQueryDTO;
import com.abc.result.PageResult;
import com.abc.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    PageResult<SetmealVO> pageQuery(SetmealQueryDTO setmealQueryDTO);

    void saveWithDish(SetmealDTO setmealDTO);

    SetmealVO selectById(Long id);

    void updateSetmealInfo(SetmealDTO setmealDTO);

    void updateSetmealStatus(Integer status, Long id);

    void deleteBatch(List<Long> ids);
}
