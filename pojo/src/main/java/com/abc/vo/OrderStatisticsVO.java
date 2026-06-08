package com.abc.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatisticsVO {
    Integer confirmed; //待派送数量
    Integer deliveryInProgress; //派送中数量
    Integer toBeConfirmed; //待接单数量
}
