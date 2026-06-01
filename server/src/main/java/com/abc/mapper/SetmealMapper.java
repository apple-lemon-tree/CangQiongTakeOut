package com.abc.mapper;

import com.abc.annotation.AutoFill;
import com.abc.dto.SetmealQueryDTO;
import com.abc.entity.Setmeal;
import com.abc.enumeration.OperationType;
import com.abc.vo.SetmealVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SetmealMapper {
    @AutoFill(value = OperationType.INSERT)
    void insert(Setmeal setmeal);

    Page<SetmealVO> pageQuery(SetmealQueryDTO setmealQueryDTO);

    SetmealVO selectById(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void updateSetmeal(Setmeal setmeal);
}
