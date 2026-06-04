package com.abc.service.impl;

import com.abc.constant.StatusConstant;
import com.abc.context.CurrentHolder;
import com.abc.dto.CategoryDTO;
import com.abc.dto.CatergoryPageQueryDTO;
import com.abc.entity.Category;
import com.abc.mapper.CategoryMapper;
import com.abc.mapper.EmployeeMapper;
import com.abc.result.PageResult;
import com.abc.service.CategoryService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryMapper categoryMapper;

    @Override
    public PageResult<Category> pageQuery(CatergoryPageQueryDTO catergoryPageQueryDTO) {
        PageHelper.startPage(catergoryPageQueryDTO.getPage(),catergoryPageQueryDTO.getPageSize());
        Page<Category> page = categoryMapper.list(catergoryPageQueryDTO);
        long total = page.getTotal();
        List<Category> records = page.getResult();
        return new PageResult<>(total,records);
    }

    @Override
    @CacheEvict(cacheNames = "categoryCache",allEntries = true)
    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                .build();
        categoryMapper.update(category);
    }

    @Override
    @CacheEvict(cacheNames = "categoryCache",allEntries = true)
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        category.setStatus(StatusConstant.ENABLE);
        categoryMapper.add(category);
    }

    @Override
    @CacheEvict(cacheNames = "categoryCache",allEntries = true)
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        categoryMapper.update(category);
    }

    @Override
    @CacheEvict(cacheNames = "categoryCache",allEntries = true)
    public void deleteById(Long id) {
        categoryMapper.delete(id);
    }

    @Override
    public List<Category> selectByType(Integer type) {
        List<Category> categoryList = categoryMapper.selectByType(type);
        return categoryList;
    }
}
