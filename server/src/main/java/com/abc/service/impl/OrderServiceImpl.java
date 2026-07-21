package com.abc.service.impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.abc.constant.MessageConstant;
import com.abc.context.CurrentHolder;
import com.abc.dto.*;
import com.abc.entity.*;
import com.abc.exception.AddressBookBusinessException;
import com.abc.exception.OrderBusinessException;
import com.abc.exception.ShoppingCartBusinessException;
import com.abc.mapper.*;
import com.abc.result.PageResult;
import com.abc.service.OrderService;
import com.abc.vo.OrderPaymentVO;
import com.abc.vo.OrderStatisticsVO;
import com.abc.vo.OrderSubmitVO;
import com.abc.vo.OrderVO;
import com.abc.websocket.WebSocketServer;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final AddressBookMapper addressBookMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final UserMapper userMapper;
    private final WebSocketServer webSocketServer;
    private final CommentMapper commentMapper;

    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrderSubmitDTO orderSubmitDTO) {

        //处理业务异常(地址为空，购物车为空)
        //收货地址是空的吗
        AddressBook addressBook = addressBookMapper.getById(orderSubmitDTO.getAddressBookId());
        if (addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //购物车是空的吗
        Long userId = CurrentHolder.getCurrentId();
        ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        shoppingCartItem.setUserId(userId);
        List<ShoppingCartItem> list = shoppingCartMapper.list(shoppingCartItem);
        if (list == null || list.isEmpty()){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单表插入一条数据
        Orders order = new Orders();
        BeanUtils.copyProperties(orderSubmitDTO,order);
        order.setNumber(System.currentTimeMillis() + userId.toString());
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setUserId(userId);
        order.setOrderTime(LocalDateTime.now());
//        order.setCheckoutTime();
        order.setPayStatus(Orders.UN_PAID);
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getProvinceName()+addressBook.getCityName()+addressBook.getDistrictName()+addressBook.getDetail());
//        order.setUserName(addressBook.);
        order.setConsignee(addressBook.getConsignee());

        orderMapper.insert(order);
        //向订单明细表插入n条数据
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCartItem item : list){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(item,orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);
        //清空用户的购物车
        shoppingCartMapper.deleteByUserId(userId);
        //封装VO并返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderTime(order.getOrderTime())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .build();
        return orderSubmitVO;
    }

    public OrderPaymentVO payment(OrderPaymentDTO orderPaymentDTO){
        Long userId = CurrentHolder.getCurrentId();
        User user = userMapper.getById(userId);
        /*
        这里是微信支付的代码
         */
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getStr("code") != null && jsonObject.getStr("code").equals("ORDERPAID")){
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toBean(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getStr("package"));
        return vo;
    }

    public void paySuccess(String orderNumber){
        Long userId = CurrentHolder.getCurrentId();

        Orders orderDB = orderMapper.getByNumberAndUserId(orderNumber,userId);

        Orders order = Orders.builder()
                .id(orderDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(order);
        //通过websocket向客户端浏览器推送消息 type oderId content
        Map map = new HashMap<>();
        map.put("type",1);
        map.put("orderId",orderDB.getId());
        map.put("content","订单号:"+orderNumber);
        String json = JSONUtil.toJsonStr(map);
        webSocketServer.sendToAllClient(json);
    }

    @Override
    public PageResult<OrderVO> pageQuery4User(OrderPageQueryDTO orderPageQueryDTO) {
        PageHelper.startPage(orderPageQueryDTO.getPage(),orderPageQueryDTO.getPageSize());

        //分页条件查询
        Page<Orders> page = orderMapper.pageQuery(orderPageQueryDTO);

        List<OrderVO> list = new ArrayList<>();

        if (page != null && page.getTotal() > 0){
            for (Orders order: page){
                Long orderId = order.getId();
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
                OrderVO orderVO = new OrderVO();
                //订单的基本信息
                BeanUtils.copyProperties(order, orderVO);
                //订单中下关系的菜品信息
                orderVO.setOrderDetailList(orderDetails);
                //对待已完成订单的处理
                if (Objects.equals(order.getStatus(), Orders.COMPLETED)){
                    Integer comments = commentMapper.selectByOrderId(order.getId());
                    //情况1： comments为空 -> 未评价
                    if (comments == 0){
                        orderVO.setCommentBtnText("评价");
                    } else if (comments == 1) {
                        //情况2： 有评价记录，但还没有追评过 -> 可以追评 0=为追评，1=已追评
                        orderVO.setCommentBtnText("追评");
                    }else{
                        //情况3： 已经追评过了 -> 按钮隐藏或置灰
                        orderVO.setCommentBtnText("已追评");
                    }
                }else{
                    orderVO.setCommentBtnText(null);
                }

                //把上面两部分数据整合为OrderVO,符合接口中后端返回数据的格式
                list.add(orderVO);
            }
        }
        return new PageResult<>(page.getTotal(),list);
    }

    @Override
    public OrderVO getOrderDetails(Long id) {
        Orders orders = orderMapper.getDetailsById(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void userCancelById(Long id) {
        //根据id查询订单
        Orders orders = orderMapper.getByOrderId(id);
        //校验订单是否存在
        if (orders == null){
            //订单不存在
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //分析订单状态，1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (orders.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //订单处于 2待接单 状态下取消，需要进行退款
        if (orders.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            /*
           退款逻辑
             */
        }
        //如果订单处于 1未付款 则不必考虑退款逻辑

        //更新订单状态，取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void repetitionOrder(Long id) {
        //先根据id查询订单包含了哪些商品
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        //将订单详情中的内容 装到 购物车里
        List<ShoppingCartItem> cart = new ArrayList<>();
        for (OrderDetail od: orderDetails){
            ShoppingCartItem item = new ShoppingCartItem();
            BeanUtils.copyProperties(od,item);
            item.setUserId(CurrentHolder.getCurrentId());
            item.setCreateTime(LocalDateTime.now());
            cart.add(item);
        }
        shoppingCartMapper.insertBatch(cart);
    }

    /**
     * 分页查询订单信息(带条件筛选)
     * @param orderPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrderPageQueryDTO orderPageQueryDTO) {
        PageHelper.startPage(orderPageQueryDTO.getPage(),orderPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(orderPageQueryDTO);
        List<OrderVO> orderVOList = getOrderVOList(page);
        return new PageResult(page.getTotal(), orderVOList);
    }


    /**
     * 将当前分页查询到的数据从Orders包装为OrderVO
     * 其实就是填充个 orderDishes字段 （orderDetailList字段是user端查询订单用的，这里不用管）
     * @param page
     * @return
     */
    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        //初步从数据库orders表中查到的信息
        List<Orders> ordersList = page.getResult();
        //需要对上面的数据进行封装，使其满足前端接口需要的格式
        List<OrderVO> orderVOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ordersList)){
            for (Orders orders: ordersList){
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                //根据这条订单的信息去order_detail表里的查询订单的菜品名称和数量，并拼接为字符串
                String orderDishes = getOrderDetailsStr(orders);
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单Id查询订单中的菜品和对应的数量，并拼成字符串
     * @param orders
     * @return
     */
    private String getOrderDetailsStr(Orders orders) {
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
        StringBuilder dishNameAndNumber = new StringBuilder();
        for (OrderDetail od: orderDetails){
            String dishName = od.getName();
            Integer dishNumber = od.getNumber();
            dishNameAndNumber.append(dishName).append("*").append(dishNumber.toString());
        }
        return dishNameAndNumber.toString();
    }


    @Override
    public OrderVO getOrderInfo(Long id) {
        Orders orders = orderMapper.getByOrderId(id);
        List<OrderDetail> details = orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(details);
        orderVO.setOrderDishes(getOrderDetailsStr(orders));
        return orderVO;
    }

    @Override
    public OrderStatisticsVO orderstistics() {
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);

        return new OrderStatisticsVO(confirmed,deliveryInProgress,toBeConfirmed);
    }

    @Override
    public void confirm(OrderConfirmDTO orderConfirmDTO) {
        Orders orders = orderMapper.getByOrderId(orderConfirmDTO.getId());
        //订单不存在或订单状态不是 "待接单"
        if (orders == null || !orders.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders1 = Orders.builder()
                .id(orderConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders1);
    }

    @Override
    public void rejection(OrderRejectionDTO orderRejectionDTO) {

        Orders orders = orderMapper.getByOrderId(orderRejectionDTO.getId());
        //订单不存在或订单状态不是 "待接单"
        if (orders == null || !orders.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //得看一看是否已支付，用户支付了的订单在拒单时还得退钱
        Integer payStatus = orders.getPayStatus();
        if (payStatus == Orders.PAID){
            //用户已经支付了订单，在拒单时得退钱
            /*
            退款逻辑
             */
        }
        Orders orders1 = Orders.builder()
                .id(orderRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(orderRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.REFUND)
                .build();
        orderMapper.update(orders1);
    }

    @Override
    public void cancel(OrderCancelDTO orderCancelDTO) {
        Orders orders = orderMapper.getByOrderId(orderCancelDTO.getId());
        //订单不存在或订单状态不是 "已接单"
        if (orders == null || !orders.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Integer payStatus = orders.getPayStatus();
        if (payStatus == 1){
            /*
            退款逻辑
             */
        }
        Orders orders1 = Orders.builder()
                .id(orderCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(orderCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .payStatus(Orders.REFUND)
                .build();

        orderMapper.update(orders1);
    }

    @Override
    public void delivery(Long id) {
        Orders orders = orderMapper.getByOrderId(id);
        //订单不存在或订单状态不是 "已接单"
        if (orders == null || !orders.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders1 = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();

        orderMapper.update(orders1);
    }

    @Override
    public void complete(Long id) {
        Orders orders = orderMapper.getByOrderId(id);
        //订单不存在或订单状态不是 "派送中"
        if (orders == null || !orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders1 = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders1);
    }


    @Override
    public void reminder(Long id) {
        Orders od = orderMapper.getByOrderId(id);

        if (od == null){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Map map = new HashMap<>();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单号:" + od.getNumber());
        webSocketServer.sendToAllClient(JSONUtil.toJsonStr(map));
    }
}
