# 搭建Feign Consumer
```text
1.Spring Cloud Feign整合了Spring Cloud Ribbon和Spring Cloud Hystrix的功能,而且还提供了一种
    比Ribbon更简单的服务调用方式(声明式服务调用);
2.创建一个Spring Boot(2.1.9)项目,groupId填com.example,artifactId填feign-consumer,然后引入如下依赖:
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
3.在Spring Boot的入口类中加入@EnableFeignClients和@EnableDiscoveryClient注解,
    用于开启Spring Cloud Feign和服务注册与发现:
    @EnableFeignClients
    @EnableDiscoveryClient
    @SpringBootApplication
    public class FeignConsumerApplication {
        public static void main(String[] args) {
            SpringApplication.run(FeignConsumerApplication.class, args);
        }
    }
4.Spring Cloud Ribbon中访问这些服务需要通过RestTemplate对象来实现,Spring Cloud Feign对这个步骤进行了
    进一步的封装,在Feign Consumer中调用这些服务只需要定义一个UserService接口:
    @FeignClient(value = "Eureka-Client")
    public interface UserService {
        @GetMapping("user/{id}")
        public User get(@PathVariable("id") Long id);
        @GetMapping("user")
        public List<User> get();
        @PostMapping("user")
        public void add(@RequestBody User user);
        @PutMapping("user")
        public void update(@RequestBody User user);
        @DeleteMapping("user/{id}")
        public void delete(@PathVariable("id") Long id);
    }
5.在Feign Consumer中定义一个TestController,来调用UserService中定义的服务:
    @RestController
    public class TestController {
        @Autowired
        private UserService userService;
        @GetMapping("user/{id}")
        public User getUser(@PathVariable Long id) {
            return userService.get(id);
        }
        @GetMapping("user")
        public List<User> getUsers() {
            return userService.get();
        }
        @PostMapping("user")
        public void addUser() {
            User user = new User(1L, "kimi", "123456");
            userService.add(user);
        }
        @PutMapping("user")
        public void updateUser() {
            User user = new User(1L, "kimi", "123456");
            userService.update(user);
        }
        @DeleteMapping("user/{id}")
        public void deleteUser(@PathVariable Long id) {
            userService.delete(id);
        }
    }
6.配置application.yml:
    server:
        port: 9000
    spring:
        application:
            name: Server-Consumer
    eureka:
        client:
            service-url:
                defaultZone: http://kimi:123456@peer1:8080/eureka/,http://kimi:123456@peer2:8081/eureka/
7.拷贝Eureka-Server和Eureka-Client过来,执行以下步骤:
    [1]分别启动以下服务:
        (1)启动Eureka-Server集群,端口号为8080和8081;
        (2)启动两个Eureka-Client,端口号为8082和8083;
        (3)启动Feign-Consumer,端口号为9000;
    [2]多次访问http://localhost:9000/user/1服务,观察8082和8083服务的控制台,通过轮询实现了客户端负载均衡;
```
# Ribbon相关配置: 
```text
Spring Cloud Feign内部的客户端负载均衡是通过Ribbon来实现的,所以在配置Ribbon和Spring Cloud Ribbon配置一样;
```
# Hystrix相关配置
```text
1.要在Spring Cloud [1]Feign中开启Hystrix,可以在yml中添加如下配置:
    feign:
        hystrix:
            enabled: true
[2]剩下的Hystrix配置和之前在Spring Cloud Hystrix服务容错中介绍的Hystrix属性配置一样;
[3]在Feign-Consumer中配置服务降级:
    (1)定义一个用于处理服务降级方法的类UserServiceFallback,并且实现上面定义的UserService接口:
        @Component
        public class UserServiceFallback implements UserService {
            private Logger log = LoggerFactory.getLogger(this.getClass());
            @Override
            public User get(Long id) {
                return new User(-1L, "default", "123456");
            }
            @Override
            public List<User> get() { return null; }
            @Override
            public void add(User user) {
                log.info("test fallback");
            }
            @Override
            public void update(User user) {
                log.info("test fallback");
            }
            @Override
            public void delete(Long id) {
                log.info("test fallback");
            }
        }
    (2)在UserService的中通过@FeignClient注解的fallback属性来指定对应的服务降级实现类:
        @FeignClient(value = "Server-Provider", fallback = UserServiceFallback.class)
        public interface UserService {
            ...
        }
    (3)重启Feign-Consumer，并关闭Eureka Client服务,访问http://localhost:9000/user/1,
        由于Eureka-Client服务提供者都关闭了,所以这里会直接触发服务降级,查看降级后的返回结果;
```
# 其余Feign配置
```text
1.请求压缩:
    [1]Spring Cloud Feign支持对请求与响应进行GZIP压缩,以减少通信过程中的性能损耗:
        feign:
            compression:
                request:
                    enabled: true
                response:
                    enabled: true
    [2]对请求压缩做一些更细致的设置:
    (指定了压缩的请求数据类型,并设置了请求压缩的大小下限)
        feign:
            compression:
                request:
                    enabled: true
                    mime-types: text/xml,application/xml,application/json
                    min-request-size: 2048
2.日志配置:
    [1]Feign提供了日志打印的功能,Feign的日志级别分为四种:
        (1)NONE: 不记录任何信息;
        (2)BASIC: 仅记录请求方法、URL以及响应状态码和执行时间;
        (3)HEADERS: 除了记录BASIC级别的信息之外,还会记录请求和响应的头信息;
        (4)FULL: 记录所有请求与响应的明细,包括头信息、请求体、元数据等;
    [2]日志级别默认为NONE,要改变级别可以在入口类中定义一个日志配置Bean,并在配置文件中进行配置:
        (1)在入口类中定义一个日志配置Bean:
            @EnableFeignClients
            @EnableDiscoveryClient
            @SpringBootApplication
            public class FeignConsumerApplication {
               @Bean
               Logger.Level feignLoggerLevel() {
                   return Logger.Level.FULL;
               }
               public static void main(String[] args) {
                    SpringApplication.run(FeignConsumerApplication.class, args);
                }
            }
        (2)在yml文件中配置日志级别:
            logging:
                level:
                    com:
                        example:
                            feignconsumer:
                                service:
                                    UserService: debug
```
