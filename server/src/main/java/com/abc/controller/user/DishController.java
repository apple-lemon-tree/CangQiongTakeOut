package com.abc.controller.user;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.abc.constant.RedisConstants;
import com.abc.constant.StatusConstant;
import com.abc.dto.DishDTO;
import com.abc.dto.DishQueryDTO;
import com.abc.entity.Dish;
import com.abc.result.PageResult;
import com.abc.result.Result;
import com.abc.service.DishService;
import com.abc.vo.DishVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController("userDishController")
@RequestMapping("/user/dish")
@RequiredArgsConstructor
@Validated //参数校验  处理缓存穿透问题
public class DishController {

    private final DishService dishService;
    private final StringRedisTemplate stringRedisTemplate;
    /**
     * 根据分类的id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public Result<List<DishVO>> listWithFlavor(Long categoryId){
        log.info("根据分类Id查询菜品:{}",categoryId);
        List<DishVO> list = dishService.listWithFlavor(categoryId).orElse(null);
        return Result.success("查询成功",list);
    }

}
