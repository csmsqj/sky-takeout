package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        log.info("微信用户登录，userLoginDTO：{}", userLoginDTO);
        //通过后端的两个数据和前端的code，向前端发送请求，返回OPENID 和 会话密钥
        String s = HttpClientUtil.doGet(WX_LOGIN, Map.of(
                "appid", weChatProperties.getAppid(),
                "secret", weChatProperties.getSecret(),
                "js_code", userLoginDTO.getCode(),
                "grant_type", "authorization_code"
        ));
        String openid = JSON.parseObject(s).getString("openid");

        //判断 OpenID 是否合法，为空失败，抛出业务异常
        if (openid == null || openid.isEmpty()) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);

        }
        //如果是新用户(根据 OpenID 查找用户，最后返回用户)，那么完成注册(通过构造器的方式构造用户信息,冰插入表)，最后返回用户对象。
        User user = userMapper.selectByOpenid(openid);
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        return user;
    }


}
