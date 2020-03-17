package com.example.cloud.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.example.cloud.domain.CommonResult;
import com.example.cloud.domain.User;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCollapser;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheRemove;
import com.netflix.hystrix.contrib.javanica.cache.annotation.CacheResult;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author dengzhiming
 * @date 2020/3/11 13:56
 */
@Slf4j
@Service
public class UserService {
    @Resource
    private RestTemplate template;
    @Value("${service-url.user-service}")
    private String userServiceUrl;

    @HystrixCommand(fallbackMethod = "getDefaultUser")
    public CommonResult getUser(Long id) {
        return template.getForObject(userServiceUrl + "/user/{1}", CommonResult.class, id);
    }

    public CommonResult getDefaultUser(Long id) {
        User defaultUser = new User(-1L, "defaultUser", "123456");
        return new CommonResult<>(defaultUser);
    }

    @HystrixCommand(
            fallbackMethod = "getDefaultUser",
            commandKey = "getUserCommand",
            groupKey = "getUserGroup",
            threadPoolKey = "getUserThreadPool"
    )
    public CommonResult getUserCommand(Long id){
        return template.getForObject(userServiceUrl+"/user/{1}",CommonResult.class,id);
    }

    @HystrixCommand(fallbackMethod = "getDefaultUser2",ignoreExceptions = {NullPointerException.class})
    public CommonResult getUserException(Long id){
        if(id == 1){
            throw new IndexOutOfBoundsException();
        }else if(id == 2){
            throw new NullPointerException();
        }
        return template.getForObject(userServiceUrl+"/user/{1}",CommonResult.class,id);
    }

    public CommonResult getDefaultUser2(Long id, Throwable e){
        log.error("getDefaultUser2 id:{},throwable class:{}", id, e.getClass());
        User defaultUser = new User(-2L, "defaultUser2", "123456");
        return new CommonResult<>(defaultUser);
    }

    @CacheResult(cacheKeyMethod = "getCacheKey")
    @HystrixCommand(fallbackMethod = "getDefaultUser",commandKey = "getUserCache")
    public CommonResult getUserCache(Long id){
        log.info("getUserCache id:{}", id);
        return template.getForObject(userServiceUrl+"/user/{1}",CommonResult.class,id);
    }

    //为缓存生成key的方法
    private String getCacheKey(Long id){
        return String.valueOf(id);
    }

    @CacheRemove(commandKey = "getUserCache",cacheKeyMethod = "getCacheKey")
    @HystrixCommand
    public CommonResult removeCache(Long id){
        log.info("removeCache id:{}", id);
        return template.getForObject(userServiceUrl+"/user/{1}",CommonResult.class,id);
    }

    @HystrixCollapser(batchMethod = "getUserByIds",collapserProperties = {
            @HystrixProperty(name = "timerDelayInMilliseconds",value = "100")
    })
    public Future<User> getUserFuture(Long id){
        return new  AsyncResult<User>(){
            @Override
            public User invoke() {
                CommonResult commonResult = template.getForObject(userServiceUrl + "/user/{1}", CommonResult.class, id);
                Map data = (Map) commonResult.getData();
                User user = BeanUtil.mapToBean(data,User.class,true);
                log.info("getUserById username:{}", user.getUsername());
                return user;
            }
        };
    }

    @HystrixCommand
    public List<User> getUserByIds(List<Long> ids){
        log.info("getUserByIds:{}", ids);
        CommonResult commonResult = template.getForObject(userServiceUrl + "/user/getUserByIds?ids={1}", CommonResult.class, CollUtil.join(ids, ","));
        assert commonResult != null;
        return (List<User>)commonResult.getData();
    }
}
