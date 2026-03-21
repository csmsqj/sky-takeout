package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealSeivice;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/admin/setmeal")
@RestController
public class SetmealController {
    @Autowired
    private SetmealSeivice setmealSeivice;
    //新增套餐
    @PostMapping
    public Result<Void> save(@RequestBody SetmealDTO setmealDTO) {
        setmealSeivice.sava(setmealDTO);

        return Result.success();
    }

    //套餐分页查询
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        return setmealSeivice.page(setmealPageQueryDTO);

    }

    //数据回显功能
@GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        return setmealSeivice.getById(id);
    }

    //修改套餐
    @PutMapping
    public Result<Void> update(@RequestBody SetmealDTO setmealDTO) {

        log.info("修改套餐信息：{}", setmealDTO);
        setmealSeivice.update(setmealDTO);

        return Result.success();
    }


    //批量删除套餐
    @DeleteMapping
public Result<Void> delete(@RequestParam List<Long> ids) {
        log.info("批量删除套餐，ids：{}", ids);
        setmealSeivice.delete(ids);
        return Result.success();
    }


    //套餐起售、停售
    @PostMapping("/status/{status}")
public Result<Void> startOrStop(@PathVariable Integer status, Long id) {
        log.info("套餐起售、停售：{}", status);
        setmealSeivice.startOrStop(status, id);
        return Result.success();
    }
}
