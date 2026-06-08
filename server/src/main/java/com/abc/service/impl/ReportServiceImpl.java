package com.abc.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.abc.dto.GoodsSalesDTO;
import com.abc.entity.Orders;
import com.abc.mapper.OrderMapper;
import com.abc.mapper.UserMapper;
import com.abc.service.ReportService;
import com.abc.service.WorkspaceService;
import com.abc.vo.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Double> turnoverList = new ArrayList<>();
        while (!begin.equals(end.plusDays(1))){
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        for (LocalDate date: dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date,LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        return new TurnoverReportVO(
                CollUtil.join(dateList,","),
                CollUtil.join(turnoverList,",")
                );
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        while (!begin.equals(end.plusDays(1))){
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        for (LocalDate date: dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date,LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("end",endTime);
            Integer totalUser = userMapper.countByMap(map);
            map.put("begin",beginTime);
            Integer newUser = userMapper.countByMap(map);
            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        return new UserReportVO(
                CollUtil.join(dateList,","),
                CollUtil.join(newUserList,","),
                CollUtil.join(totalUserList,",")
        );
    }

    @Override
    public OrderNumberVO getOrderNumber(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        while (!begin.equals(end.plusDays(1))){
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        Integer totalOrderCount = 0;
        Integer totalvalidCount = 0;
        for (LocalDate date: dateList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date,LocalTime.MAX);
            Integer orderCount = getOrderCount(beginTime,endTime,null);
            Integer validOrderCount = getOrderCount(beginTime,endTime,Orders.COMPLETED);
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);

            totalOrderCount += orderCount;
            totalvalidCount += validOrderCount;
        }

        Double rate = 0.0;
        if (totalOrderCount != 0){
            rate = totalvalidCount.doubleValue() / totalOrderCount;
        }

        return new OrderNumberVO(
                CollUtil.join(dateList,","),
                CollUtil.join(orderCountList,","),
                CollUtil.join(validOrderCountList,","),
                totalOrderCount,
                totalvalidCount,
                rate
        );
    }

    /**
     * 根据条件统计订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status){
        Map map = new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",status);

        return orderMapper.countOrderByMap(map);
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end,LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime,endTime);
        //相当于for循环往names里添加元素
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = CollUtil.join(names,",");
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = CollUtil.join(numbers,",");
        return new SalesTop10ReportVO(
                nameList,numberList
        );
    }

    @Override
    public void exportBussinessData(HttpServletResponse response) {
        //1.查询数据库，获取营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin,LocalTime.MIN),LocalDateTime.of(dateEnd,LocalTime.MAX));

        //2.通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");


        try{
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据-时间
            sheet.getRow(1).getCell(1).setCellValue("时间："+dateBegin+"至"+dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();

        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
