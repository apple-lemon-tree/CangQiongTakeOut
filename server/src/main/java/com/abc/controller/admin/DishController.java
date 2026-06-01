package com.abc.controller.admin;

import com.abc.dto.DishDTO;
import com.abc.dto.DishQueryDTO;
import com.abc.entity.Dish;
import com.abc.result.PageResult;
import com.abc.result.Result;
import com.abc.service.DishService;
import com.abc.vo.DishVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    /**
     * 新增菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品：{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success("新增菜品成功");
    }

    /**
     * 菜品分页查询
     * @param dishQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(DishQueryDTO dishQueryDTO){
        log.info("分页查询菜品：{}",dishQueryDTO);
        PageResult<DishVO> pageResult = dishService.pageQuery(dishQueryDTO);
        return Result.success("分页查询成功",pageResult);
    }

    /**
     * 菜品批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品批量删除:{}",ids);
        dishService.deleteBatch(ids);
        return Result.success("删除成功");
    }

    /**
     * 根据菜品Id查询菜品相关信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishVO> findById(@PathVariable Long id){
        log.info("根据菜品id查询菜品信息:{}",id);
        DishVO dishVO = dishService.seleteById(id);
        return Result.success("根据id查询菜品",dishVO);
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     * @return
     */
    @PutMapping
    public Result updateDishWithFlavor(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success("修改菜品信息成功");
    }

    /**
     * 停售/起售 菜品
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status,Long id){
        log.info("停售/启售 菜品：{}",id);
        dishService.startOrStop(status,id);
        return Result.success("操作成功");
    }

    /**
     * 根据分类的id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result list(Long categoryId){
        log.info("根据分类Id查询菜品:{}",categoryId);
        List<Dish> dishes = dishService.list(categoryId);
        return Result.success("查询成功",dishes);
    }
}
