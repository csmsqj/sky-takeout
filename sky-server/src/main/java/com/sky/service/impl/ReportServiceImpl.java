package com.sky.service.impl;

import com.google.common.collect.Lists;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService{
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        log.info("营业额数据统计：{}到{}", begin, end);
        List<LocalDate> dateList = Lists.newArrayList();
        //date.plusDays(1) 不会修改 date 本身，LocalDate 是不可变对象，plusDays 返回新对象。你这样写是死循环。
        for(LocalDate date=begin;!date.equals( end);date=date.plusDays(1)){
            dateList.add(date);

        }
        dateList.add(end);
        String date = StringUtils.join(dateList, ",");
        List<Double> list=new ArrayList<>();
        for (LocalDate localDate : dateList) {
            //获取当天的营业额，需要查订单表条件是当天的最短时间和最晚时间,要求必须是已完成的订单
            //LocalDateTime的作用是把年月日时间和后面的时分秒时间拼接
            LocalDateTime endTime=localDate.atTime(LocalTime.MAX);
            LocalDateTime beginTime=localDate.atTime(LocalTime.MIN);
            Double amout=orderMapper.selectByBeginTimeAndEndTime(beginTime,endTime, Orders.COMPLETED);
//没有处理 NULL。没有订单的天 SUM() 返回 NULL，amout 就是 null，
// 加到 list 里变成 null。
// 拼接后 turnover 变成 "null,null,null,38.0,null,null,null"，前端拿到 "null" 就无法解析数字，所以不显示。
            if(amout==null){
                amout=0.0;
            }
list.add(amout);
        }

        String turnover = StringUtils.join(list, ",");

        return TurnoverReportVO.builder()
        .dateList(date)
        .turnoverList(turnover)
        .build();


    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = Lists.newArrayList();
        //date.plusDays(1) 不会修改 date 本身，LocalDate 是不可变对象，plusDays 返回新对象。你这样写是死循环。
        for(LocalDate date=begin;!date.equals( end);date=date.plusDays(1)){
            dateList.add(date);

        }
        dateList.add(end);
        String date = StringUtils.join(dateList, ",");
        List<Integer> totleUserlist=new ArrayList<>();
        List<Integer> newUserlist=new ArrayList<>();
        for(LocalDate localDate : dateList){
            LocalDateTime endTime = localDate.atTime(LocalTime.MAX);
LocalDateTime beginTime = localDate.atTime(LocalTime.MIN);

            Integer totalUser=userMapper.countByDate(null,endTime);
            Integer newUserCount=userMapper.countByDate(beginTime,endTime);
            totleUserlist.add(totalUser);
            newUserlist.add(newUserCount);
        }
        log.info("日期列表：{}", date);
        log.info("用户总量：{}", totleUserlist);
        log.info("新增用户：{}", newUserlist);
        return UserReportVO.builder()
                .dateList(date)
                .totalUserList(StringUtils.join(totleUserlist, ","))
                .newUserList(StringUtils.join(newUserlist, ","))
                .build();


    }

    @Override
    //订单总数和有效订单数
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = Lists.newArrayList();
        //date.plusDays(1) 不会修改 date 本身，LocalDate 是不可变对象，plusDays 返回新对象。
        for(LocalDate date=begin;!date.equals( end);date=date.plusDays(1)){
            dateList.add(date);

        }
        dateList.add(end);
        List<Integer> totalOrderCountList=new ArrayList<>();
        List<Integer> validOrderCountList=new ArrayList<>();
        Integer totalOrderCount=0;
        Integer validOrderCount=0;

        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = localDate.atTime(LocalTime.MIN);
            LocalDateTime endTime = localDate.atTime(LocalTime.MAX);
            Integer totalCount = orderMapper.countByDate(beginTime, endTime,null);
            Integer validCount = orderMapper.countByDate(beginTime, endTime, Orders.COMPLETED);
            totalOrderCountList.add(totalCount);
            validOrderCountList.add(validCount);
            totalOrderCount+=totalCount;
            validOrderCount+=validCount;
        }
        Double orderCompletionRate = validOrderCount.doubleValue()/totalOrderCount;
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(totalOrderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();

    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        //菜品的销量前十
        // 要求首先是已完成订单，所以必须要查询菜评表，并且还要求统计数量,菜品名称，在明细表中，所以是双表查询

List<String> nameList=new ArrayList<>();
List<Integer> numberList=new ArrayList<>();
//本可以用专门的类封装，但是对于两个，为了方便就用 map 集的数据作为键，后面每一行的数据作为值，形成键值对集合。每一行就是一个键值对集合，
// 所以这是一个键值对集合的集合，第一行是名称字符串类型
// 第二行封装的是值，它至少有两行，会有很多种类型，所以用 OBJECT 类型合。那么它的封装规则是第一行
        List<Map<String,Object>> map=orderMapper.selectTop10(begin.atTime(LocalTime.MIN),end.atTime(LocalTime.MAX),Orders.COMPLETED);
        for (Map<String, Object> m : map) {
            String name = (String)m.get("name");
            //由于sum总数量返回类型不确定，而且只能转化为肯定的类型。所以先转化为所有 long,integer 的父类 number，再转化为 integer
            Integer number = ((Number)m.get("number")).intValue();
            nameList.add( name);
            numberList.add(number);

        }
        log.info("名称：{}", nameList);
        log.info("数量：{}", numberList);
return SalesTop10ReportVO.builder()
.nameList(StringUtils.join(nameList, ","))
.numberList(StringUtils.join(numberList, ","))
.build();
    }
}
