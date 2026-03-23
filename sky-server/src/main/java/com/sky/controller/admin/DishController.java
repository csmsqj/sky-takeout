package com.sky.controller.admin;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.impl.DishServiceImpl;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
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
    @CachePut(cacheNames = "dishCache", key = "#dishDTO.categoryId")
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

//根据id删除菜品信息
@CacheEvict(cacheNames = "dishCache",allEntries = true)
@DeleteMapping
    public Result<Void> delete(@RequestParam List<Long> ids){
log.info("删除菜品");
dishService.delete(ids);
return Result.success();
}


//修改菜品
    @PutMapping
    @CacheEvict(cacheNames = "dishCache",allEntries = true)
    public Result<Void> update(@RequestBody DishDTO dishDTO){
       dishService.update(dishDTO);

return Result.success();
    }

    //根据id查询菜品
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id){
log.info("根据id查询菜品");
DishVO dishVO = dishService.getById(id);
return Result.success(dishVO);
    }

//起售或停售菜品
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "dishCache",allEntries = true)
    public Result<Void> startOrStop(@PathVariable Integer status,@RequestParam Long id){
log.info("起售或停售");
dishService.startOrStop(status,id);
return Result.success();
    }

    //根据分类id查询菜品
    @GetMapping("/list")
    public Result<List<Dish>> list(Long categoryId){
log.info("根据分类id查询菜品");
List<Dish> list = dishService.list(categoryId);
return Result.success(list);
    }

}
