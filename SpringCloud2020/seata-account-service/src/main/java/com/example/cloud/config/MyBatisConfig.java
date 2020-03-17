package com.example.cloud.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author dengzhiming
 * @date 2020/3/16 0:33
 */
@Configuration
@MapperScan({"com.example.cloud.dao"})
public class MyBatisConfig {
}
