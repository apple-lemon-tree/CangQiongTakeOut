package com.abc.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishItemVO {
    private String name;
    private Integer copies;
    private String image;
    private String description;
}
