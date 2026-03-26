package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.impl.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/admin/report")
public class ReportController {
@Autowired
private ReportService reportService;


//营业额数据统计
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("营业额数据统计：{}到{}", begin, end);
TurnoverReportVO turnoverReportVO = reportService.turnoverStatistics(begin, end);

        return Result.success(turnoverReportVO);
    }


    //查找用户总量和新增用户数量
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("营业额数据统计：{}到{}", begin, end);
        UserReportVO userReportVO = reportService.userStatistics(begin, end);

        return Result.success(userReportVO);
    }

    //订单总数和有效订单数
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> orderStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("营业额数据统计：{}到{}", begin, end);
        OrderReportVO orderReportVO = reportService.orderStatistics(begin, end);
        return Result.success(orderReportVO);

    }

@GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(
           @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
         @DateTimeFormat(pattern="yyyy-MM-dd")   LocalDate end
){
        log.info("营业额数据统计：{}到{}", begin, end);
        SalesTop10ReportVO salesTop10ReportVO = reportService.top10(begin, end);

        return Result.success(salesTop10ReportVO);
}



}
