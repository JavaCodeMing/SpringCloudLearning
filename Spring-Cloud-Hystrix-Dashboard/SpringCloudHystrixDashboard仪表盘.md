# 背景
```text
1.Hystrix提供了Hystrix Dashboard来实时监控Hystrix的运行情况,通过Hystrix Dashboard反馈的实时信息,
    可以帮助我们快速发现系统中存在的问题,从而及时地采取应对措施;
2.Spring Cloud对Hystrix Dashboard进行了整合,这里将介绍如何使用Hystrix Dashboard监控单个和多个Hystrix实例;
```
# 监控单个Hystrix实例
```text
1.创建一个Spring Boot(2.1.9)项目,groupId填com.example,artifactId填hystrix-dashboard,
    点击Ops,勾选Spring Boot Actuator,点击Spring Cloud Circuit Breaker,勾选Hystrix和Hystrix Dashboard;
    生成的pom文件内容如下:
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.9.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.example</groupId>
    <artifactId>hystrix-dashboard</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>Hystrix-Dashboard</name>
    <description>Demo project for Spring Boot</description>
    <properties>
        <java.version>1.8</java.version>
        <spring-cloud.version>Greenwich.SR3</spring-cloud.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
2.在application.yml文件中添加配置:
    spring:
        application:
            name: Hystrix-Dashboard
    server:
        port: 9002
3.入口类上加入注解@EnableHystrixDashboard来启用Hystrix Dashboard的功能;
    启动项目访问http://localhost:9002/hystrix,可进入到Hystrix Dashboard界面;
    界面上提供了三种监控的模式: (两种集群监控,一种单机监控)
        Cluster via Turbine(default cluster)
        Cluster via Turbine(custom cluster)
        Single Hystrix App
4.将"Spring Cloud Hystrix服务容错"中的项目都拷贝一份过来,在消费端的pom文件中添加actuator的依赖,
    并在yml文件中添加配置暴露监控信息:
    [1]pom.xml:
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    [2]application.yml: (请求监控默认不开启,会导致连接不上Command Metric Stream)
        management:
            endpoints:
                web:
                    exposure:
                        include: "*"
5.分别启动Eureka-Server集群,Eureka-Client,Eureka-Consumer,并在监控页面上输入
    http://localhost:9000/actuator/hystrix.stream向消费端发送几条请求,查看监控页面;
```
![image](https://note.youdao.com/yws/public/resource/6d1f11c67c306f128bdcb75a7c16e0b6/xmlnote/353D2B691F404A02B558610FE7637D3C/15732)
# Turbine集群监控
```text
1.使用Turbine实现对Hystrix的集群监控的思路:
    (1)Turbine从Eureka服务注册中心通过服务名获取服务实例,然后Hystrix Dashboard对Turbine进行监控;
    (2)这样就实现了Hystrix Dashboard同时对多个Hystrix(消费端)实例同时进行监控的功能;
2.创建一个Spring Boot(2.1.9)项目,groupId填com.example,artifactId填turbine,然后引入如下依赖:
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-turbine</artifactId>
    </dependency>
3.在入口类中加入@EnableTurbine来开启Turbine的功能,并且加入@EnableHystrixDashboard注解,开启服务获取功能;
4.在application.yml文件中添加配置:
    spring:
        application:
            name: Turbine
    server:
        port: 9003
    eureka:
        client:
            defaultZone: http://kimi:123456@peer1:8080/eureka/,http://kimi:123456@peer2:8081/eureka/
    turbine:
        app-config: Eureka-Consumer
        cluster-name-expression: new String('default')
        combine-host-port: true
    [1]Turbine的配置解析:
        (1)turbine.app-config: 指定了需要收集监控信息的服务名;
        (2)turbine.cluster-name-expression: 参数指定了集群名称为default, 当服务数量非常多的时候,可以启动多个Turbine
            服务来构建不同的聚合集群, 而该参数可以用来区分这些不同的聚合集群,同时该参数值可以在Hystrix仪表盘中用来定位
            不同的聚合集群,只需在Hystrix Stream的URL中通过cluster参数来指定(即Cluster via Turbine(custom cluster))
        (3)turbine.combine-host-port:设置为true,可以让同一主机上的服务通过主机名与端口号的组合来进行区分, 默认情况
            下会以host来区分不同的服务,这会使得在本地调试的时候,本机上的不同服务聚合成一个服务来统计;
5.配置完毕后,分别启动下面这些服务:
    [1]启动Eureka-Server集群,端口号为8080和8081;
    [2]启动一个Eureka-Client,端口号为8082;
    [3]启动两个Eureka-Consumer，端口号为9000和9001;
    [4]启动Turbine服务,端口号为9003;
    [5]最后启动Hystrix-Dashboard服务,端口号为9002;
6.启动好这些服务后,访问Eureka-server的http://localhost:8080可看到这些实例;
7.访问Hystrix-dashboard的地址:http://localhost:9002/hystrix，在页面的地址栏输入http://localhost:9003/turbine.stream,
    稍后发送请求后就可看到监控信息;
```
