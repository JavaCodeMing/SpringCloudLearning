package com.example.cloud.controller;

import com.example.cloud.domain.CommonResult;
import com.example.cloud.domain.User;
import com.example.cloud.service.UserSentinelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author dengzhiming
 * @date 2020/3/15 20:24
 */
@RestController
@RequestMapping("/user")
public class UserFeignSentinelController {
    @Resource
    private UserSentinelService userSentinelService;

    @GetMapping("/{id}")
    public CommonResult getUser(@PathVariable Long id) {
        return userSentinelService.getUser(id);
    }

    @GetMapping("/getByUsername/{username}")
    public CommonResult getByUsername(@PathVariable String username) {
        return userSentinelService.getByUsername(username);
    }

    @PostMapping("/create")
    public CommonResult create(@RequestBody User user) {
        return userSentinelService.create(user);
    }

    @PostMapping("/update")
    public CommonResult update(@RequestBody User user) {
        return userSentinelService.update(user);
    }

    @PostMapping("/delete/{id}")
    public CommonResult delete(@PathVariable Long id) {
        return userSentinelService.delete(id);
    }
}
