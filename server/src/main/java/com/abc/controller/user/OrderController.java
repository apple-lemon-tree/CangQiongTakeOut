package com.abc.controller.user;

import com.abc.dto.OrderPageQueryDTO;
import com.abc.dto.OrderPaymentDTO;
import com.abc.dto.OrderSubmitDTO;
import com.abc.result.PageResult;
import com.abc.result.Result;
import com.abc.service.OrderService;
import com.abc.vo.OrderPaymentVO;
import com.abc.vo.OrderSubmitVO;
import com.abc.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController("userOrderController")
@RequestMapping("/user/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param orderSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrderSubmitDTO orderSubmitDTO){
        log.info("用户下单，参数为：{}",orderSubmitDTO);
        OrderSubmitVO  orderSubmitVO = orderService.submitOrder(orderSubmitDTO);
        return Result.success("",orderSubmitVO);
    }

    /**
     * 支付接口
     * @param orderPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    public Result<OrderPaymentVO> payment(@RequestBody OrderPaymentDTO orderPaymentDTO){
        log.info("订单支付：{}",orderPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(orderPaymentDTO);
        log.info("生成预支付交易单：{}",orderPaymentVO);
        orderService.paySuccess(orderPaymentDTO.getOrderNumber());
        return Result.success("支付完成",orderPaymentVO);
    }

    /**
     * 分页查询订单表
     * @param orderPageQueryDTO
     * @return
     */
    @GetMapping("/historyOrders")
    public Result<PageResult> page(OrderPageQueryDTO orderPageQueryDTO){
        PageResult pageResult = orderService.pageQuery4User(orderPageQueryDTO);
        return Result.success("分页查询成功",pageResult);
    }

    /**
     * 根据订单Id查询订单详细信息
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> getOrderDetails(@PathVariable Long id){
        OrderVO orderVO = orderService.getOrderDetails(id);
        return Result.success("查询成功",orderVO);
    }

    /**
     * 取消订单
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    public Result canncelOrder(@PathVariable Long id){
        orderService.userCancelById(id);
        return Result.success("用户撤销订单");
    }

    /**
     * 再来一单
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    public Result repetitionOrder(@PathVariable Long id){
        orderService.repetitionOrder(id);
        return Result.success("用户又来了一单");
    }

}
