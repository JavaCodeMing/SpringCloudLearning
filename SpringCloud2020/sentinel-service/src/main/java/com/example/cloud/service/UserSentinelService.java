package com.example.cloud.service;

import com.example.cloud.domain.CommonResult;
import com.example.cloud.domain.User;
import com.example.cloud.service.impl.UserFallbackSentinelService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author dengzhiming
 * @date 2020/3/15 20:11
 */
@FeignClient(value = "nacos-user-service", fallback = UserFallbackSentinelService.class)
public interface UserSentinelService {
    @PostMapping("/user/create")
    CommonResult create(@RequestBody User user);
    @GetMapping("/user/{id}")
    CommonResult getUser(@PathVariable Long id);
    @GetMapping("/user/getByUsername/{username}")
    CommonResult getByUsername(@PathVariable String username);
    @PostMapping("/user/update")
    CommonResult update(@RequestBody User user);
    @PostMapping("/user/delete/{id}")
    CommonResult delete(@PathVariable Long id);
}
