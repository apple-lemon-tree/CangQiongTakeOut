package com.abc.controller.user;


import com.abc.dto.UserLoginDTO;
import com.abc.entity.User;
import com.abc.result.Result;
import com.abc.service.UserService;
import com.abc.utils.JwtUtil;
import com.abc.vo.UserLoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("微信用户登录：{}",userLoginDTO.getCode());
        User user = userService.wxLogin(userLoginDTO);

        //为微信用户生成jwt令牌
        Map<String,Object> claims = new HashMap<>();
        claims.put("userId",user.getId());
        String token = jwtUtil.generateToken("",claims);
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        return Result.success("",userLoginVO);

    }
}
