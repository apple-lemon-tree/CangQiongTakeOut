package com.abc.service;

import com.abc.dto.*;
import com.abc.result.PageResult;
import com.abc.vo.OrderPaymentVO;
import com.abc.vo.OrderStatisticsVO;
import com.abc.vo.OrderSubmitVO;
import com.abc.vo.OrderVO;

public interface OrderService {

    OrderSubmitVO submitOrder(OrderSubmitDTO orderSubmitDTO);

    OrderPaymentVO payment(OrderPaymentDTO orderPaymentDTO);

    void paySuccess(String outTradeNo);

    PageResult pageQuery4User(OrderPageQueryDTO orderPageQueryDTO);

    OrderVO getOrderDetails(Long id);

    void userCancelById(Long id);

    void repetitionOrder(Long id);

    PageResult conditionSearch(OrderPageQueryDTO orderPageQueryDTO);

    OrderVO getOrderInfo(Long id);

    OrderStatisticsVO orderstistics();

    void confirm(OrderConfirmDTO orderConfirmDTO);

    void rejection(OrderRejectionDTO orderRejectionDTO);

    void cancel(OrderCancelDTO orderCancelDTO);

    void delivery(Long id);

    void complete(Long id);

    void reminder(Long id);
}
