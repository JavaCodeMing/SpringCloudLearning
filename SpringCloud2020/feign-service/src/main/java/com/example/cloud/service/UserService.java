package com.example.cloud.service;

import com.example.cloud.domain.CommonResult;
import com.example.cloud.domain.User;
import com.example.cloud.service.impl.UserFallbackService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dengzhiming
 * @date 2020/3/11 22:35
 */
@FeignClient(value = "user-service",fallback = UserFallbackService.class)
@Service
public interface UserService {
    @PostMapping("/user/create")
    public CommonResult create(@RequestBody User user);

    @GetMapping("/user/{id}")
    public CommonResult<User> getUser(@PathVariable Long id);

    @GetMapping("/user/getUserByIds")
    public CommonResult<List<User>> getUserByIds(@RequestParam List<Long> ids);

    @GetMapping("/user/getByUsername/{username}")
    public CommonResult<User> getByUsername(@PathVariable String username);

    @PutMapping("/user/update")
    public CommonResult update(@RequestBody User user);

    @DeleteMapping("/user/delete/{id}")
    public CommonResult delete(@PathVariable Long id);
}
