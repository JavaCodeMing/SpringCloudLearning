package com.example.cloud.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dengzhiming
 * @date 2020/3/15 14:46
 */
@RestController
public class TestController {
    @GetMapping("/test")
    public String test(){
        return "测试成功";
    }
}
