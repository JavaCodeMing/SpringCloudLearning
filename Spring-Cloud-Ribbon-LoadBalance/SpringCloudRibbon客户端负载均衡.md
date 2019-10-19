# 负载均衡简介
```text
1.作用: 使获取服务的请求被均衡的分配到各个实例中;
2.分类: 服务端负载均衡和客户端负载均衡
    [1]服务端的负载均衡: 
        (1)常用的软硬件: F5(硬件)或者Nginx(软件)
        (2)原理:
            在硬件设备或者软件模块中维护一份可用服务清单,然后客户端发送服务请求
            到这些负载均衡的设备上,这些设备根据一些算法均衡的将请求转发出去;
    [2]客户端负载均衡:
        (1)常见的组件: Ribbon
        (2)原理:
            客户端自己从服务注册中心(如Eureka Server)中获取服务清单缓存到本地,
            然后通过Ribbon内部算法均衡的去访问这些服务;
```
# Ribbon简介
```
1.Ribbon是由Netflix开发的一款基于HTTP和TCP的负载均衡的开源软件;
2.Ribbon负载均衡的方式:
    [1]直接给Ribbon配置好服务列表清单;
    [2]配合Eureka主动的去获取服务清单;
    需要使用到这些服务的时候Ribbon通过轮询或者随机等均衡算法去获取服务;
```
# RestTemplate详解
```text
1.作用: 用来发送REST请求的摸板,包含了GET,POST,PUT,DELETE等HTTP Method对应的方法;
2.发送Get请求: RestTemplate中与GET请求对应的方法有getForEntity和getForObject;
    [1]getForEntity: 该方法返回ResponseEntity对象,该对象包含了返回报文头,报文体和状态码等信息;
        (1)getForEntity的三个重载方法: 
            getForEntity(String url, Class<T> responseType, Object... uriVariables);
            getForEntity(String url, Class<T> responseType, Map<String, ?> uriVariables);
            getForEntity(URI url, Class<T> responseType);
            (第一个参数为Url,第二个参数为返回值的类型,第三个参数为请求的参数(可以是数组,也可以是Map))
        (2)getForEntity(String url, Class<T> responseType, Object... uriVariables):
            @GetMapping("user/{id:\\d+}")
            public User getUser(@PathVariable Long id) {
                return this.restTemplate
                    .getForEntity("http://Server-Provider/user/{1}", User.class, id)
                    .getBody();
            }
            ({1}为参数的占位符,匹配参数数组的第一个元素;因为第二个参数指定了类型为User;
            所以调用getBody方法返回类型也为User)
        (3)getForEntity(String url, Class<T> responseType, Map<String, ?> uriVariables):
            @GetMapping("user/{id:\\d+}")
            public User getUser(@PathVariable Long id) {
                Map<String, Object> params = new HashMap<>();
                params.put("id", id);
                return this.restTemplate
                    .getForEntity("http://Server-Provider/user/{id}", User.class, params)
                    .getBody();
            }
        (4)getForEntity(URI url, Class<T> responseType): 
            @GetMapping("user/{id:\\d+}")
            public User getUser(@PathVariable Long id) {
                Map<String, Object> params = new HashMap<>();
                params.put("id", id);
                // org.springframework.web.util.UriComponentsBuilder
                URI uri = UriComponentsBuilder.fromUriString("http://Server-Provider/user/{id}")
                    .build().expand(params).encode().toUri();
                    return this.restTemplate.getForEntity(uri, User.class).getBody();
            }
            (expand方法可接收数组和Map两种类型的参数)
    [2]getForObject: 
        (1)getForObject方法和getForEntity方法类似,getForObject方法相当于getForEntity方法调用了
            getBody方法,直接返回结果对象,为不是ResponseEntity对象;
        (2)getForObject方法和getForEntity方法一样,也有三个重载方法,参数类型和getForEntity方法一致;
3.发送POST请求: 使用RestTemplate发送POST请求的方法主要有postForEntity,postForObject和postForLocation(使用较少);
    [1]postForEntity和postForObject也分别有三个重载方法,方法参数和使用方式和上面getForEntity和getForObject一样;
        @GetMapping("user")
        public List<User> getUsers() {
            return this.restTemplate.getForObject("http://Server-Provider/user", List.class);
        }
4.发送PUT请求: 使用RestTemplate发送PUT请求,使用的是它的put方法,put方法返回值是void类型;
    [1]RestTemplate的put方法的三个重载方法:
        put(String url, Object request, Object... uriVariables);
        put(String url, Object request, Map<String, ?> uriVariables);
        put(URI url, Object request);
    [2]案例:
        @GetMapping("user/update")
        public void updateUser() throws JsonProcessingException {
            User user = new User(1L, "mrbird", "123456");
            this.restTemplate.put("http://Server-Provider/user", user);
        }
        (在RESTful风格的接口中,判断成功失败不再是通过返回值的某个标识来判断的,而是通过返回报文的状态码
        是否为200来判断;当这个方法成功执行并返回时,返回报文状态为200,即可判断方法执行成功;)
5.发送DELETE请求: 使用RestTemplate发送DELETE请求,使用的是它的delete方法,delete方法返回值是void类型;
    [1]RestTemplate的delete方法的三个重载方法:
        delete(String url, Object... uriVariables)；
        delete(String url, Map<String, ?> uriVariables);
        delete(URI url);
    [2]实例:
        @GetMapping("user/delete/{id:\\d+}")
        public void deleteUser(@PathVariable Long id) {
            this.restTemplate.delete("http://Server-Provider/user/{1}", id);
        }
```
# RestTemplates实战
```text
1.把Eureka的服务端,客户端和消费端复制一份到另一个目录;
2.在Eureka客户端中编写一套RESTful风格的测试接口:
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
3.User对象:
    public class User implements Serializable {
        private static final long serialVersionUID = 1339434510787399891L;
        private Long id;
        private String username;
        private String password;
        public User() { }
        public User(Long id, String username, String password) {
            this.id = id;
            this.username = username;
            this.password = password;
        }
        // get,set略
        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    '}';
        }
    }
    注: User对象必须有默认的构造方法,否则在JSON与实体对象转换的时候会抛出如下异常:
        JSON parse error: Can not construct instance of model.Class: no suitable constructor found
4.在消费端中创建User类,并使用RestTemplates分别去获取客户端的服务:
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
5.测试负载均衡:
    [1]分别启动两个Eureka Server用于集群,两个Eureka Client实例,然后启动Server-Consumer;
    [2]用postman多次测试以下请求: (访问后查看客户端的日志,看是否达到负载均衡效果)
        http://localhost:9000/user/1
        http://localhost:9000/user/
        http://localhost:9000/user/add
        http://localhost:9000/user/update
        http://localhost:9000/user/delete/1
```
# Spring Cloud Ribbon配置
```text
1.Spring Cloud Ribbon的配置分为全局(ribbon开头)和指定服务名称(服务名称开头):
2.指定全局的服务请求连接超时时间:
    ribbon:
        ConnectTimeout: 200
3.设置获取Eureka-Client服务的请求连接超时时间:
    Eureka-Client:
        ribbon:
            ConnectTimeout: 200
4.设置获取Eureka-Client服务的负载均衡算法从轮询改为随机:
    Eureka-Client:
        ribbon:
            NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RandomRule
5.设置处理Eureka-Client服务的超时时间:
    Eureka-Client:
        ribbon:
            ReadTimeout: 1000
6.开启重试机制,即获取服务失败是否从另外一个节点重试,默认值为false:
    spring:
        cloud:
            loadbalancer:
                retry:
                    enabled: true
7.对Eureka-Client的所有请求在失败的时候都进行重试:
    Eureka-Client:
        ribbon:
            OkToRetryOnAllOperations: true
8.切换Eureka-Client实例的重试次数:
    Eureka-Client:
        ribbon:
            MaxAutoRetriesNextServer: 1
9.对Eureka-Client当前实例的重试次数:
    Eureka-Client:
        ribbon:
            MaxAutoRetries: 1
10.如果不和Eureka搭配使用的话,就需要手动指定服务清单给Ribbon:
    Eureka-Client:
        ribbon:
            listOfServers: localhost:8082,localhost:8083
```
