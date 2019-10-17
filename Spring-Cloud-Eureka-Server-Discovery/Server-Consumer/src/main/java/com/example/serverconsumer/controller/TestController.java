package com.example.serverconsumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Created by dengzhiming on 2019/10/15
 */
@RestController
public class TestController {
    @Autowired
    private RestTemplate template;

    @GetMapping("/info")
    public String getInfo(){
        return this.template.getForEntity("http://Eureka-Client/info", String.class).getBody();
    }
}
