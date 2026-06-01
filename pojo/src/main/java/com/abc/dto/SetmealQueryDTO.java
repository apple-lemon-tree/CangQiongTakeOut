package com.abc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetmealQueryDTO {
    private int page;
    private int pageSize;
    private Integer status;
    private String name;
    private Long categoryId;
}
