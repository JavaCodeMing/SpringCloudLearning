package com.example.eurekaclient.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private DiscoveryClient client;

    @GetMapping("/info")
    public String info(){
        ServiceInstance serviceInstance = client.getInstances("Eureka-Client").get(0);
        String info = "host: "+serviceInstance.getHost()+", service_id: "+serviceInstance.getServiceId();
        logger.info(info);
        return info;
    }

    @GetMapping("/hello")
    public String hello(){
        return "hello world";
    }
}