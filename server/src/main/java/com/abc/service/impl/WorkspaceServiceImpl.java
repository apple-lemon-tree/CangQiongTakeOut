package com.abc.service.impl;

import com.abc.constant.StatusConstant;
import com.abc.entity.Orders;
import com.abc.mapper.DishMapper;
import com.abc.mapper.OrderMapper;
import com.abc.mapper.SetmealMapper;
import com.abc.mapper.UserMapper;
import com.abc.service.WorkspaceService;
import com.abc.vo.BusinessDataVO;
import com.abc.vo.DishOverViewVO;
import com.abc.vo.OrderOverViewVO;
import com.abc.vo.SetmealOverViewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    @Override
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        /**
         * 营业额：当日已完成订单的总金额
         * 有效订单：当日已完成订单的数量
         * 订单完成率：有效订单数 / 总订单数
         * 平均客单价：营业额 / 有效订单数
         * 新增用户：当日新增用户的数量
         */

        Map map = new HashMap<>();
        map.put("begin",begin);
        map.put("end",end);

        //查询订单总数
        Integer totalOrderCount = orderMapper.countOrderByMap(map);


        map.put("status", Orders.COMPLETED);
        //营业额
        Double turnover = orderMapper.sumByMap(map);
        turnover = turnover == null ? 0.0 : turnover;

        //有效订单数
        Integer validOrderCount = orderMapper.countOrderByMap(map);

        Double unitPrice = 0.0;

        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0 && validOrderCount != 0){
            //订单完成率
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            //平均客单价
            unitPrice = turnover / validOrderCount;
        }
        //新增用户数
        Integer newUsers = userMapper.countByMap(map);

        return new BusinessDataVO(
            turnover,validOrderCount,orderCompletionRate,unitPrice,newUsers
        );
    }

    @Override
    public OrderOverViewVO getOrderOverView() {
        Map map = new HashMap<>();
        map.put("begin",LocalDateTime.now().with(LocalDateTime.MIN));
        map.put("end", LocalDateTime.now());
        //待接单
        map.put("status",Orders.TO_BE_CONFIRMED);
        Integer waittingOrders = orderMapper.countOrderByMap(map);
        //待派送
        map.put("status",Orders.CONFIRMED);
        Integer deliveredOrders = orderMapper.countOrderByMap(map);
        //已完成
        map.put("status",Orders.COMPLETED);
        Integer completedOrders = orderMapper.countOrderByMap(map);
        //已取消
        map.put("status",Orders.CANCELLED);
        Integer cancelledOrders = orderMapper.countOrderByMap(map);
        //全部订单
        map.put("status",null);
        Integer allOrders = orderMapper.countOrderByMap(map);

        return new OrderOverViewVO(
                waittingOrders,deliveredOrders,completedOrders,cancelledOrders,allOrders
        );
    }

    @Override
    public DishOverViewVO getDishOverView() {
        Map map = new HashMap<>();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = dishMapper.countByMap(map);

        map.put("status",StatusConstant.DISABLE);
        Integer discontinued = dishMapper.countByMap(map);

        return new DishOverViewVO(sold,discontinued);
    }

    @Override
    public SetmealOverViewVO getSetmealOverView() {

        Map map = new HashMap();
        map.put("status",StatusConstant.ENABLE);
        Integer sold = setmealMapper.countByMap(map);

        map.put("statis",StatusConstant.DISABLE);
        Integer discontinued = setmealMapper.countByMap(map);

        return new SetmealOverViewVO(sold,discontinued);
    }


}
