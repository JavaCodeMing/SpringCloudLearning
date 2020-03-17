package com.example.cloud.service.impl;

import com.example.cloud.domain.CommonResult;
import com.example.cloud.domain.User;
import com.example.cloud.service.UserSentinelService;
import org.springframework.stereotype.Component;

/**
 * @author dengzhiming
 * @date 2020/3/15 20:12
 */
@Component
public class UserFallbackSentinelService implements UserSentinelService {
    @Override
    public CommonResult create(User user) {
        User defaultUser = new User(-1L, "defaultUser", "123456");
        return new CommonResult<>(defaultUser, "服务降级返回", 200);
    }

    @Override
    public CommonResult<User> getUser(Long id) {
        User defaultUser = new User(-1L, "defaultUser", "123456");
        return new CommonResult<>(defaultUser, "服务降级返回", 200);
    }

    @Override
    public CommonResult<User> getByUsername(String username) {
        User defaultUser = new User(-1L, "defaultUser", "123456");
        return new CommonResult<>(defaultUser, "服务降级返回", 200);
    }

    @Override
    public CommonResult update(User user) {
        return new CommonResult("调用失败，服务被降级", 500);
    }

    @Override
    public CommonResult delete(Long id) {
        return new CommonResult("调用失败，服务被降级", 500);
    }
}
