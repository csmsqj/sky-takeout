package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DishFlavorMapper {


    void insertBatch(@Param("flavors") List<DishFlavor> flavors);
    //根据菜品id批量查询口味数据

    void deleteByDishIds(@Param("ids") List<Long> ids);

    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);

    @Select("select * from dish_flavor where dish_id = #{id}")
    List<DishFlavor> selectByIds(Long id);
}
