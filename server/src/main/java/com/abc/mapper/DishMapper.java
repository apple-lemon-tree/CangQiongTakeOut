package com.abc.mapper;

import com.abc.annotation.AutoFill;
import com.abc.dto.DishQueryDTO;
import com.abc.entity.Dish;
import com.abc.enumeration.OperationType;
import com.abc.vo.DishVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {
    
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> pageQuery(DishQueryDTO dishQueryDTO);

    void deleteByIds(List<Long> ids);

    DishVO selectById(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    List<Dish> list(Long id);
}
