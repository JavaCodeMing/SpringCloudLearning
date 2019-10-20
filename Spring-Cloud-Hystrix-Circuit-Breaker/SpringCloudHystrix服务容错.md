# 背景
```text
1.在微服务架构中,服务间通常会形成相互依赖关系,如有三个微服务节点(A,B和C,): B为A的消费者,C为B的消费者;
    假如由于网络波动或者A服务自身故障,导致B调用A服务的线程被挂起进入长时间的等待;
2.在高并发的情况下,调用线程长时间被挂起等待,可能导致B的资源被耗竭随之崩溃,从而导致C服务也不可用;
    这种连环式的雪崩效应在微服务中较为常见,为了解决这个问题,服务熔断技术应运而出;
3.微服务架构中的断路器能够及时地发现故障服务,并向服务调用方返回错误响应,而不是长时间的等待;
4.Spring Cloud Hystrix在Hystrix的基础上进行了封装,提供了服务熔断,服务降级,线程隔离等功能来提高服务的容错率;
```
# Hystrix的引入使用
```text
1.配置Hystrix之前,关闭Eureka-Client的效果:
    [1]分别使用peer1和peer2配置启动Eureka-Server集群,然后启动两个Eureka-Client实例,
        端口分别为8082和8083,最后启动Ribbon-Consumer;
    [2]准备完毕后,关闭端口为8082的Eureka-Client,然后多次发送GET请求http://localhost:9000/user/1,查看响应结果:
        在多次访问中,因负载均衡,当请求分发到端口为8082的Eureka-Client时,会出现500错误;
2.引入Spring Cloud Hystrix依赖到Consumer中:
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
    </dependency>
3.在入口类上加入@EnableHystrix或者@EnableCircuitBreaker注解:(这两个注解是等价的)
    @EnableHystrix
    @EnableDiscoveryClient
    @SpringBootApplication
    public class EurekaConsumerApplication {
        @Bean
        @LoadBalanced
        RestTemplate restTemplate(){
            return new RestTemplate();
        }
        public static void main(String[] args) {
            SpringApplication.run(EurekaConsumerApplication.class, args);
        }
    }
    (@SpringCloudApplication(等价于): @EnableCircuitBreaker、@EnableDiscoveryClient和@SpringBootApplication)
4.将TestController中的方法提取出来,创建一个UserService: (为简单起见,不创建接口)
    @Service("userService")
    public class UserService {
        private Logger log = LoggerFactory.getLogger(this.getClass());
        @Autowired
        private RestTemplate template;
        public User getUser(@PathVariable Long id) {
            return this.template.getForObject("http://Eureka-Provider/user/{id}", User.class, id);
        }
        public List<User> getUsers() {
            return this.template.getForObject("http://Eureka-Provider/user", List.class);
        }
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
        public void updateUser() {
            User user = new User(1L, "mrbird", "123456");
            this.template.put("http://Eureka-Client/user", user);
        }
        public void deleteUser(@PathVariable Long id) {
            this.template.delete("http://Eureka-Client/user/{1}", id);
        }
    }
5.改造UserService的getUser方法:
    //该注解的fallbackMethod属性指定了被调用的方法不可用时的回调方法(即服务降级)
    @HystrixCommand(fallbackMethod = "getUserDefault")
    public User getUser(@PathVariable Long id) {
        return this.template.getForObject("http://Eureka-Provider/user/{id}", User.class, id);
    }
    public User getUserDefault(Long id) {
        User user = new User();
        user.setId(-1L);
        user.setUsername("defaultUser");
        user.setPassword("123456");
        return user;
    }
6.重写TestController:
    @RestController
    public class TestController {
        @Autowired
        RestTemplate template;
        @Autowired
        UserService userService;
        @GetMapping("user/{id:\\d+}")
        public User getUser(@PathVariable Long id){
            return this.userService.getUser(id);
        }
        @GetMapping("user")
        public List<User> getUsers() {
            return this.userService.getUsers();
        }
        @GetMapping("user/add")
        public String addUser(){
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
    }
7.测试服务降级:
    [1]重新启动8082端口的Eureka-Client,发送数次GET请求http://localhost:9000/user/1后,
        再次关闭8082端口的Eureka-Client;
    [2]断开后,继续发送GET请求http://localhost:9000/user/1,
        当请求分发到8082端口时返回回调方法的数据;(也可通过服务响应超时模拟)
```
# @HystrixCommand注解详解
```text
@HystrixCommand注解还包含许多别的属性功能,下面介绍一些常用的属性配置:
1.服务降级:
    通过@HystrixCommand注解中的fallbackMethod属性,指定当服务不能正常响应时的调用方法名;
    (服务降级时调用的方法需和正常调用方法的参数及返回值类型一致)
2.异常处理:
    [1]使用@HystrixCommand注解标注的方法中,除了HystrixBadRequestException异常外,别的异常都会触发服务降级;
    [2]通过@HystrixCommand注解的ignoreExceptions属性,指定不发生服务降级的异常;(即当发生此种异常时会向外抛出)
        @HystrixCommand(fallbackMethod="getUserDefault2",ignoreExceptions={NullPointerException.class})
    [3]当方法异常触发服务降级时,可通过回调方法的参数Throwable对象获取异常信息;
        @HystrixCommand(fallbackMethod = "getUserDefault2")
        public User getUserDefault(Long id, Throwable e) {
            System.out.println(e.getMessage());
            User user = new User();
            user.setId(-2L);
            user.setUsername("defaultUser2");
            user.setPassword("123456");
            return user;
        }
3.命名与分组:
    [1]指定@HystrixCommand注解的commandKey、groupKey以及threadPoolKey属性可设置命令名称、分组以及线程池划分:
        @HystrixCommand(fallbackMethod ="getUserDefault",commandKey="getUserById",groupKey="userGroup",
            threadPoolKey = "getUserThread")
        public User getUser(@PathVariable Long id) {
	        log.info("获取用户信息");
            return restTemplate.getForObject("http://Eureka-Client/user/{id}", User.class, id);
        }
        (以上配置指定了命令的名称为getUserById,组名为userGroup,线程池名称为getUserThread)
    [2]通过设置命令组,Hystrix会根据组来组织和统计命令的告警、仪表盘等信息;
    [3]默认情况下,Hystrix命令通过组名来划分线程池,即组名相同的命令放到同一个线程池里,如果通过threadPoolKey
        设置了线程池名称,则按照线程池名称划分;
        当getUser方法被调用时,日志打印: (可看到线程名称为getUserThread-1)
            2019-10-17 15:58:50.734  INFO 16192 --- [getUserThread-1] com.example.demo.Service.UserService:获取用户信息
4.Hystrix缓存:
    [1]开启缓存: 
        (1)在需要启用缓存的方法上添加@CacheResult注解即可;
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
            (Hystrix会将返回的User对象进行缓存,缓存的key默认为方法的所有参数)
       (2)在测试的时候遇到一个异常:
            java.lang.IllegalStateException: Request caching is not available. 
            Maybe you need to initialize the HystrixRequestContext? ......
            1)在Hystrix的issue中找到了类似的提问: https://github.com/Netflix/Hystrix/issues/1314
                大致意思是在使用Hytrix缓存之前,需要通过HystrixRequestContext.initializeContext初始化Hystrix请求上下文,
                请求结束之后需要调用shutdown方法关闭请求;
            2)通过定义过滤器来实现此过程:
                @Component
                @WebFilter(filterName="hystrixRequestContextServletFilter",urlPatterns="/*",asyncSupported=true)
                public class HystrixRequestContextServletFilter implements Filter {
                    @Override
                    public void init(FilterConfig filterConfig) throws ServletException{}
                    @Override
                    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, 
                        FilterChain filterChain) throws IOException, ServletException {
                        HystrixRequestContext context = HystrixRequestContext.initializeContext();
                        filterChain.doFilter(servletRequest,servletResponse);
                        context.close();
                    }
                    @Override
                    public void destroy() {}
                }
            3)改造完毕后,重启项目再次访问http://localhost:9000/testCache,能正常获取到数据;
            (缺点: 该缓存只能在一次请求中生效;即一次请求查询多次时起作用;)
    [2]设定key值: (指定缓存的key值)指定key的值有两种方式: (方法2的优先级比方法1高)
        (1)通过@CacheKey注解指定:
            1)直接指定参数为key:
                @CacheResult
                @HystrixCommand(fallbackMethod ="getUserDefault",commandKey="getUserById",groupKey="userGroup",
                    threadPoolKey = "getUserThread")
                public User getUser(@CacheKey("id") @PathVariable Long id) {
	                log.info("获取用户信息");
                    return restTemplate.getForObject("http://Eureka-Client/user/{id}", User.class, id);
                }
            2)指定参数对象内部属性为key:
                @CacheResult
                @HystrixCommand(fallbackMethod ="getUserDefault",commandKey="getUserById",groupKey="userGroup",
                    threadPoolKey = "getUserThread")
                public User getUser(@CacheKey("id") @PathVariable User user) {
	                log.info("获取用户信息");
                    return restTemplate.getForObject("http://Eureka-Client/user/{id}", User.class, user.getId);
                }
        (2)通过方法来指定: (方法的返回值必须是String类型)
            public String getCacheKey(Long id) {
                return String.valueOf(id);
            }
            @CacheResult(cacheKeyMethod = "getCacheKey")
            @HystrixCommand(fallbackMethod ="getUserDefault",commandKey="getUserById",groupKey="userGroup",
                threadPoolKey = "getUserThread")
            public User getUser(@CacheKey("id") @PathVariable Long id) {
	           log.info("获取用户信息");
                return restTemplate.getForObject("http://Eureka-Client/user/{id}", User.class, id);
            }
    [3]缓存清除: (适用于更新操作,用来防止缓存数据和实际数据不一致的问题)
        @CacheRemove(commandKey = "getUserById")
        @HystrixCommand
        public void updateUser(@CacheKey("id") User user) {
            this.restTemplate.put("http://Eureka-Client/user", user);
        }
        (@CacheRemove的commandKey属性和getUser里定义的一致)
    [4]请求合并: 将多个单个请求合并成一个请求,去调用服务提供者,从而降低服务提供者负载的,一种应对高并发的解决办法;
        (1)@HystrixCollapser注解: 可将处于很短的时间段(默认10 毫秒)内对同一依赖服务的多个请求进行整合并以批量方式发起请求;
        (2)改造下Eureka-Client的UserController接口,提供一个批量处理的方法:
            @GetMapping("users")
            public List<User> get(String ids) {
                logger.info("批量获取用户信息");
                List<User> list = new ArrayList<>();
                for (String id : ids.split(",")) {
                    list.add(new User(Long.valueOf(id), "user" + id, "123456"));
                }
                return list;
            }
        (3)在Consumer的UserService里添加两个方法:
            @HystrixCollapser(batchMethod = "findUserBatch", collapserProperties = {
                @HystrixProperty(name = "timerDelayInMilliseconds", value = "100")})
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
                User[] users = this.template.getForObject("http://Eureka-Client/user/users?ids={1}", 
                    User[].class, StringUtils.join(ids, ","));
                return Arrays.asList(users != null ? users : new User[0]);
            }
            1)@HystrixCollapser注解的batchMethod属性: 指定批量处理的方法;
            2)timerDelayInMilliseconds: 多个调用合并为一个批量处理操作的时间范围;
        (4)在Client的TestController中添加一个测试方法测试批量处理:
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
            (对findUser方法进行了4次的调用,最后一次调用之前先让线程等待200毫秒,只有前3次会被合并)
        (5)启动Consumer,访问http://localhost:9000/testRequestMerge:
            1)观察Consumer的控制台,可以看到前3个请求合并成了一个请求;
            2)Client控制台未打印findUser方法中的获取单个用户信息的日志,说明所有请求以批量形式来处理,
                超出时间范围的作为另一批,实际单个请求的方法不会执行;
        (请求的合并可减轻带宽和服务的压力,但合并请求的过程也会带来额外的开销,容易造成响应超时,需谨慎使用)
```
