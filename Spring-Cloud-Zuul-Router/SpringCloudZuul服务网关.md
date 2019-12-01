# 简介:
```text
1.在微服务的架构中,服务网关就是一个介于客户端与服务端之间的中间层;在这种情况下,
    客户端只需要跟服务网关交互,无需调用具体的微服务接口;
2.好处在于,客户端可以降低复杂性;对于需要认证的服务,只需要在服务网关配置即可;同样也方便
    后期微服务的变更和重构,即微服务接口变更只需在服务网关调整配置即可,无需更改客户端代码;
3.Zuul是一款由Netflix开发的微服务网关开源软件,可以和其自家开发的Eureka,
    Ribbon和Hystrix配合使用,Spring Cloud对其进行了封装;
```
# 入门
```text
1.先将相关微服务搭建好,将之前的Eureka-Server、Eureka-Client和Feign-Consumer拷贝过来,做如下步骤:
    [1]分别启动如下服务: (启动完访问http://localhost:8080/进行查看)
        (1)启动Eureka-Server集群,端口号为8080和8081;
        (2)启动一个Eureka-Client,端口号为8082;
        (3)启动一个Feign-Consumer,端口号为9000;
        (其中Feign-Consumer虽然之前我们将它定义为服务消费者,但其也可以充当服务提供者的角色)
    [2]构建一个微服务网关Zuul-Gateway:
        (1)创建一个Spring Boot(2.1.9)项目,groupId填com.example,artifactId填zuul-gateway,然后引入如下依赖:
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-starter-netflix-zuul</artifactId>
            </dependency>
            (spring-cloud-starter-zuul依赖已经包含了Hystrix和Ribbon,所以zuul支持前面介绍的Hystrix和Ribbon相关配置)
        (2)在Spring Boot的入口类上标注@EnableZuulProxy注解,开启Zuul服务网关的功能:
            @EnableZuulProxy
            @SpringBootApplication
            public class ZuulGatewayApplication {
                public static void main(String[] args) {
                    SpringApplication.run(ZuulGatewayApplication.class, args);
                }
            }
        (3)配置下application.yml:
            server:
                port: 12580
            spring:
                application:
                    name: Zuul-Gateway
```
# 路由配置
```text
1.传统配置:传统配置就是手动指定服务的转发地址,如在yml中配置:
    zuul:
        routes:
        # 所有符合path上规则的访问都将被路由转发到url地址上
            api-a:
                path: /api-a/**
                url: http://localhost:8082
        (访问服务网关进行测试: http://localhost:12580/api-a/hello)
2.基于服务名称配置:
    [1]与传统配置的区别: 
        可借助Eureka来实现通过服务名称配置路由,不需要知道服务的具体地址和端口号等信息;
    [2]在Zuul-Gateway项目中引入Eureka依赖:
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
    [3]在入口类中加入@EnableDiscoveryClient注解,使其具有获取服务的能力,并在yml中配置基于服务名称的路由:
        eureka:
            client:
                service-url:
                    # 指明Eureka服务注册中心的地址
                    defaultZone: http://kimi:123456@peer1:8080/eureka/,http://kimi:123456@peer2:8081/eureka/
        zuul:
            routes:
                # 路由,通过serviceId来指定服务名称
                api-b:
                    path: /api-b/**
                    serviceId: Eureka-Client
                api-c:
                    path: /api-c/**
                    serviceId: Server-Consumer
        (访问服务网关进行测试:http://localhost:12580/api-b/hello)
        (访问服务网关进行测试:http://localhost:12580/api-c/user/1)
    [4]基于服务名称的简化路由配置,格式为zuul.routes.<serviceid>=<path>:
        zuul:
            routes:
                Eureka-Client:
                    path: /api-b/**
                Server-Consumer:
                    path: /api-c/**
3.默认路由配置规则:
    [1]尝试访问 http://localhost:12580/server-consumer/user/1,能正常访问;
    [2]Zuul配合Eureka后将会成一套默认的配置规则,当我们使用服务名称作为请求的前缀路径时,
        实际上就会匹配上类似下面的默认路由配置:
        zuul:
            routes:
                server-consumer:
                    path: /server-consumer/**
                    serviceId: server-consumer
    [3]关闭默认路由配置: 
        zuul:
            ignored-services: server-consumer
        (设置为zuul.ignored-services=*的时候将关闭所有默认路由配置规则)
4.优先级:
    某个请求路径可以和多个路由配置规则相匹配的话，Zuul根据匹配的先后顺序来决定最终使用哪个路由配置;
5.前缀配置: 使用zuul.prefix可为网关的请求路径加个前缀:
    zuul:
        prefix: /gateway
    (如 http://localhost:12580/gateway/api-c/user/1)
6.本地跳转: Zuul网关除了支持将服务转发到各个微服务上之外,还支持将服务跳转到网关本身的服务上
    [1]添加本地跳转的网关配置:
        zuul:
            routes:
                api-e:
                    path: /api-e/**
                    url: forward:/test
    [2]在Zuul-Gateway入口类中加上该REST服务:
        @EnableZuulProxy
        @EnableDiscoveryClient
        @SpringBootApplication
        @RestController
        public class ZuulGatewayApplication {
            @GetMapping("/test/hello")
            public String hello() {
                return "hello zuul";
            }
            public static void main(String[] args) {
                SpringApplication.run(ZuulGatewayApplication.class, args);
            }
        }
    [3]访问测试: http://localhost:12580/api-e/hello
```
# 头部过滤 & 重定向
```text
1.在使用Zuul网关的时候你可能会遇到Cookie丢失的情况,因为默认情况下Zuul会过滤掉HTTP请求头中的一些敏感信息,
    这些敏感信息通过下面的配置设定:
    zuul:
        sensitive-headers: Cookie,Set-Cookie,Authorization
2.如果想关闭这个默认配置,通过设置全局参数为空来覆盖默认值:
    zuul:
        sensitive-headers:
3.如果只想关闭某个路由的HTTP请求头过滤,可以这样:
    zuul:
        routes:
            api-a:
                sensitive-headers:
4.Zuul另一个常见问题是重定向的问题,可以通过下面的设置解决:
    zuul:
        add-host-header: true
```
# 过滤器
![image](https://note.youdao.com/yws/public/resource/6d1f11c67c306f128bdcb75a7c16e0b6/xmlnote/DDB94C26FE974B50806BFD1E1AAD0E99/16050)
```text
1.Zuul另一个核心的功能就是请求过滤;Zuul中默认定义了4种不同生命周期的过滤器类型,在如上图所示:
2.这4种过滤器处于不同的生命周期,所以其职责也各不相同:
    [1]PRE: PRE过滤器用于将请求路径与配置的路由规则进行匹配,以找到需要转发的目标地址,并做一些前置加工,比如请求的校验等;
    [2]ROUTING: ROUTING过滤器用于将外部请求转发到具体服务实例上去;
    [3]POST: POST过滤器用于将微服务的响应信息返回到客户端,这个过程种可以对返回数据进行加工处理;
    [4]ERROR: 上述的过程发生异常后将调用ERROR过滤器;ERROR过滤器捕获到异常后需要将异常信息返回给客户端,所以最终还是会调用POST过滤器;
3.核心过滤器: (其中优先级数字越小,优先级越高)
    [1]post: 
        LocationRewriteFilter: 
        SendErrorFilter: 0,处理有错误的请求响应
        SendResponseFilter: 1000,处理正常的请求响应
    [2]pre:
        DebugFilter: 1,标记调试标志
        FromBodyWrapperFilter: -1,包装请求体
        PreDecorationFilter: 5,处理请求上下文供后续使用
        Servlet30ResquestWrapper: 
        Servlet30WrapperFilter: -2,包装HttpServletRequest请求
        ServletDetectionFilter: -3,标记处理Servlet的类型
    [3]route:
        RibbonRoutingFilter: 10,serviceId请求转发
        SimpleHostRoutingFilter: 100,url请求转发
        SendForwardFilter: 500,forward请求转发
4.在application.yml中,关闭这些过滤器:
    [1]格式: zuul.<SimpleClassName>.<filterType>.disable=true
    [2]示例: (关闭SendResponseFilter过滤器)
        zuul:
            SendResponseFilter:
                post:
                    disable:
                        true
5.自定义Zuul过滤器:
    [1]自定义一个PreSendForwardFilter用于获取请求转发前的一些信息:
        public class PreSendForwardFilter extends ZuulFilter{
            @Override
            public String filterType() {
                return null;
            }
            @Override
            public int filterOrder() {
                return 0;
            }
            @Override
            public boolean shouldFilter() {
                return false;
            }
            @Override
            public Object run() {
                return null;
            }
        }
    [2]可以看到自定义Zuul过滤器只需要继承ZuulFilter,然后实现以下四个抽象方法即可:
        (1)filterType: 对应Zuul生命周期的四个阶段: pre、post、route和error;
        (2)filterOrder: 过滤器的优先级,数字越小,优先级越高;
        (3)shouldFilter: 方法返回boolean类型,true时表示是否执行该过滤器的run方法,false则表示不执行;
        (4)run: 过滤器的过滤逻辑;
    [3]继续完善PreSendForwardFilter:
        @Component
        public class PreSendForwardFilter extends ZuulFilter{
            private Logger log = LoggerFactory.getLogger(this.getClass());
            @Override
            public String filterType() {
                return "pre";
            }
            @Override
            public int filterOrder() {
                return 1;
            }
            @Override
            public boolean shouldFilter() {
                return true;
            }
            @Override
            public Object run() {
                RequestContext requestContext = RequestContext.getCurrentContext();
                HttpServletRequest request = requestContext.getRequest();
                String host = request.getRemoteHost();
                String method = request.getMethod();
                String uri = request.getRequestURI();
                log.info("请求URI：{}，HTTP Method：{}，请求IP：{}", uri, method, host);
                return null;
            }
        }
    [4]访问http://localhost:12580/api-a/hello,查看控制台日志;
```
