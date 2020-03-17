package com.example.cloud.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dengzhiming
 * @date 2020/3/12 0:42
 */
@RefreshScope
@RestController
public class ConfigClientController {
    @Value("${message}")
    private String message;

    @GetMapping("/message")
    public String getConfigInfo() {
        return message;
    }
}
