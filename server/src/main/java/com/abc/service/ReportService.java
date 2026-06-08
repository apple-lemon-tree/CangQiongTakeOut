package com.abc.service;

import com.abc.vo.OrderNumberVO;
import com.abc.vo.SalesTop10ReportVO;
import com.abc.vo.TurnoverReportVO;
import com.abc.vo.UserReportVO;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;

public interface ReportService {
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    OrderNumberVO getOrderNumber(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);

    void exportBussinessData(HttpServletResponse response);
}
