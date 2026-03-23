package com.sky.controller.user;

import com.alibaba.fastjson.support.hsf.HSFJSONUtils;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController("userController")
@RequestMapping("/user/user")
public class UserController {
@Autowired
private UserService userService;
@Autowired
private JwtProperties jwtProperties;
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录");
        User user = userService.login(userLoginDTO);
        //生成 JWT 令牌。通过 JWTproperties配置属性生成，最重要的是通过微信用户的 ID 生成。第三个算法claims是键值对
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String jwt = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims
        );
        UserLoginVO userLoginVO=UserLoginVO
                .builder()
                .id(user.getId())
                .token(jwt)
                .openid(user.getOpenid())
                .build();

        return Result.success(userLoginVO);
    }

}
