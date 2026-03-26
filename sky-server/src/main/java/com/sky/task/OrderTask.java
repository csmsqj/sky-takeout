package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;


    //订单超时订单
    //每分钟执行一次
    @Scheduled(cron = "0 * * * * ? ")
    public void orderTimeOut() {
        log.info("订单超时订单{}", LocalDateTime.now());
//订单表中查超时订单，具体是要查未付款且超过15分钟的订单(现在的时间更多一些)
        List<Orders> orders = orderMapper.selectByStatusAndOrderTimeOut(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));

        //这里其实可以便利获得所有的 ID 列表，然后将 ID 列表状态取消原因封装为特殊的函数。这样的话，只需要操作一次数据库，更加节省时间
        if(orders != null&&orders.size()>0) {
            for (Orders order : orders) {
                Orders o = Orders.builder()
                        .id(order.getId())
                        .status(Orders.CANCELLED)
                        .cancelReason("订单超时，自动取消")
                        .build();
                orderMapper.update(o);

            }
        }



    }


    //物理管理端未点击完成的订单
@Scheduled(cron = "0 0 1 * * ? ")//每天凌晨1点执行
    public void orderFinish() {
        log.info("物理管理端未点击完成的订单{}", LocalDateTime.now());
        List<Orders> orders = orderMapper.selectByStatusAndOrderTimeOut(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusHours(-1));
        if(orders != null&&orders.size()>0) {
            for (Orders order : orders) {
                Orders o = Orders.builder()
                        .id(order.getId())
                        .status(Orders.COMPLETED)
                        .build();
                orderMapper.update(o);

            }
        }
    }

}
