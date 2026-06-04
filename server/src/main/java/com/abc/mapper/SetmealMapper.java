package com.abc.mapper;

import com.abc.annotation.AutoFill;
import com.abc.dto.SetmealQueryDTO;
import com.abc.entity.Setmeal;
import com.abc.enumeration.OperationType;
import com.abc.vo.DishItemVO;
import com.abc.vo.SetmealVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {
    @AutoFill(value = OperationType.INSERT)
    void insert(Setmeal setmeal);

    Page<SetmealVO> pageQuery(SetmealQueryDTO setmealQueryDTO);

    SetmealVO selectById(Long id);

    @AutoFill(value = OperationType.UPDATE)
    void updateSetmeal(Setmeal setmeal);

    @Select("select * from setmeal where category_id=#{categoryId} and status=1")
    List<Setmeal> list(Long categoryId);

    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemById(Long setmealId);
}
