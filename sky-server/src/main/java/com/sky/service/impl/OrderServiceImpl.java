package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    //用户下单
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO orderSubmitDTO) {
        //1判断是否异常，比如地址簿id是否存在(通过Dto的地址id查)，
        AddressBook addressBook = addressBookMapper.getById(orderSubmitDTO.getAddressBookId());
        if (addressBook == null) {

            throw new RuntimeException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId = BaseContext.getCurrentId();
//购物车是否为空（通过user_id）
        List<ShoppingCart> list = shoppingCartMapper.getByUserId(userId);
        if (list == null || list.size() == 0) {
            throw new RuntimeException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //2订单表插入一条数据,需要Order对象,额外需要设置时间，状态，订单号，user用户id，（手机号，地址，收件人）
        Orders order = new Orders();
        BeanUtils.copyProperties(orderSubmitDTO, order);
        order.setAddress(addressBook.getDetail());// 地址
        order.setPhone(addressBook.getPhone());//手机号
        order.setConsignee(addressBook.getConsignee());//收货人
        order.setUserId(userId);//用户id
        order.setOrderTime(LocalDateTime.now());//订单时间
        order.setStatus(Orders.PENDING_PAYMENT);//待付款
        order.setPayStatus(Orders.UN_PAID);//待支付
        order.setNumber(String.valueOf(System.currentTimeMillis()));//订单号
        orderMapper.insert(order);//注意插入的id会回填到order对象中
        //3订单明细表插入多条数据（除了购物车数据的菜品与口味或者套餐，价格，菜品id,name,image插入,还需要补充OrderId，所以封装到对象）
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart shoppingCart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetails.add(orderDetail);

        }
        orderDetailMapper.insertBatch(orderDetails);

        //4清空购物车
        shoppingCartMapper.deleteByUserId(userId);

        //5返回指定的VO对象
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderTime(order.getOrderTime())
                .orderAmount(order.getAmount())
                .build();
        return orderSubmitVO;

    }

    /**
     * 模拟支付（跳过微信支付，直接修改订单状态）
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        log.info("模拟支付，订单号：{}", ordersPaymentDTO.getOrderNumber());

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());

        // 直接修改订单状态为"待接单"，支付状态为"已支付"
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED) // 待接单
                .payStatus(Orders.PAID) // 已支付
                .checkoutTime(LocalDateTime.now()) // 结账时间
                .build();
        orderMapper.update(orders);

        // 返回空的VO，前端拿到非null就认为成功
        return new OrderPaymentVO();
    }

    @Override
    public PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        if (page == null || page.size() == 0) {
            return new PageResult(0L, null);
        }

        List<Long> orderIds = new ArrayList<>();
        for (Orders orders : page) {
            orderIds.add(orders.getId());
        }
        // 由于订单详细要在订单类中，一次性查所有订单的明细，按 orderId分组
        List<OrderDetail> allDetails = orderDetailMapper.selectByOrderIds(orderIds);
        // 按 orderId 分组
        java.util.Map<Long, List<OrderDetail>> detailMap = new java.util.HashMap<>();
        for (OrderDetail detail : allDetails) {
            detailMap.computeIfAbsent(detail.getOrderId(), k -> new ArrayList<>()).add(detail);
        }

        List<OrderVO> orderVOList = new ArrayList<>();
        for (Orders orders : page) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDetailList(detailMap.getOrDefault(orders.getId(), new ArrayList<>()));
            orderVOList.add(orderVO);
        }
        return new PageResult(page.getTotal(), orderVOList);
    }

    @Override
    // 订单详情
    public OrderVO orderDetailById(Long id) {
        //先查询订单，通过id查询订单详情
        Orders orders = orderMapper.getById(id);
        Long OrderId = orders.getId();
        List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderIds(List.of(OrderId));
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;

    }

    @Override
    public void cancle(Long id) {
        //存在性与状态拦截：根据 id 查出数据库实体 ordersDB。若为空，抛出带常量的业务异常
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }
//仅允许待付款1、待接单2取消
        if (orders.getStatus() != Orders.PENDING_PAYMENT && orders.getStatus() != Orders.TO_BE_CONFIRMED) {
            throw new RuntimeException(MessageConstant.ORDER_STATUS_ERROR);
        }
///满足则将状态改为已取消4，必须要新建一个 Orders 对象，设置 id 和 status，调用 update 方法
        Orders updateOrders = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .build();
        orderMapper.update(updateOrders);


    }

    @Override
    public void repetition(Long id) {
        //将订单详情表数据查询出来，封装到购物车对象
        List<OrderDetail> orderDetails = orderDetailMapper.selectByOrderIds(List.of(id));
        if (orderDetails == null || orderDetails.size() == 0) {
            throw new RuntimeException(MessageConstant.ORDER_NOT_FOUND);
        }
        //不要n+1，使用批量添加
        List<ShoppingCart> shoppingCarts = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart, "id");
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCarts.add(shoppingCart);



        }

        shoppingCartMapper.saveBanch(shoppingCarts);

    }

    @Override
    //管理端订单条件查询
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("条件搜索订单，搜索条件：{}", ordersPageQueryDTO);
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        //对订单进性条件查询，订单号，手机号，订单状态，时间范围，用户id（可选）都可以作为条件
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
//不再关注 VO 中的 orderDetailList（PC端列表不展示菜品大图片），而
// 是特意启用了 VO 中的 String orderDishes 字段（订单菜品信息字符串）。
// 调用私有辅助方法，进行数据转换与降维组装
        List<OrderVO> orderVOList = getOrderVOList(page);

        // 4. 封装并返回标准的分页结果对象
        return new PageResult(page.getTotal(), orderVOList);
    }


    /**
     * 私有辅助方法：将 Page<Orders> 转换为 List<OrderVO> 并拼接菜品字符串
     * 用途：管理端订单列表页，需要把每个订单的菜品明细拼成一行文字概述
     * 例如："宫保鸡丁*3;鱼香肉丝*1;"
     */
    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        List<OrderVO> orderVOList = new ArrayList<>();

        // 获取当前页的订单列表
        List<Orders> ordersList = page.getResult();

        // 防御性判断：如果没有订单数据，直接返回空集合
        if (ordersList == null || ordersList.isEmpty()) {
            return orderVOList;
        }

        // ==================== 第一步：批量查询所有订单的明细（避免N+1） ====================
        // 收集当前页所有订单的ID
        List<Long> orderIds = new ArrayList<>();
        for (Orders orders : ordersList) {
            orderIds.add(orders.getId());
        }

        // 一次SQL查出所有订单的明细，而不是每个订单查一次
        List<OrderDetail> allDetails = orderDetailMapper.selectByOrderIds(orderIds);

        // ==================== 第二步：按orderId分组，方便后续取用 ====================
        // key=订单ID，value=该订单下的所有明细列表
        java.util.Map<Long, List<OrderDetail>> detailMap = new java.util.HashMap<>();
        for (OrderDetail detail : allDetails) {
            // computeIfAbsent：如果key不存在就创建新ArrayList，然后把当前明细加进去
            detailMap.computeIfAbsent(detail.getOrderId(), k -> new ArrayList<>()).add(detail);
        }

        // ==================== 第三步：遍历每个订单，组装OrderVO ====================
        for (Orders orders : ordersList) {
            OrderVO orderVO = new OrderVO();

            // 将 Orders 的基础字段（id, number, status, amount, phone...）拷贝到 OrderVO
            // OrderVO extends Orders，所以所有父类字段都能被拷贝
            BeanUtils.copyProperties(orders, orderVO);

            // 从分组map中取出当前订单的明细列表
            List<OrderDetail> orderDetailList = detailMap.getOrDefault(orders.getId(), new ArrayList<>());

            // ============ 拼接菜品概述字符串 ============
            // 目的：管理端列表页空间有限，用一行文字展示"这单点了啥"
            // 效果：如 "宫保鸡丁*3;鱼香肉丝*1;孙祖大礼包*2;"
            if (!orderDetailList.isEmpty()) {
                // 把每个明细拼成 "菜名*数量;" 的格式
                List<String> dishStrings = new ArrayList<>();
                for (OrderDetail detail : orderDetailList) {
                    dishStrings.add(detail.getName() + "*" + detail.getNumber() + ";");
                }
                // 拼成一个完整字符串，设置到VO
                String orderDishes = String.join("", dishStrings);
                orderVO.setOrderDishes(orderDishes);
            }

            orderVOList.add(orderVO);
        }

        return orderVOList;
    }
}
