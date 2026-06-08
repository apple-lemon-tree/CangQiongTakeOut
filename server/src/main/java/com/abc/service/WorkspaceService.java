package com.abc.service;

import com.abc.vo.BusinessDataVO;
import com.abc.vo.DishOverViewVO;
import com.abc.vo.OrderOverViewVO;
import com.abc.vo.SetmealOverViewVO;

import java.time.LocalDateTime;

public interface WorkspaceService {
    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);

    OrderOverViewVO getOrderOverView();

    DishOverViewVO getDishOverView();

    SetmealOverViewVO getSetmealOverView();
}
