package com.sky.mapper;


import com.sky.entity.ShoppingCart;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface ShoppingCartMapper {

    //查询购物车项，可以将下面的替换购物车项代替
    public List<ShoppingCart> list(ShoppingCart shoppingCart);

    //修改购物车项数量
    @Select("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumber(ShoppingCart shoppingCart1);

    //添加购物车项
    void save(ShoppingCart shoppingCart);

    //根据用户ID查询购物车项
    @Select("select * from shopping_cart where user_id = #{userId}")
    List<ShoppingCart> selectShoppingCart(Long userId);

//删除购物车项
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);
}
