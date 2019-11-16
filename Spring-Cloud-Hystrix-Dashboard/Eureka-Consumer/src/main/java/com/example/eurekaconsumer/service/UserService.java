package com.example.eurekaconsumer.service;

import com.example.eurekaconsumer.domain.User;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCollapser;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheResult;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by dengzhiming on 2019/10/17
 */
@Service("userService")
public class UserService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private RestTemplate template;

    //该注解的fallbackMethod属性指定了被调用的方法不可用时的回调方法(即服务降级)
    @CacheResult
    @HystrixCommand(fallbackMethod = "getUserDefault")
    public User getUser(@PathVariable Long id) {
        return this.template.getForObject("http://Eureka-Client/user/{id}", User.class, id);
    }

    public User getUserDefault(Long id) {
        User user = new User();
        user.setId(-1L);
        user.setUsername("defaultUser");
        user.setPassword("123456");
        return user;
    }

    @HystrixCollapser(batchMethod = "findUserBatch", collapserProperties = {
            @HystrixProperty(name = "timerDelayInMilliseconds", value = "100")
    })
    public Future<User> findUser(Long id) {
        log.info("获取单个用户信息");
        return new AsyncResult<User>() {
            @Override
            public User invoke() {
                return template.getForObject("http://Eureka-Client/user/{id}", User.class, id);
            }
        };
    }

    @HystrixCommand
    public List<User> findUserBatch(List<Long> ids) {
        log.info("批量获取用户信息,ids: " + ids);
        User[] users = this.template.getForObject("http://Eureka-Client/user/users?ids={1}", User[].class, StringUtils.join(ids, ","));
        return Arrays.asList(users != null ? users : new User[0]);
    }

    public List<User> getUsers() {
        return this.template.getForObject("http://Eureka-Client/user", List.class);
    }

    public String addUser() {
        User user = new User(1L, "kimi", "123456");
        HttpStatus statusCode = this.template
                .postForEntity("http://Eureka-Client/user", user, null)
                .getStatusCode();
        if (statusCode.is2xxSuccessful()) {
            return "新增用户成功";
        } else {
            return "新增用户失败";
        }
    }

    public void updateUser() {
        User user = new User(1L, "mrbird", "123456");
        this.template.put("http://Eureka-Client/user", user);
    }

    public void deleteUser(@PathVariable Long id) {
        this.template.delete("http://Eureka-Client/user/{1}", id);
    }


}
