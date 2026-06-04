package com.abc.dto;

import lombok.Data;

@Data
public class OrderCancelDTO {
    private Long id;
    private String cancelReason;
}
