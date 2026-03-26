package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("userOrderController")
@RequestMapping("/user/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    // 用户下单
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO orderSubmitDTO) {
        log.info("用户下单：{}", orderSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submit(orderSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    // 模拟支付
    @PutMapping("/payment")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) {
        log.info("模拟支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        return Result.success(orderPaymentVO);
    }

    // 分页查询历史订单
    @GetMapping("/historyOrders")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult pageResult = orderService.historyOrders(ordersPageQueryDTO);

        return Result.success(pageResult);
    }

    // 查看摸一个订单详情
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> orderDetailById(@PathVariable Long id) {
        log.info("查看订单详情，订单id为：{}", id);
        OrderVO orderVO = orderService.orderDetailById(id);
        return Result.success(orderVO);
    }

    // 取消订单
@PutMapping("/cancel/{id}")
    public Result<Void> cancle(@PathVariable Long id) {
        log.info("取消订单，订单id为：{}", id);
        orderService.cancle(id);
        return Result.success();
    }

    //再来一单,就是将原订单中的商品重新加入到购物车中
    @PostMapping("/repetition/{id}")
    public Result<Void> repetition(@PathVariable Long id) {
        log.info("再来一单，订单id为：{}", id);
        orderService.repetition(id);
        return Result.success();
    }


    //订单提醒用户点击催单会提醒浏览器管理端
    @GetMapping("/reminder/{id}")
    public Result<Void> reminder(@PathVariable Long id) {
        log.info("订单提醒，订单id为：{}", id);
        orderService.reminder(id);
        return Result.success();
    }

}
