package com.abc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class OrderPaymentDTO {
    private String orderNumber;
    private Integer payMethod;
}
