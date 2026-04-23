package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.github.pagehelper.Page;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {


    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

@AutoFill(value= OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> page(DishPageQueryDTO dishPageQueryDTO);

    //根据id查询菜品信息
    List<Dish> selectByIds(@Param("ids") List<Long> ids);

    //批量删除菜品

    void delete(List<Long> ids);

    @AutoFill(value= OperationType.UPDATE)
    void update(Dish dish);

    @Select("select * from dish where id = #{id}")
    Dish getByID(Long id);

    List<Dish> list(Dish dish);

    Integer countByMap(Map map);
}
