package com.abc.controller.admin;

import com.abc.dto.*;
import com.abc.entity.Orders;
import com.abc.result.PageResult;
import com.abc.result.Result;
import com.abc.service.OrderService;
import com.abc.vo.OrderStatisticsVO;
import com.abc.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("adminOrderController")
@RequestMapping("/admin/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 管理端订单查询
     * @param orderPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(OrderPageQueryDTO orderPageQueryDTO){
        log.info("订单信息分页查询");
        PageResult pageResult = orderService.conditionSearch(orderPageQueryDTO);
        return Result.success("分页查询订单信息",pageResult);
    }

    /**
     * 根据订单的id查询订单的相关信息
     * @param id
     * @return
     */
    @GetMapping("/details/{id}")
    public Result<OrderVO> getOrderInfo(@PathVariable Long id){
        log.info("根据订单id查询订单信息：{}",id);
        OrderVO orderVO = orderService.getOrderInfo(id);
        return Result.success("查询成功",orderVO);
    }

    /**
     * 统计待接单、待派送、派送中 订单的数量
     * @return
     */
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> orderStatistics(){
        log.info("统计各个状态的订单数量");
        OrderStatisticsVO orderStatisticsVO = orderService.orderstistics();
        return Result.success("统计成功",orderStatisticsVO);
    }

    /**
     * 店家接单
     * @param orderConfirmDTO
     * @return
     */
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrderConfirmDTO orderConfirmDTO){
        log.info("店家接单：{}",orderConfirmDTO.getId());
        orderService.confirm(orderConfirmDTO);
        return Result.success("接单成功");
    }

    /**
     * 店家拒单
     * @param orderRejectionDTO
     * @return
     */
    @PutMapping("/rejection")
    public Result rejection(@RequestBody OrderRejectionDTO orderRejectionDTO){
        log.info("店家拒单：{}",orderRejectionDTO.getId());
        orderService.rejection(orderRejectionDTO);
        return Result.success("拒单成功");
    }

    /**
     * 店家取消订单
     * @param orderCancelDTO
     * @return
     */
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrderCancelDTO orderCancelDTO){
        log.info("店家撤销订单");
        orderService.cancel(orderCancelDTO);
        return Result.success("取消订单成功");
    }

    /**
     * 店家接单后派送
     * @param id
     * @return
     */
    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable Long id){
        orderService.delivery(id);
        return Result.success("派送订单成功");
    }

    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id){
        log.info("完成订单");
        orderService.complete(id);
        return Result.success("订单已完成");
    }


}
