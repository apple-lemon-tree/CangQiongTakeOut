package com.abc.mapper;

import com.abc.annotation.AutoFill;
import com.abc.dto.CatergoryPageQueryDTO;
import com.abc.entity.Category;
import com.abc.enumeration.OperationType;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    Page<Category> list(CatergoryPageQueryDTO catergoryPageQueryDTO);

    @AutoFill(value = OperationType.UPDATE)
    void update(Category category);

    @Insert("insert into category " +
            "values(default,#{type},#{name},#{sort},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    @AutoFill(value = OperationType.INSERT)
    void add(Category category);

    @Delete("delete from category where id=#{id}")
    void delete(Long id);


    List<Category> selectByType(Integer type);
}
