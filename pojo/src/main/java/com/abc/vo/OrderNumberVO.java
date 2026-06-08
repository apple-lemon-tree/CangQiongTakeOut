package com.abc.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderNumberVO {
    private String dateList;

    private String orderCountList;
    private String validOrderCountList;

    private Integer totalOrderCount;
    private Integer validOrderCount;

    private Double orderCompletionRate;
}
