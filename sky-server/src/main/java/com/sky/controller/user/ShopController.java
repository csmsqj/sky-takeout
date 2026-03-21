package com.sky.controller.user;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
//由于ioc容器中不能有同名的bean，所以通过@RestController重命名
@RestController("UserShopController")
@RequestMapping("/user/shop")
public class ShopController {
@Autowired
private RedisTemplate redisTemplate;
    @GetMapping("/status")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        log.info("获取商户状态，status：{}", status);
        return Result.success(status);
    }

}


