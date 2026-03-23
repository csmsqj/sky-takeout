package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/shoppingCart")
public class ShoppingCartController {
@Autowired
private ShoppingCartService shoppingCartService;

    // 添加购物车
    @PostMapping("/add")
    public Result<Void> add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车");
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
    }


    // 查看购物车。
    @GetMapping("/list")
    public Result<List<ShoppingCart>> selectShoppingCart() {
        log.info("查看购物车");
        List<ShoppingCart> list=shoppingCartService.selectShoppingCart();
        return Result.success(list);
    }

     // 清空购物车
@DeleteMapping("/clean")
    public Result<Void> clean() {
        log.info("清空购物车");
        shoppingCartService.clean();
        return Result.success();
    }

}
