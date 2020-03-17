package com.example.cloud.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dengzhiming
 * @date 2020/3/13 22:39
 */
@RestController
public class FallbackController {

    @GetMapping("/fallback")
    public Object fallback(){
        Map<String,Object> result = new HashMap<>();
        result.put("code",500);
        result.put("message","Get request fallback!");
        result.put("data",null);
        return result;
    }
}
