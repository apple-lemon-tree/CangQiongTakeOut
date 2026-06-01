package com.abc.controller.admin;

import com.abc.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminShopController")
@RequiredArgsConstructor
@RequestMapping("/admin/shop")
public class ShopController {

    private final StringRedisTemplate stringRedisTemplate;

    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺的营业状态：{}",status == 1 ? "营业中":"打烊中");
        stringRedisTemplate.opsForValue().set("SHOP_STATUS",status.toString());
        return Result.success("设置成功");
    }

    @GetMapping("/status")
    public Result<Integer> getStatus(){
        Integer status = Integer.valueOf(stringRedisTemplate.opsForValue().get("SHOP_STATUS"));
        log.info("获取到的店铺的营业状态为：{}",status==1 ? "营业中":"打烊中");
        return Result.success("",status);

    }
}
