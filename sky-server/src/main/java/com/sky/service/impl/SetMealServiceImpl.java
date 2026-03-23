package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service

public class SetMealServiceImpl implements SetmealService {

@Autowired
private SetmealMapper setMealMapper;
@Autowired
private SetmealDishMapper setMealDishMapper;
@Autowired
private DishMapper dishMapper;
    @Override
    @Transactional
    public void sava(SetmealDTO setmealDTO) {
        log.info("新增套餐：{}", setmealDTO);
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //设计为setmeal为套餐，setmealdish为套餐中的菜品
        setMealMapper.save(setmeal);
        //获取数据库生成的套餐id,通过setmeal对象获取id，
        // 因为在保存套餐时，数据库会自动生成一个唯一的id，并将这个id设置到setmeal对象中，所以我们可以通过setmeal.getId()来获取这个生成的id。
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
setMealDishMapper.save(setmealDishes);

    }

    @Override
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询：{}", setmealPageQueryDTO);
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setMealMapper.page(setmealPageQueryDTO);
        long total = page.getTotal();
        List<SetmealVO> dishVO = page.getResult();
return Result.success(new PageResult(total,dishVO));
    }


    //数据回显功能
    @Override
    public Result<SetmealVO> getById(Long id) {
        log.info("根据id查询套餐信息：{}", id);
        //要显示的数据：套餐基本信息，套餐包含的菜品信息
        Setmeal setmeal = setMealMapper.getById(id);
        List<SetmealDish> setmealDishes = setMealDishMapper.getBySetmealId(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return Result.success(setmealVO);



    }


    //修改套餐
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        log.info("修改套餐信息：{}", setmealDTO);
       Setmeal setmeal = new Setmeal();
BeanUtils.copyProperties(setmealDTO,setmeal);
       setMealMapper.update(setmeal);
       //删除套餐中的菜品,根据套餐id
        Long id = setmealDTO.getId();
        setMealDishMapper.deleteBySetmealId(id);

        //重新添加套餐中的菜品,为1对多，需要为每个菜品设置套餐id
//如果未传菜品信息，则直接返回
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if(setmealDishes!=null && setmealDishes.size()>0) {
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(id);
            }
            setMealDishMapper.save(setmealDishes);
        }

    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        //1.判断套餐是否在售卖，如果在售卖，抛出一个业务异常
        List<Setmeal> setmeals = setMealMapper.getByIds(ids);
        for (Setmeal setmeal : setmeals) {
            if(setmeal.getStatus()==1){
                throw new RuntimeException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        //2.删除套餐表中的数据
setMealMapper.deleteByIds(ids);
        //删除套餐菜品关系表
        for (Long id : ids) {
            setMealDishMapper.deleteBySetmealId(id);
        }


    }

    @Override
    public void startOrStop(Integer status, Long id) {
        log.info("套餐起售、停售：{}", status);
        //首先获取菜品信息(根据套餐id在多表中查），判断菜品是否在售卖
        List<SetmealDish> list = setMealDishMapper.getBySetmealId(id);
        for (SetmealDish setmealDish : list) {
            Long dishId = setmealDish.getDishId();
if(dishMapper.getByID(dishId).getStatus()==0){
                throw new RuntimeException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }

        //根据id和状态修改套餐的状态,相当于更新，复用update方法
        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);
        setMealMapper.update(setmeal);
    }

    //根据条件查询套餐列表，条件是分类 ID，查询状态。
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list=setMealMapper.list(setmeal);
        return list;
    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setMealMapper.getDishItemById(id);

    }
}
