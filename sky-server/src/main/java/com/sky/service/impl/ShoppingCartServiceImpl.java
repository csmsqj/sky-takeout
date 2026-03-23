package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    @Transactional
    //添加购物车项
    public void add(ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车，shoppingCartDTO:{}", shoppingCartDTO);
        // 检查购物车中是否已经存在相同的菜品或套餐，如果存在则增加数量，否则添加新的购物车项
        //必须知道用户的ID才能进行查询，
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);

        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
//由于菜品和套餐都要查，合并为1个，要么查菜品，要么查套餐（菜品查菜品id和口味，套餐查套餐id），都需要满足一个用户
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list != null && list.size() > 0) {
            ShoppingCart shoppingCart1 = list.get(0);
            shoppingCart1.setNumber(shoppingCart1.getNumber() + 1);
            //修改数量,根据购物车的ID来修改,这里的购物车ID已经确定了
            shoppingCartMapper.updateNumber(shoppingCart1);
        }
//如果不存在，则添加新的购物车项
        else {
            //判断到底是菜品还是套餐,给购物车项添加名称、图片、价格等信息
            Long setmealId = shoppingCartDTO.getSetmealId();
            if (setmealId != null) {
                //套餐
                Setmeal byId = setmealMapper.getById(setmealId);
                shoppingCart.setName(byId.getName());
                shoppingCart.setImage(byId.getImage());
                shoppingCart.setAmount(byId.getPrice());
                shoppingCart.setCreateTime(LocalDateTime.now());

            } else {
                //菜品
                Dish byID = dishMapper.getByID(shoppingCartDTO.getDishId());
                shoppingCart.setImage(byID.getImage());
                shoppingCart.setName(byID.getName());
                shoppingCart.setAmount(byID.getPrice());
                shoppingCart.setCreateTime(LocalDateTime.now());

            }
            shoppingCart.setNumber(1);
            shoppingCartMapper.save(shoppingCart);
        }
    }

    @Override
    public List<ShoppingCart> selectShoppingCart() {
        //查询当前用户的购物车项,根据用户ID查询
        Long userId = BaseContext.getCurrentId();
        //可以复用查询所有的方法，这里写根据用户id查询的方法
       return  shoppingCartMapper.selectShoppingCart(userId);


    }

    @Override
    public void clean() {
        //清空购物车,根据用户ID删除(在拦截器中已经获取了用户ID并放入了BaseContext中，所以这里可以直接获取用户ID)
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }
}
