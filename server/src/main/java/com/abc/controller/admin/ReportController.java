package com.abc.controller.admin;

import com.abc.result.Result;
import com.abc.service.ReportService;
import com.abc.vo.OrderNumberVO;
import com.abc.vo.SalesTop10ReportVO;
import com.abc.vo.TurnoverReportVO;
import com.abc.vo.UserReportVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController("adminReportController")
@RequestMapping("/admin/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;


    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end)
    {
        log.info("营业额统计：{}，{}",begin,end);
        TurnoverReportVO vo= reportService.getTurnoverStatistics(begin, end);
        return Result.success("查询统计信息成功",vo);
    }

    /**
     * 统计新增用户
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    )
    {
        log.info("新增用户数：{}，{}",begin,end);
        UserReportVO vo= reportService.getUserStatistics(begin, end);
        return Result.success("查询统计信息成功",vo);
    }

    /**
     * 统计（有效）订单数据
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/ordersStatistics")
    public Result<OrderNumberVO> orderNumberStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    )
    {
        log.info("查询有效订单数:{},{}",begin,end);
        OrderNumberVO vo = reportService.getOrderNumber(begin,end);
        return Result.success("查询有效订单成功",vo);
    }

    /**
     * 统计top10热销餐品
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    )
    {
        log.info("销量排名top10：{}，{}",begin,end);
        SalesTop10ReportVO vo = reportService.getSalesTop10(begin,end);
        return Result.success("销量Top10",vo);
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response){
        reportService.exportBussinessData(response);
    }



}
