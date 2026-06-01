package com.abc.service;

import com.abc.dto.CategoryDTO;
import com.abc.dto.CatergoryPageQueryDTO;
import com.abc.entity.Category;
import com.abc.result.PageResult;

import java.util.List;

public interface CategoryService {
    PageResult<Category> pageQuery(CatergoryPageQueryDTO catergoryPageQueryDTO);

    void startOrStop(Integer status, Long id);

    void save(CategoryDTO categoryDTO);

    void update(CategoryDTO categoryDTO);

    void deleteById(Long id);

    List<Category> selectByType(Integer type);
}
