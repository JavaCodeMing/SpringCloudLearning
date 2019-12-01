# 简介
```text
1.Spring Cloud Config可以对微服务配置进行统一的外部管理,并且默认采用Git来管理配置信息;
2.相对于传统的每个微服务对应一份自个儿的配置文件来说,通过Spring Cloud Config统一管理所有微服务配置具有如下优点:
    [1]集中管理微服务配置,当微服务数量众多的时候,使用这种方式会更为方便;
    [2]通过Git管理微服务配置,方便追踪配置修改记录;
    [3]可以在应用运行期间修改配置,微服务能够自动更新配置;
3.Spring Cloud Config包含了服务端Server和客户端Client:
    [1]服务端用于从Git仓库中加载配置,并且缓存到本地;
    [2]客户端用于从服务端获取配置信息;
```
# 搭建Config-Server
```text
1.创建好一个存储配置文件的Git仓库(以GitHub为例),创建一个名称为Spring-Cloud-Config,然后往仓库的master分支push四个配置文件:
    [1]febs.yml: message: 'default properties (master v1.0)'
    [2]febs-dev.yml: message: 'dev properties (master v1.0)'
    [3]febs-test.yml: message: 'test properties (master v1.0)'
    [4]febs-pro.yml: message: 'pro properties (master v1.0)'
2.接着创建一个test分支,同样push这四个配置文件,不过配置文件中message属性的内容末尾都改为了(test v1.0);
3.创建好配置文件仓库后,新建一个Spring Boot项目,artifactId为config-server,然后引入如下依赖:
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-config-server</artifactId>
        <version>2.1.4.RELEASE</version>
    </dependency>
4.在入口类中加入@EnableConfigServer注解,开启Spring Cloud Config服务端功能,配置application.yml:
    server:
        port: 12580
    spring:
        application:
            name: config-server
        cloud:
            config:
                server:
                    git:
                        uri: https://github.com/JavaCodeMing/Spring-Cloud-Config.git
                        username: xxx
                        password: xxx
    [1]配置具体含义如下:
        (1)spring.cloud.config.server.git.uri: 配置了Git仓库的地址(GitHub,码云)
        (2)spring.cloud.config.server.git.username: 仓库用户名,即Git托管平台的用户名;
        (3)spring.cloud.config.server.git.password: 仓库密码,即Git托管平台的密码;
        (4)server.port: 服务端的端口号为12580
    [2]启动应用,便可使用下面这些格式来访问配置信息了:
        (1)/{application}/{profile}[/{label}]
        (2)/{application}-{profile}.yml
        (3)/{label}/{application}-{profile}.yml
        (4)/{application}-{profile}.properties
        (5)/{label}/{application}-{profile}.properties
5.访问测试:
    [1]访问http://localhost:12580/master/febs-test:
    [2]访问http://localhost:12580/master/febs-test.yml:
        message: test properties (master v1.0)
    [3]访问http://localhost:12580/test/febs-test.yml:
        message: test properties (test v1.0)
```
# 搭建Config-Client
```
1.新建一个Spring Boot项目,artifactId为config-client,然后引入如下依赖:
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
        <version>2.1.4.RELEASE</version>
    </dependency>
2.创建bootstrap.yml,并进行配置:
    server:
        port: 12581
    spring:
        application:
            name: febs
        cloud:
            config:
                profile: dev
                label: test
                uri: http://localhost:12580
    [1]配置含义:
        (1)spring.application.name: 对应配置文件规则中的{application};
        (2)spring.cloud.config.profile: 对应配置文件规则中的{profile}部分;
        (3)spring.cloud.config.label: 对应配置文件规则中的{label}部分;
        (4)spring.cloud.config.uri: 对应Config-Server的地址;
    [2]测试;
        (1)访问http://localhost:12581/message:
            dev properties (test v1.0)
```
# Config-Server额外配置
```text
1.占位符的使用: 
    [1]使用占位符来灵活的指定Git仓库地址,Config-Server的Git仓库地址改为:
        spring:
            cloud:
                config:
                    server:
                        git:
                            uri: https://github.com/JavaCodeMing/{application}
        ({application}指Config-Client的项目名称)
2.子目录支持: 
    [1]通过search-paths属性来指定使用的配置文件:
        spring:
            cloud:
                config:
                    server:
                        git:
                            uri: https://github.com/JavaCodeMing/{application}
                            username: xxx
                            password: xxx
                            search-paths: '{application}'
3.clone-on-start: 
    [1]默认情况下Config-Server在启动的时候并不会马上就去Git参考clone配置文件,只有当
        Config-Clinet从Config-Server获取相关配置信息的时候,其才会去进行clone操作;
    [2]将clone-on-start属性设置为true,其Config-Server在启动的时候就进行clone操作:
        spring
            cloud:
                config:
                    server:
                        git:
                            clone-on-start: true
        (好处在于: 当Git连接信息有误时,可以马上发现)
4.整合Spring Security: (用来做用户名密码认证)
    [1]在Config-Server中加入Spring Security依赖:
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
    [2]在Config-Server的配置文件application.yml中加入用户名和密码:
        spring:
            security:
                user:
                    name: kimi
                    password: 123456
    [3]在Config-Client中配置Config-Server的用户名和密码,否则在获取配置的时候将报401错误:
        spring:
            cloud:
                config:
                    username: kimi
                    password: 123456
5.加密解密:
    [1]以上Config-Server中Git仓库的密码是直接明文配置的,一般情况下需要对这些敏感信息进行加密处理;
        (1)安装JCE(Java Cryptography Extension):
            1)JCE下载地址:
                https://www.oracle.com/technetwork/java/javase/downloads/jce-all-download-5170447.html
            2)解压后,将内含的三个文件复制到$JAVA HOME/jre/lib/security目录下;
    [2]对称加密:
        (1)使用对称加密的方式需要在Config-Server中配置加密用的密钥: (密钥配置在bootstrap.yml中,否则不生效)
            encrypt:
                key: hello
        (2)配置好后重启Config-Server,访问 http://localhost:12580/encrypt/status 查看密钥是否配置成功;
        (3)在Git Bash中,使用curl命令来获取加密后的密码: (实例中是关闭security后的)
            curl -u kimi:123456 http://localhost:12580/encrypt -d kimi1234
            1)-u kimi:123456: 表示Config-Server中security设置的用户名和密码(bug:引入security后该命令获取不到加密后的密码);
            2)localhost:12580: 表示Config-Server的ip和端口;
            3)-d kimi1234: 表示git的密码;
        (4)用加密后的密码替换掉Git连接的明文密码:
            spring:
                cloud:
                    config:
                        server:
                            git:
                                password: '{cipher}xxxxxx'
        (使用{cipher}开头表明这是一个加密的内容,Config-Server会自动为其解密;)
        (当然,你还可以为任何你想加密的属性值进行加密,不限于密码)
    [3]非对称加密:
        (1)keytool工具: 用于生成非对称加密的密码(JDK中的一个密钥和证书 管理工具,位于%JAVA_HOME%\bin\目录下);
            keytool -genkeypair -alias config-server -keyalg RSA -keysize 1024 -validity 365 -keystore config-server.keystore
            1)keytool -genkeypair -alias: 生成一个密钥对
            2)config-server: 别名
            3)-keyalg RSA: 算法用的RSA,非对称加密的主流算法
            4)-keystore config-server.keystore: 生成的密钥文件放在config-server.keystore文件里面
        (2)命令执行结束后,会在%JAVA_HOME%\bin\目录下生成一个 config-server.keystore文件;
            密钥文件的有效期默认为90天,可以使用-validity 365来改变其有效天数为365天;
        (3)将密钥文件拷贝到Config-Server的resources目录下,然后在bootstrap.yml中添加如下配置:
            encrypt:
                key-store:
                    location: classpath:config-server.keystore
                    alias: Config-Server
                    password: 123456
                    secret: 654321
            1)encrypt.key-store.password: 秘钥库口令;
            2)encrypt.key-store.secret: <Config-Server>的密钥口令
        (4)将Git连接的密码改回明文,然后重启Config-Server,运行如下命令重新对Git连接密码进行加密:
            curl -X POST -u kimi:123456 http://localhost:12580/encrypt -d kimi1234
        (5)将加密后的密码替换明文密码即可: (此处加密正常解密失败,暂时不可用)
            spring:
                cloud:
                    config:
                        server:
                            git:
                                password: '{cipher}xxxxxx'
6.刷新配置:
    [1]在不重启Config-Client的情况下,使应用获得的配置信息随Git仓库存储的配置信息更新:
        (1)在Config-Client中添加spring-boot-starter-actuator依赖:
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-starter-actuator</artifactId>
            </dependency>
            (该依赖包含了/refresh端点,可以用来刷新配置)
        (2)在获取配置的Controller上加入@RefreshScope注解:
            @RestController
            @RefreshScope
            public class TestController {
                @Value("${message}")
                private String message;
                @GetMapping("message")
                public String getMessage() {
                    return this.message;
                }
            }
        (3)在Config-Client的配置文件中加入如下配置来关闭认证,否则我们无权访问/refresh端点:
            management:
                endpoints:
                    web:
                        exposure:
                            include: "*"
        (4)重启Config-Client,先访问http://localhost:12581/actuator/refresh进行配置刷新,
            再访问http://localhost:12581/message获得更新后的配置;
```
# 集群配置
```text
1.此处是将Config-Server注册到Eureka来搭建集群:
    [1]从Spring Cloud Eureka服务治理中,将Eureka-Server拷贝过来,并启动Eureka-Server集群;
    [2]在Config-Server中加入Eureka依赖:
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
    [3]在Config-Server的启动类中加入@EnableDiscoveryClient注解,用于将服务注册到Eureka服务注册中心上:
        @EnableDiscoveryClient
        @EnableConfigServer
        @SpringBootApplication
        public class ConfigServerApplication {
            public static void main(String[] args) {
                SpringApplication.run(SpringBootConfigServerApplication.class, args);
            }
        }
    [4]在Config-Server配置类application.yml中指定Eureka服务注册中心的地址:
        eureka:
            client:
                service-url:
                    defaultZone: http://kimi:123456@peer1:8080/eureka/,http://kimi:123456@peer2:8081/eureka/
    [5]改造好后,启动Config-Server,再次访问Eureka服务注册中心地址: http://localhost:8080/,查看服务是否已启动;
    [6]在Config-Client中引入Eureka依赖:
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
    [7]在Config-Client的bootstrap.yml中指定获取Config-Server服务的地址:
        spring:
            application:
                name: febs
            cloud:
                config:
                    username: kimi
                    password: 123456
                    profile: dev
                    label: test
                    #uri: http://localhost:12580
                    discovery:
                        enabled: true
                        service-id: config-server
        eureka:
            client:
                service-url:
                    defaultZone: http://kimi:123456@peer1:8080/eureka/,http://kimi:123456@peer2:8081/eureka/
        1)eureka.client.serviceUrl.defaultZone:指定了Eureka服务注册中心的地址;
        2)spring.cloud.config.discovery.enabled: 指定为true开启获取服务的功能;
        3)spring.cloud.config.discovery.service-id: 表明需要获取服务的名称为config-server;
    [8]在Config-Client的启动类中引入@EnableDiscoveryClient注解:
        @EnableDiscoveryClient
        @SpringBootApplication
        public class ConfigClientApplication {
            public static void main(String[] args) {
                SpringApplication.run(ConfigClientApplication.class, args);
            }
        }
    [9]启动Config-Client,访问: http://localhost:12581/message,查看配置;
```
