package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
   private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setMealDishMapper;
    @Override
    public void savaDish(DishDTO dishDTO) {
//首先要插入菜品的基本信息，才能获取到菜品id，才能保存口味数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
dishMapper.insert(dish);
//获取id
Long dishId = dish.getId();

//然后插入指定的口味表，dishId为菜品id(给每个口味数据设置菜品id，再批量插入口味数据)
        List<DishFlavor> flavors = dishDTO.getFlavors();
if(flavors!=null&&flavors.size()>0){
    for (DishFlavor flavor : flavors) {
flavor.setDishId(dishId);
    }
    dishFlavorMapper.insertBatch(flavors);
}

    }

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());

        Page<DishVO> page=dishMapper.page(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());


    }

    //批量删除菜品
    @Override
    public void delete(List<Long> ids) {
        //由于菜品和套餐是关联关系，所以要判断菜品是否关联了套餐，如果关联了套餐则不能删除
        //可以直接ids得到数组判断是否为空，也可以直接遍历ids判断，第一种更好
            int count = setMealDishMapper.countByDishId(ids);
            if(count>0){
               log.error("菜品正在被套餐关联，不能删除");

                throw  new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }


        //菜品如果启售状态，则不能删除
List<Dish> l=dishMapper.selectByIds(ids);
        for (Dish dish : l) {
            if(dish.getStatus()==1){
                //抛出异常
                log.error("菜品正在启售中，不能删除");
                throw  new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //删除菜品
dishMapper.delete(ids);
        //删除菜品对应的口味数据
dishFlavorMapper.deleteByDishIds(ids);
    }

    @Override

    //更新菜品，更新菜品需要更新菜品基本信息和口味数据，口味数据为删除再插入
    @Transactional
    public void update(DishDTO dishDTO) {
//更新方法为先更新菜品
//更新菜品需要除了口味的数据再根据id更新
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        Long dishId = dishDTO.getId();
//口味为删除再插入,更新口味需要口味数据,需要给口味数据的id赋值
        //删除口味数据
        dishFlavorMapper.deleteByDishId(dishId);
        //插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null&&flavors.size()>0){
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            dishFlavorMapper.insertBatch(flavors);
        }

    }


    //根据id查询菜品信息，查询菜品基本信息和口味数据，封装成DishVO返回
    @Override
    @Transactional
    public DishVO getById(Long id) {
        //由于要显示口味数据，所以要查询菜品基本信息和口味数据，最后封装成DishVO返回
        Dish dish= dishMapper.getByID(id);
List<DishFlavor> flavors = dishFlavorMapper.selectByIds(id);
DishVO dishVO = new DishVO();
BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors( flavors);
return dishVO;
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.update(dish);
    }

    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = new Dish();
        dish.setStatus(StatusConstant.ENABLE);
dish.setCategoryId(categoryId);
        return dishMapper.list(dish);


    }

    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> list = dishMapper.list(dish);
        List<DishVO> dishVOList = new ArrayList<>();
        for (Dish d : list) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);
            dishVO.setFlavors(dishFlavorMapper.selectByIds(d.getId()));
            dishVOList.add(dishVO);
            log.info("dishVO:{}",dishVO);
        }
return dishVOList;
    }


}
