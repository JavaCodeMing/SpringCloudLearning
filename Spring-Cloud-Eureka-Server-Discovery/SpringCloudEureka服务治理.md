# 背景
```text
1.考虑当前有两个微服务实例A和B,A服务需要调用B服务的某个REST接口;
    假如某一天B服务迁移到了另外一台服务器,IP和端口也发生了变化,
    这时候我们不得不去修改A服务中调用B服务REST接口的静态配置;
2.随着公司业务的发展,微服务的数量也越来越多,服务间的关系可能变得非常复杂,
    传统的微服务维护变得愈加困难,也很容易出错;
3.所谓服务治理就是用来实现各个微服务实例的自动化注册与发现,在这种模式下,
    服务间的调用不再通过指定具体的实例地址来实现,而是通过向服务注册中心
    获取服务名并发起请求调用实现;
```
# Eureka的简介
![image](https://note.youdao.com/yws/public/resource/6d1f11c67c306f128bdcb75a7c16e0b6/xmlnote/9F78F4CE54DB47C7916B966ADE51EE7D/11977)
```
1.Eureka是由Netflix开发的一款服务治理开源框架,Spring-cloud对其进行了集成;
2.Eureka的功能组件: 服务端和客户端
    [1]Eureka服务端: 是一个服务注册中心(Eureka Server)
        (1)功能: 提供服务的注册和发现,即当前有哪些服务注册进来可供使用;
    [2]Eureka客户端: 
        (1)功能:
            1)将自己提供的服务注册到Eureka服务端,并周期性地发送心跳来更新它的服务租约;
            2)从服务端查询当前注册的服务信息并把它们缓存到本地并周期性地刷新服务状态;
    (这样服务消费者(Server Consumer)便可以从服务注册中心获取服务名称,并消费服务)
```
# 搭建Eureka-Server服务注册中心
```text
1.新建一个Spring Boot(2.1.9)项目,groupId填com.example,artifactId填eureka-server,
    点击Spring Cloud Discovery,勾选Eureka Server;生成的pom文件内容如下:
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.9.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.example</groupId>
    <artifactId>eureka-server</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>Eureka-Server</name>
    <description>Demo project for Spring Boot</description>
    <properties>
        <java.version>1.8</java.version>
        <spring-cloud.version>Greenwich.SR3</spring-cloud.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
2.在启动类上添加@EnableEurekaServer注解,表明这是一个Eureka服务端:
    @EnableEurekaServer
    @SpringBootApplication
    public class EurekaServerApplication {
        public static void main(String[] args) {
            SpringApplication.run(EurekaServerApplication.class, args);
        }
    }
3.在application.yml中添加相关配置:
    server:
        port: 8080
    spring:
        application:
            name: Eureka-Server
    eureka:
        instance:
            hostname: localhost
        client:
            # 防止只有一个服务端时自身注册的问题
            register-with-eureka: false
            fetch-registry: false
        service-url:
            defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
    [1]Eureka配置说明:
        (1)eureka.instance.hostname: 指定了Eureka服务端的IP;
        (2)eureka.client.register-with-eureka: 
            表示是否将服务注册到Eureka服务端,由于自身就是Eureka服务端,所以设置为false;
        (3)eureka.client.fetch-registry: 
            表示是否从Eureka服务端获取服务信息,由于只有一个Eureka服务端,无需获取,设置为false;
        (4)eureka.client.service-url: 
            值为Map类型,用于指定Eureka服务端的地址;(默认为defaultZone: http://localhost:8761/eureka)
4.启动服务进行测试:
    [1]访问http://localhost:8080/,能进入到Eureka监控界面;
```
# 搭建Eureka-Client服务提供者
```text
1.新建一个Spring Boot项目,groupId填com.example,artifactId填eureka-client,
    点击Spring Cloud Discovery,勾选Eureka Discovery Client;生成的pom文件内容如下:
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.9.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.example</groupId>
    <artifactId>eureka-client</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>Eureka-Client</name>
    <description>Demo project for Spring Boot</description>
    <properties>
        <java.version>1.8</java.version>
        <spring-cloud.version>Greenwich.SR3</spring-cloud.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
2.在启动类上添加@EnableEurekaClient或@EnableDiscoveryClient注解,表明这是一个Eureka客户端:
    @EnableEurekaClient
    @SpringBootApplication
    public class EurekaClientApplication {
        public static void main(String[] args) {
            SpringApplication.run(EurekaClientApplication.class,args);
        }
    }
3.配置application.yml:
    server:
        port: 8082
    spring:
        application:
            name: Eureka-Client
    eureka:
        client:
            register-with-eureka: true
            fetch-registry: true
            service-url:
                defaultZone: http://localhost:8080/eureka/
    [1]配置说明:
        (1)server.port: 指定服务的端口为8082;
        (2)spring.application.name: 用于指定服务名称,后续服务消费者基于服务名来调用服务;
        (3)eureka.client.service-url: 
            值为Map类型,用于指定Eureka服务端的地址;
        (4)eureka.client.register-with-eureka: 表示是否将服务注册到Eureka服务端;
        (5)eureka.client.fetch-registry: 表示是否从Eureka服务端获取服务信息;
4.编写一个TestController,对外提供一些REST服务:
    @RestController
    public class TestController {
        private Logger logger = LoggerFactory.getLogger(this.getClass());
        @Autowired
        private DiscoveryClient client; #该对象可用来获取服务的一些信息;
        @GetMapping("/info")
        public String info(){
            ServiceInstance serviceInstance = client.getInstances("Eureka-Client").get(0);
            String info = "host: "+serviceInstance.getHost()+",service_id: "
                +serviceInstance.getServiceId();
            logger.info(info);
            return info;
        }
        @GetMapping("/hello")
        public String hello(){
            return "hello world";
        }
    }

5.启动Eureka-Client,可以从控制台中看到注册成功的消息:
    Registered Applications size is zero : true
    Application version is -1: true
    Getting all instance registry info from the eureka server
    The response status is 200
    Starting heartbeat executor: renew interval is: 30
    InstanceInfoReplicator onDemand update allowed rate per min is 4
    Discovery Client initialized at timestamp 1530667498944 with initial instances 
    Registering application Server-Provider with eureka with status UP
    Saw local status change event StatusChangeEvent [timestamp=1530667498949,current=UP, previous=STARTING] 
    DiscoveryClient_SERVER-PROVIDER/192.168.73.109:Server-Provider:8082: registering service... 
    DiscoveryClient_SERVER-PROVIDER/192.168.73.109:Server-Provider:8082 - registration status: 204
    Tomcat started on port(s): 8082 (http)
    Updating port to 8082
    Started DemoApplication in 7.007 seconds (JVM running for 8.355)
    [1]第3,4行输出表示已经成功从Eureka服务端获取到了服务;
    [2]第5行表示发送心跳给Eureka服务端,续约(renew)服务;
    [3]第8到11行表示已经成功将服务注册到了Eureka服务端;
6.进行访问测试:
    [1]再次访问http://localhost:8080/:
        可看到服务列表里已经出现了名字为Server-providerde的服务;状态为UP,表示在线;
    [2]关闭Eureka客户端,再次刷新http://localhost:8080/:
        可看到虽然Eureka客户端已经关闭了,但是Eureka服务端页面的服务服务列表中依然还有该服务;
        页面红色文字提示,Eureka已经进入了保护模式;
        当Eureka服务端在短时间内同时丢失了过多的Eureka客户端时,Eureka服务端会进入保护模式,不去剔除这些客户端;
    [3]关闭保护模式: (在Eureka服务端添加配置)
        eureka:
            server:
                enable-self-preservation: false
```
# Eureka-Server集群
```text
1.修改Eureka服务端的配置:
    eureka:
        client:
            register-with-eureka: true
            fetch-registry: true
    [1]开启这两个参数可以让当前Eureka服务端将自己也作为服务注册到别的Eureka服务端,
        并且从别的Eureka服务端获取服务进行同步;
    [2]这两参数默认就是true,可不进行配置,也可显示配置;
2.在Eureka-Server项目的src/main/resource目录下新建application-peer1.yml,配置如下:
    server:
        port: 8080
        context-path: /eureka  #因为源码未更新,不加会导致不可用
    spring:
        application:
            name: Eureka-Server
    security:
        user:
            name: kimi
            password: 123456
    eureka:
        instance:
            hostname: peer1
        client:
            register-with-eureka: true
            fetch-registry: true
        service-url:
            defaultZone: http://peer2:8081/eureka/
    server:
        enable-self-preservation: false
    [1]配置说明:
        (1)server.port: 指定服务端口;
        (2)spring.application.name: 指定服务名称;
        (3)eureka.instance.hostname: 指定当前服务实例的地址;
        (4)eureka.client.service-url: 指定Eureka服务端的地址为另外一个Eureka服务端的地址;
3.创建另外一个Eureka服务端peer2的yml配置application-peer2.yml:
    server:
        port: 8081
            context-path: /eureka #因为源码未更新,不加会导致不可用
    spring:
        application:
            name: Eureka-Server
    security:
        user:
            name: kimi
            password: 123456
    eureka:
        instance:
            hostname: peer2
        client:
            register-with-eureka: true
            fetch-registry: true
            service-url:
                defaultZone: http://peer1:8080/eureka/
        server:
            enable-self-preservation: false
4.修改hosts文件:(C:\Windows\System32\drivers\etc\)
    127.0.0.1       peer1
    127.0.0.1       peer2
5.Eureka-Server项目打包成jar,运行以下两条命令来部署peer1和peer2:
    java -jar eureka-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=peer1
    java -jar eureka-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=peer2
6.修改Eureka客户端的配置: (应对Eureka服务端集群化)
    eureka:
        client:
            serviceUrl:
                defaultZone: http://peer1:8080/eureka/,http://peer2:8081/eureka/
7.Eureka客户端打成jar包，然后分别用端口8082和8083启动两个服务:
    java -jar eureka-client-0.0.1-SNAPSHOT.jar --server.port=8082
    java -jar eureka-client-0.0.1-SNAPSHOT.jar --server.port=8083
8.访问http://peer2:8080/或者http://peer2:8081/,查看Eureka的监控界面;
```
# 搭建Server-Consumer服务消费者
```text
1.新建一个Spring Boot项目,groupId填com.example,artifactId填server-consumer,
    点击Spring Cloud Discovery,勾选Eureka Discovery Client,点击Spring Cloud  Routing,勾选Ribbon;
    生成的pom文件内容如下:
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.9.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.example</groupId>
    <artifactId>server-consumer</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>Server-Consumer</name>
    <description>Demo project for Spring Boot</description>
    <properties>
        <java.version>1.8</java.version>
        <spring-cloud.version>Greenwich.SR3</spring-cloud.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <!--一款实现服务负载均衡的开源软件-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
2.编写配置文件application.yml:
    server:
        port: 9000
    spring:
        application:
            name: Server-Consumer
    eureka:
        client:
            service-url:
                defaultZone: http://peer1:8080/eureka/,http://peer2:8081/eureka/
3.在入口类中加入@EnableDiscoveryClient注解用于发现服务和注册服务,并配置一个RestTemplate Bean,
    然后加上@LoadBalanced注解来开启负载均衡:
    @EnableDiscoveryClient
    @SpringBootApplication
    public class ServerConsumerApplication {
        @Bean
        @LoadBalanced
        RestTemplate restTemplate(){ return new RestTemplate(); }
        public static void main(String[] args) {
            SpringApplication.run(ServerConsumerApplication.class, args);
        }
    }
4.编写一个TestController,用于消费服务:
    @RestController
    public class TestController {
        @Autowired
        private RestTemplate template;
        @GetMapping("/info")
        public String getInfo(){
            # 使用RestTemplate对象均衡的去获取服务并消费
            # 使用服务名称(Eureka-Client)去获取服务的,而不是使用传统的IP加端口的形式
            return this.template.getForEntity("http://Eureka-Client/info",String.class).getBody();
        }
    }
5.启动该项目,访问http://localhost:9000/info:
    成功获取到了信息,多次访问该接口,观察8082和8083Eureka客户端的后台查看负载均衡的效果;
    关闭一个Eureka服务端,再次访问http://localhost:9000/info,还可成功获取到信息,这就是Eureka服务端集群的好处;
```
# Eureka-Server添加认证
```text
1.在Eureka-Server引入Spring-Security依赖:
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
        <version>2.1.9.RELEASE</version>
        <scope>compile</scope>
    </dependency>
2.在Eureka-Server的application-peer1.yml和application-peer2.yml中配置用户名和密码:
    spring:
        security:
            user:
                name: kimi
                password: 123456
3.在Eureka的客户端和消费端的配置中添加用户名和密码:
    eureka:
        client:
            service-url:
                defaultZone: http://kimi:123456@peer1:8080/eureka/,http://kimi:123456@peer2:8081/eureka/
    [1]重新打包并部署后,访问http://localhost:8080/,页面将弹出验证窗口,输入用户名和密码后即可访问;
4.添加配置类关闭防csrf: (spring boot2.0版的Spring security 会默认开启防csrf攻击)
    (需要关闭防csrf攻击否则否则服务不可用)
    @EnableWebSecurity
    public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable();
        }
    }
```
