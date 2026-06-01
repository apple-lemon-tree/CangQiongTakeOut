package com.abc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishQueryDTO {
    private int page;
    private int pageSize;
    private Integer categoryId;
    private String name;
    private Integer status;
}
