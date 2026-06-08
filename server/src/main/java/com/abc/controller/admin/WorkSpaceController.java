package com.abc.controller.admin;


import com.abc.result.Result;
import com.abc.service.WorkspaceService;
import com.abc.vo.BusinessDataVO;
import com.abc.vo.DishOverViewVO;
import com.abc.vo.OrderOverViewVO;
import com.abc.vo.SetmealOverViewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/workspace")
public class WorkSpaceController {
    private final WorkspaceService workspaceService;

    /**
     * 工作台每日数据查询
     * @return
     */
    @GetMapping("/businessData")
    public Result<BusinessDataVO> businessData(){
        log.info("工作台今日数据查询");
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin,end);
        return Result.success("工作台",businessDataVO);
    }

    /**
     * 查询订单管理数据
     * @return
     */
    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> orderOverView(){
        return Result.success("查询订单管理数据成功",workspaceService.getOrderOverView());
    }

    /**
     * 查询菜品总览
     * @return
     */
    @GetMapping("/overviewDishes")
    public Result<DishOverViewVO> dishOverView(){
        return Result.success("查询菜品管理数据成功",workspaceService.getDishOverView());
    }

    /**
     * 查询套餐总览
     * @return
     */
    @GetMapping("/overviewSetmeals")
    public Result<SetmealOverViewVO> setmealOverView(){
        return Result.success("查询套餐管理数据成功",workspaceService.getSetmealOverView());
    }
}
