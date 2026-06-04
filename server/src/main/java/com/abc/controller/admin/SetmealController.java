package com.abc.controller.admin;

import com.abc.dto.SetmealDTO;
import com.abc.dto.SetmealQueryDTO;
import com.abc.result.PageResult;
import com.abc.result.Result;
import com.abc.service.SetmealService;
import com.abc.vo.SetmealVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController("adminSetmealController")
@RequestMapping("/admin/setmeal")
@RequiredArgsConstructor
public class SetmealController {
    private final SetmealService setmealService;

    /**
     * 分页查询套餐
     * @param setmealQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(SetmealQueryDTO setmealQueryDTO){
        log.info("分页查询套餐：{}",setmealQueryDTO);
        PageResult<SetmealVO> pageResult = setmealService.pageQuery(setmealQueryDTO);
        return Result.success("分页查询成功",pageResult);
    }

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐：{}",setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success("新增菜品成功");
    }

    /**
     * 根据套餐id查询套餐信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SetmealVO> selectById(@PathVariable Long id){
        log.info("根据id查询套餐：{}",id);
        SetmealVO setmealVO = setmealService.selectById(id);
        return Result.success("查询成功",setmealVO);
    }

    /**
     * 修改套餐信息
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result updateSetmeal(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐信息：{}",setmealDTO);
        setmealService.updateSetmealInfo(setmealDTO);
        return Result.success("修改成功");
    }

    /**
     * 启售/停售 套餐
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("套餐起售/停售");
        setmealService.updateSetmealStatus(status,id);
        return Result.success("起售/停售 修改成功");
    }

    /**
     * 套餐批量删除
     * @return
     */
    @DeleteMapping
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result deleteByIds(@RequestParam List<Long> ids){
        log.info("套餐批量删除：{}",ids);
        setmealService.deleteBatch(ids);
        return Result.success("删除成功");
    }

}
