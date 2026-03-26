package com.sky.service.impl;

import com.google.common.collect.Lists;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceService workspaceService;

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
        Double orderCompletionRate = 0.0;
        if(totalOrderCount!=0){
            orderCompletionRate = validOrderCount.doubleValue()/totalOrderCount;
        }

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

    @Override
    public void export(HttpServletResponse response) {
        //先查询数据库获取营业数据
        LocalDateTime begin =LocalDate.now().atTime(LocalTime.MIN).plusDays(-29);
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);

        //读入 EXCEL 文件，再把它输入到浏览器当中
        InputStream inputStream= this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        //在 FINALLY 中关闭流，确保它们始终可以关闭，不会直接抛出异常。所以在外部声明变量
        XSSFWorkbook xssfWorkbook=null;
        ServletOutputStream servletOutputStream=null;



        try {
             xssfWorkbook= new XSSFWorkbook(inputStream);
            //填充数据,首先获取 SHEET，再获取行，再获取格子
            XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(0);
xssfSheet.getRow(1).getCell(1).setCellValue(begin+" to "+end);
xssfSheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());
            xssfSheet.getRow(3).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            xssfSheet.getRow(3).getCell(6).setCellValue(businessDataVO.getNewUsers());
            xssfSheet.getRow(4).getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            xssfSheet.getRow(4).getCell(4).setCellValue(businessDataVO.getUnitPrice());
            LocalDate begin2=LocalDate.now().plusDays(-29);
            for(int i=0;i<30;i++) {
                LocalDate date=begin2.plusDays(i);
                //必须要通过当天年月日时间来查,再把它转化为年月日时分秒
                BusinessDataVO businessDataVOI = workspaceService.getBusinessData(date.atTime(LocalTime.MIN), date.atTime(LocalTime.MAX));
                XSSFRow row = xssfSheet.getRow(7 + i);
row.getCell(1).setCellValue(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                row.getCell(2).setCellValue(businessDataVOI.getTurnover());
                row.getCell(3).setCellValue(businessDataVOI.getValidOrderCount());
                row.getCell(4).setCellValue(businessDataVOI.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessDataVOI.getUnitPrice());
                row.getCell(6).setCellValue(businessDataVOI.getNewUsers());
            }

            //用输出流输出到浏览器当中
             servletOutputStream= response.getOutputStream();
xssfWorkbook.write(servletOutputStream);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                if(inputStream!=null){
                    inputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                if(servletOutputStream!=null){
                    servletOutputStream.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                if(xssfWorkbook!=null){
                    xssfWorkbook.close();
                }
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }


    }


    }
}
