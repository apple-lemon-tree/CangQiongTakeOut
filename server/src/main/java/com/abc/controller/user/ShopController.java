package com.abc.controller.user;

import com.abc.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("userShopController")
@RequiredArgsConstructor
@RequestMapping("/user/shop")
public class ShopController {

    private final StringRedisTemplate stringRedisTemplate;

    @GetMapping("/status")
    public Result getStatus(){
        Integer status = Integer.valueOf(stringRedisTemplate.opsForValue().get("SHOP_STATUS"));
        log.info("获取到的店铺的营业状态为：{}",status==1 ? "营业中":"打烊中");
        return Result.success("",status);

    }
}