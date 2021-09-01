# CMPP转HTTP项目

## 项目简介
该项目将常用短信协议CMPP、SGIP、SMGP、SMPP进行封装，提供http接口发送短信，使刚接触短信的用户，只需要简单配置通道，即可快速发送短信。

## 项目技术栈
Spring Boot + Mysql + Redis + Netty 4.x + Hutool 5.x + Mybatis-plus 3.x + Guava + Caffeine 等

## 项目使用
1. 前提：本地安装了mysql 5.x和redis
2. 数据库初始化：执行resource/sql下的sms.sql文件创建数据库和表
3. 账号密码修改：修改application.yml中Mysql和Redis的验证信息
4. 初始化通道：在sms_channel表中添加通道，主要字段说明如下：
+ channel_ip: 通道方提供的通道ip地址
+ port: 通道方提供的通道端口号（cmpp默认为7890，通道方也可能换成其他端口号）
+ login_name: 通道登录用户名
+ password: 通道登录用户密码
+ src_id: 接入号，一般以106***开头
+ msg_src： 企业代码，对应submit的msg_src, 大部分通道提供的sp_id与username是一致的，也有的通道方不提供企业代码，不提供企业代码时与username填一样的即可
+ speed: 单个连接的速度
+ max_connect: 当前通道最大连接数
5. 启动项目，调用SmsController下/sms/sendMsg接口发送短信

## 感谢
项目是在 [SMSGate](https://github.com/Lihuanghe/SMSGate) 基础上开发的，感谢黄河大佬提供这么好的开源项目

## 交流
欢迎各位Star！！！qq群号765161317
![输入图片说明](https://images.gitee.com/uploads/images/2021/0901/150153_e191c1e6_1951833.png "屏幕截图.png")
