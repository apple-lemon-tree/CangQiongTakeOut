package com.abc.controller.user;


import com.abc.dto.CategoryDTO;
import com.abc.dto.CatergoryPageQueryDTO;
import com.abc.entity.Category;
import com.abc.result.PageResult;
import com.abc.result.Result;
import com.abc.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController("userCategoryController")
@RequestMapping("/user/category")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> selectByTypeId(Integer type){
        log.info("根据类型查询菜品,{}",type);
        List<Category> categoryList = categoryService.selectByType(type);
        return Result.success("查询成功",categoryList);
    }


}
