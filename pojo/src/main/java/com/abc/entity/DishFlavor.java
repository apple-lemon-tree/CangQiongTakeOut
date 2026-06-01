package com.abc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishFlavor {
    private Long id;
    private Long dishId;//逻辑外键
    private String name;
    private String value;
}
