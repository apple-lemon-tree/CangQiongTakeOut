package com.abc.mapper;

import com.abc.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    void insertBatch(List<DishFlavor> flavors);

    void deleteByDishIds(List<Long> ids);

    @Delete("delete from dish_flavor where dish_id = id;")
    void deleteByDishId(Long id);
}
