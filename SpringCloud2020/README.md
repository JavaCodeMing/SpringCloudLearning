# 示例及配置端口说明
eureka-server:
	application.yml: 8001
	application-replica1.yml: 8002
	application-replica2.yml: 8003
eureka-security-server:
	application.yml: 8004
eureka-client:
	application.yml: 8101
	application-replica.yml: 8102
	application-security.yml: 8103
user-service:
	application.yml: 8201
	application.yml: 8202
consul-user-service:
	application.yml: 8203
	application.yml: 8204
nacos-user-service:
	application.yml: 8205
	application.yml: 8206
ribbon-service:
	application.yml: 8301
consul-ribbon-service:
	application.yml: 8302
nacos-ribbon-service:
	application.yml: 8303
hystrix-service:
	application.yml: 8401
	application.yml: 8402
sentinel-service:
	application.yml: 8403
hystrix-dashboard:
	application.yml: 8501
turbine-service:
	application.yml: 8505
feign-service:
	application.yml: 8601
zuul-proxy:
	application.yml: 8701
api-gateway:
	application.yml: 8703
	application-predicate.yml: 8704
	application-filter.yml: 8705
	application-limiter.yml: 8706
	application-eureka.yml: 8707
config-server:
	application.yml: 8801
	application.yml: 8802
	application.yml: 8803
	application-amqp.yml: 8804
config-security-server:
	application.yml: 8805
config-client:
	bootstrap.yml: 8901
	bootstrap-security.yml: 8902
	bootstrap-cluster.yml: 8903
	bootstrap-amqp1.yml: 8904
	bootstrap-amqp2.yml: 8905
consul-config-client:
	bootstrap.yml: 8906
nacos-config-client:
	bootstrap.yml: 8907
admin-server:
	application.yml: 9001
admin-security-server:
	application.yml: 9002
admin-client:
	application.yml: 9005
	
oauth2-server:
	application.yml: 9101
oauth2-jwt-server:
	application.yml: 9102
oauth2-client: 
	application.yml: 9105

seata-order-service:
	application.yml: 8180
seata-storage-service:
	application.yml: 8181
seata-account-service:
	application.yml: 8182
