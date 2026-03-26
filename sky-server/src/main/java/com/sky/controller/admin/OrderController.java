package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.mapper.OrderMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Size;

@Slf4j
@RestController
@RequestMapping("/admin/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 条件搜索订单
     *
     * @param
     * @return 搜索结果
     */
    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("条件搜索订单，搜索条件：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);


        return Result.success(pageResult);


    }


    //查询对应待接单、派送中等等的数量
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics() {
        log.info("查询订单统计数据");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();

        return Result.success(orderStatisticsVO);
    }

    //查询订单详情，要求是既包含订单又包含订单的详细信息
    @GetMapping("/details/{id}")
    public Result<OrderVO> details(@PathVariable Long id) {
        log.info("查询订单详情，订单id：{}", id);
        OrderVO orderVO = orderService.details(id);

        return Result.success(orderVO);

    }

    //接单是通过给订单 ID 修改对应状态
    @PutMapping("/confirm")
    public Result confim(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接单，订单id：{}", ordersConfirmDTO.getId());
        orderService.confim(ordersConfirmDTO);

        return Result.success();
    }

    @PutMapping("/rejection")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        log.info("拒单，订单id：{}", ordersRejectionDTO.getId());
        orderService.rejection(ordersRejectionDTO);

        return Result.success();
    }

    @PutMapping("/cancel")
    //管理员取消订单，订单状态修改为已取消
    public Result adminCancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("取消订单，订单id：{}", ordersCancelDTO.getId());
        orderService.adminCancle(ordersCancelDTO);

        return Result.success();

    }

      @PutMapping("delivery/{id}")
      //修改为派送中
    public Result delivery(@PathVariable Long id) {
        log.info("订单派送，订单id：{}", id);
      orderService.delivery(id);

        return Result.success();
    }

//完成订单
    @PutMapping("/complete/{id}")
public Result complete(@PathVariable Long id) {
        log.info("订单完成，订单id：{}", id);
        orderService.complete(id);

        return Result.success();

    }


}
