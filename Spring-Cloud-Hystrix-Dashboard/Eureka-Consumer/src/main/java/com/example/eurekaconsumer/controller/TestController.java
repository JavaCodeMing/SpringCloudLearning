package com.example.eurekaconsumer.controller;

import com.example.eurekaconsumer.domain.User;
import com.example.eurekaconsumer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * Created by dengzhiming on 2019/10/16
 */
@RestController
public class TestController {

    @Autowired
    RestTemplate template;

    @Autowired
    UserService userService;

    @GetMapping("user/{id:\\d+}")
    public User getUser(@PathVariable Long id) {
        return this.userService.getUser(id);
    }

    @GetMapping("user")
    public List<User> getUsers() {
        return this.userService.getUsers();
    }

    @GetMapping("user/add")
    public String addUser() {
        return this.userService.addUser();
    }

    @GetMapping("user/update")
    public void updateUser() {
        this.userService.updateUser();
    }

    @GetMapping("user/delete/{id:\\d+}")
    public void deleteUser(@PathVariable Long id) {
        this.userService.deleteUser(id);
    }

    @GetMapping("testRequestMerge")
    public void testRequerstMerge() throws InterruptedException, ExecutionException {
        Future<User> f1 = this.userService.findUser(1L);
        Future<User> f2 = this.userService.findUser(2L);
        Future<User> f3 = this.userService.findUser(3L);
        f1.get();
        f2.get();
        f3.get();
        Thread.sleep(200);
        Future<User> f4 = this.userService.findUser(4L);
        f4.get();
    }
}
