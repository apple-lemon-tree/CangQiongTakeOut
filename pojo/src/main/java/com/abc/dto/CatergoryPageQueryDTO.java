package com.abc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CatergoryPageQueryDTO {
    private int page;
    private int pageSize;
    private String name;
    private Integer type;
}
