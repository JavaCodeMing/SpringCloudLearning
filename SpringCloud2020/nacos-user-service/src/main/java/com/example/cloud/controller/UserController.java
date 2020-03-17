package com.example.cloud.controller;

import com.example.cloud.domain.CommonResult;
import com.example.cloud.domain.User;
import com.example.cloud.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dengzhiming
 * @date 2020/3/10 23:34
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Value("${server.port}")
    private String port;

    @PostMapping("/create")
    public CommonResult create(@RequestBody User user) {
        userService.create(user);
        log.info("调用的服务端口为: " + port);
        return new CommonResult("操作成功", 200);
    }

    @GetMapping("{id}")
    public CommonResult<User> getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        log.info("调用的服务端口为: " + port);
        return new CommonResult<>(user);
    }

    @GetMapping("/getUserByIds")
    public CommonResult<List<User>> getUserByIds(@RequestParam List<Long> ids) {
        List<User> users = userService.getUserByIds(ids);
        log.info("调用的服务端口为: " + port);
        return new CommonResult<>(users);
    }

    @GetMapping("getByUsername/{username}")
    public CommonResult<User> getByUsername(@PathVariable String username) {
        User user = userService.getByUsername(username);
        log.info("调用的服务端口为: " + port);
        return new CommonResult<>(user);
    }

    @PutMapping("/update")
    public CommonResult update(@RequestBody User user) {
        userService.update(user);
        log.info("调用的服务端口为: " + port);
        return new CommonResult("操作成功", 200);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResult delete(@PathVariable Long id) {
        userService.delete(id);
        log.info("调用的服务端口为: " + port);
        return new CommonResult("操作成功", 200);
    }
}
