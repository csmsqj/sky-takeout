package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    public void savaDish(DishDTO dishDTO);

    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    void delete(List<Long> ids);

    void update(DishDTO dishDTO);

    DishVO getById(Long id);

    void startOrStop(Integer status, Long id);

    List<Dish> list(Long categoryId);


    List<DishVO> listWithFlavor(Dish dish);
}
