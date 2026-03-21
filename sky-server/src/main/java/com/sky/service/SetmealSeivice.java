package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealSeivice {
   

    void sava(SetmealDTO setmealDTO);

    Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO);

    Result<SetmealVO> getById(Long id);

    void update(SetmealDTO setmealDTO);

    void delete(List<Long> ids);

    void startOrStop(Integer status, Long id);
}
