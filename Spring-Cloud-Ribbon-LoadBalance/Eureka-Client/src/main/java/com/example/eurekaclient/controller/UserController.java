package com.example.eurekaclient.controller;

import com.example.eurekaclient.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dengzhiming on 2019/10/16
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/{id:\\d+}")
    public User getUser(@PathVariable Long id){
        logger.info("获取用户id为 " + id + "的信息");
        return new User(id, "miki", "123456");
    }

    @GetMapping
    public List<User> get() {
        List<User> list = new ArrayList<>();
        list.add(new User(1L, "miki", "123456"));
        list.add(new User(2L, "scott", "123456"));
        logger.info("获取用户信息 " + list);
        return list;
    }

    @PostMapping
    public void add(@RequestBody User user) {
        logger.info("新增用户成功 " + user);
    }

    @PutMapping
    public void update(@RequestBody User user) {
        logger.info("更新用户成功 " + user);
    }

    @DeleteMapping("/{id:\\d+}")
    public void delete(@PathVariable Long id) {
        logger.info("删除用户成功 " + id);
    }

}
