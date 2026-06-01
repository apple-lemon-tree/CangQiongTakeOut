package com.abc.controller.admin;


import com.abc.dto.CategoryDTO;
import com.abc.dto.CatergoryPageQueryDTO;
import com.abc.entity.Category;
import com.abc.entity.Dish;
import com.abc.result.PageResult;
import com.abc.result.Result;
import com.abc.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    /**
     * 分页查询 菜品/套餐 表
     * @param catergoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult<Category>> page(CatergoryPageQueryDTO catergoryPageQueryDTO){
        log.info("分类相关查询，参数为：{}",catergoryPageQueryDTO);
        PageResult<Category> pageResult = categoryService.pageQuery(catergoryPageQueryDTO);
        return Result.success("分页查询成功",pageResult);
    }

    /**
     * 设置 菜品/套餐 禁用/启用
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    public Result startorStop(@PathVariable Integer status, Long id){
        log.info("修改{} 菜品/套餐 的状态为 启用/禁用",id);
        categoryService.startOrStop(status,id);
        return Result.success("修改权限成功");
    }

    /**
     * 新增 菜品/套餐
     * @param categoryDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody CategoryDTO categoryDTO){
        log.info("新增菜品/套餐:{}",categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success("添加新 菜/套餐 成功");
    }

    /**
     * 修改 菜品/套餐 内容
     * @param categoryDTO
     * @return
     */
    @PutMapping
    public Result update(@RequestBody CategoryDTO categoryDTO){
        log.info("修改菜品/套餐：{}",categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success("修改 菜品/套餐 成功");

    }

    /**
     * 删除菜品分类
     * @param id
     * @return
     */
    @DeleteMapping
    public Result delete(Long id){
        log.info("删除 菜品/套餐 {}",id);
        categoryService.deleteById(id);
        return Result.success("根据id删除 菜品/套餐", id);
    }

    @GetMapping("/list")
    public Result<List<Category>> selectByType(Integer type){
        log.info("根据类型查询菜品,{}",type);
        List<Category> categoryList = categoryService.selectByType(type);
        return Result.success("查询成功",categoryList);
    }
}
