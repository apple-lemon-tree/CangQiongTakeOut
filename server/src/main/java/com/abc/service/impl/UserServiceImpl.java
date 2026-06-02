package com.abc.service.impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.abc.constant.MessageConstant;
import com.abc.dto.UserLoginDTO;
import com.abc.entity.User;
import com.abc.exception.LoginFailedException;
import com.abc.mapper.UserMapper;
import com.abc.properties.WeChatProperties;
import com.abc.service.UserService;
import com.abc.utils.HttpClientUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final WeChatProperties weChatProperties;
    private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        //调用微信接口服务，获得当前微信用户的openid
        Map<String,String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type","authorization_code");
        //获取返回的json信息,形如{
        //    "session_key": "6R9FKmjYi4WmPTCWJOiHHw==",
        //    "openid": "oWRg63ZAKIs3ptddi2nvj0fDkMls"
        //}
        String json = HttpClientUtil.doGet(WX_LOGIN,map);
        JSONObject jsonObject = JSONUtil.parseObj(json);
        String openid = jsonObject.getStr("openid");
        //判断openid是否为空，如果为空则标识登陆失败，抛出业务异常
        if (openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断当前用户是否为新用户
        User user = userMapper.getByOpenid(openid);
        //是新用户的话，自动完成注册
        if (user == null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        //返回这个用户对象
        return user;
    }
}
