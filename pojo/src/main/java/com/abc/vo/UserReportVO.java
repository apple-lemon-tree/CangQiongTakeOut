package com.abc.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReportVO {
    private String dateList;
    private String newUserList;
    private String totalUserList;
}
