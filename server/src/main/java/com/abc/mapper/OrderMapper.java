package com.abc.mapper;

import com.abc.dto.OrderPageQueryDTO;
import com.abc.entity.Orders;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}
