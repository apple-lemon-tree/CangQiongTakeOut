package com.abc.mapper;

import com.abc.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("select * from sky_take_out.user where id=#{openid}")
    User getByOpenid(String openid);

    void insert(User user);
}
