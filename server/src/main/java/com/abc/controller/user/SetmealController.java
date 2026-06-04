package com.abc.controller.user;

import com.abc.constant.StatusConstant;
import com.abc.dto.SetmealDTO;
import com.abc.dto.SetmealQueryDTO;
import com.abc.entity.Setmeal;
import com.abc.result.PageResult;
import com.abc.result.Result;
import com.abc.service.SetmealService;
import com.abc.vo.DishItemVO;
import com.abc.vo.SetmealVO;
import com.sun.xml.bind.annotation.XmlIsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@RequiredArgsConstructor
public class SetmealController {
    private final SetmealService setmealService;

    /**
     * 根据分类id查询套餐信息
     * @param categoryId
     * @return
     */

    @GetMapping("/list")
    public Result<List<Setmeal>> list(Long categoryId){
        List<Setmeal> setmealList = setmealService.list(categoryId);
        return Result.success("查询成功",setmealList);
    }

    /**
     * 根据套餐id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public Result<List<DishItemVO>> dishList(@PathVariable("id") Long id){
        List<DishItemVO> list = setmealService.getDishItemById(id);
        return Result.success("查询成功",list);

    }

}
