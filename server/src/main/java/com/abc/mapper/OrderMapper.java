package com.abc.mapper;

import com.abc.dto.GoodsSalesDTO;
import com.abc.dto.OrderPageQueryDTO;
import com.abc.entity.Orders;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders order);

    void update(Orders order);

    @Select("select * from sky_take_out.orders where number = #{orderNumber} and user_id = #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    Page<Orders> pageQuery(OrderPageQueryDTO orderPageQueryDTO);

    @Select("select * from sky_take_out.orders where id=#{id}")
    Orders getDetailsById(Long id);

    @Select("select * from sky_take_out.orders where id = #{id}")
    Orders getByOrderId(Long id);

    @Select("select count(id) from sky_take_out.orders where status = #{status}")
    Integer countStatus(Integer status);

    @Select("select * from sky_take_out.orders where status = #{pendingPayment} and order_time < #{time}")
    List<Orders> getByStatusAndOrderTimeLT(Integer pendingPayment, LocalDateTime time);

    Double sumByMap(Map map);

    Integer countOrderByMap(Map map);

    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
