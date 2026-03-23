package com.sky.service;


import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {
        /**
        * 微信用户登录
        *
        * @param code 微信登录时获取的code
        * @return 登录结果
        */
        User login(UserLoginDTO userLoginDTO);
}
