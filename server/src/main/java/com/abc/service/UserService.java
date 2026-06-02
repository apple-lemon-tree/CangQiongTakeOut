package com.abc.service;

import com.abc.dto.UserLoginDTO;
import com.abc.entity.User;

public interface UserService {
    User wxLogin(UserLoginDTO userLoginDTO);
}
