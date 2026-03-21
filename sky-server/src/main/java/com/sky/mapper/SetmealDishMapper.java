package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SetmealDishMapper {

//哪一个套餐（setmealId）”里面包含了“哪一个菜品（dishId）”，以及这个菜品在套餐里有“几份（copies）
    int countByDishId(@Param("ids") List<Long> ids);


    void save(List<SetmealDish> setmealDishes);

    List<SetmealDish> getBySetmealId(Long id);

    void deleteBySetmealId(Long id);
}
