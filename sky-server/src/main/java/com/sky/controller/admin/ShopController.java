package com.sky.controller.admin;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("AdminShopController")
@RequestMapping("/admin/shop")
public class ShopController {
@Autowired
private RedisTemplate<String, Object> redisTemplate;
    @PutMapping("/{status}")
    public Result<Void> startOrStop(@PathVariable Integer status) {
        log.info("修改商户状态，status：{}", status);
        //注入到redis当中,status不加引号，但是value是加引号的
        redisTemplate.opsForValue().set("SHOP_STATUS", status);


        return Result.success();
    }

    @GetMapping("/status")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get("SHOP_STATUS");
        log.info("获取商户状态，status：{}", status);
        return Result.success(status);
    }

}
