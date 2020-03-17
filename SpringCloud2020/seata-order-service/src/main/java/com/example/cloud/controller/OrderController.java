package com.example.cloud.controller;

import com.example.cloud.domain.CommonResult;
import com.example.cloud.domain.Order;
import com.example.cloud.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dengzhiming
 * @date 2020/3/16 16:12
 */
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @GetMapping("/create")
    public CommonResult create(Order order){
        orderService.create(order);
        return new CommonResult("订单创建成功!", 200);
    }
}
