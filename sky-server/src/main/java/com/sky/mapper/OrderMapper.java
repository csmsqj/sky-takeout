package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.exception.OrderBusinessException;
import com.sky.result.PageResult;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    void insert(Orders order);

    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    void update(Orders orders);

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);


    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);



    @Select("select count(*) from orders where status = #{status}")
    Integer countByStatus(Integer status);

    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> selectByStatusAndOrderTimeOut(Integer status, LocalDateTime orderTime);



   Double selectByBeginTimeAndEndTime(@Param("beginTime") LocalDateTime beginTime,
                                     @Param("endTime") LocalDateTime endTime,
                                     @Param("status") Integer status);


    Integer countByDate(@Param("beginTime")  LocalDateTime beginTime,@Param("endTime") LocalDateTime endTime,@Param("status") Integer status);

    List<Map<String, Object>> selectTop10(@Param("beginTime") LocalDateTime beginTime,@Param("endTime") LocalDateTime endTime,@Param("status") Integer status);
}
