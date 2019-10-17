package com.example.eurekaconsumer.controller;

import com.example.eurekaconsumer.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by dengzhiming on 2019/10/16
 */
@RestController
public class TestController {

    @Autowired
    private RestTemplate template;

    @GetMapping("user/{id:\\d+}")
    public User getUser(@PathVariable Long id){
        Map<String,Object> params = new HashMap<>();
        params.put("id",id);
        URI uri = UriComponentsBuilder.fromUriString("http://Eureka-Client/user/{id}")
                .build().expand(params)
                .encode().toUri();
        return this.template.getForEntity(uri,User.class).getBody();
    }

    @GetMapping("user")
    public List<User> getUsers() {
        return this.template.getForObject("http://Eureka-Client/user", List.class);
    }

    @GetMapping("user/add")
    public String addUser(){
        User user = new User(1L, "kimi", "123456");
        HttpStatus statusCode = this.template
                .postForEntity("http://Eureka-Client/user", user, null)
                .getStatusCode();
        if(statusCode.is2xxSuccessful()){
            return "新增用户成功";
        }else {
            return "新增用户失败";
        }
    }

    @GetMapping("user/update")
    public void updateUser() {
        User user = new User(1L, "mrbird", "123456");
        this.template.put("http://Eureka-Client/user", user);
    }

    @GetMapping("user/delete/{id:\\d+}")
    public void deleteUser(@PathVariable Long id) {
        this.template.delete("http://Eureka-Client/user/{1}", id);
    }

}
