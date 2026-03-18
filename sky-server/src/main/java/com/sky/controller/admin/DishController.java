package com.sky.controller.admin;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.impl.DishServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/dish")
public class DishController {
    @Autowired
    private DishServiceImpl dishService;
    /*新增菜品*/
    @PostMapping
    public Result<Void> saveDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品");

dishService.savaDish(dishDTO);
        return Result.success();
    }


    //分页查询菜品
    @GetMapping("/page")
public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
log.info("分页查询");
PageResult pageResult = dishService.page(dishPageQueryDTO);
return Result.success(pageResult);
}

//根据id查询菜品信息
@DeleteMapping
    public Result<Void> delete(@RequestParam List<Long> ids){
log.info("删除菜品");
dishService.delete(ids);
return Result.success();
}


//修改菜品
    @PutMapping
    public Result<Void> update(@RequestBody DishDTO dishDTO){
       dishService.update(dishDTO);

return Result.success();
    }
}
