package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    void insertBatch(@Param("orderDetails") List<OrderDetail> orderDetails);

    List<OrderDetail> selectByOrderIds(@Param("list") List<Long> list);
}
