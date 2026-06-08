package com.abc.task;

import com.abc.entity.Orders;
import com.abc.mapper.OrderMapper;
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
    /**
     * 处理超时订单方法
     */
    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeOutOrder(){
        log.info("定时处理超时订单：{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> orderList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT,time);
        if (orderList != null && !orderList.isEmpty()){
            for (Orders orders: orderList){
                orders = Orders.builder()
                        .status(Orders.CANCELLED)
                        .cancelReason("订单超时，自动取消")
                        .cancelTime(LocalDateTime.now())
                        .build();
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 每条凌晨1点处理处于派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("定时处理处于派送中的订单：{}",LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> orderList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS,time);

        if (orderList != null && !orderList.isEmpty()){
            for (Orders orders: orderList){
                orders = Orders.builder()
                        .status(Orders.COMPLETED)
                        .build();
                orderMapper.update(orders);
            }
        }
    }
}
