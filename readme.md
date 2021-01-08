# DES2000分布式能源项目
分为以下几个子模块

## flink-common 
共用组件与常用工具

## flink-kafka-event-to-cleandata
基于flink将kafka消息持久化到清洗mysql数据库清洗表中
kafka消息为entity对象

## flink-kafka-event-to-basedata
基于flink将kafka消息持久化到mysql数据库基础表中
kafka消息为entity对象

## flink-kafka-event-to-reids
基于flink将kafka消息存到redis中

##flink-kafka-event-to-timerproducer
基于flink定时消息生成
消息如5分钟存盘信号、5分钟计算信号、日清零信号、日决策信号、小时告警信号

##flink-kafka-event-to-timercalc
基于flink的消费5分钟计算信号
数据来源：第一数据来源为读取redis数据；第二数据来源为清洗数据表；第三数据来源为基础数据表
最后将计算的结果存储到redis和清洗历史库中

##flink-kafka-event-to-timerdbsave
基于flink的消费5分钟存盘信号
数据来源：第一数据来源为读取redis数据；第二数据来源为清洗数据表；第三数据来源为基础数据表

##flink-kafka-event-to-timerdecision
基于flink的消费日决策信号信号
数据来源：第一数据来源为读取redis数据；第二数据来源为清洗数据表；第三数据来源为基础数据表
最后将计算的结果存储到redis和清洗历史库中

##架构图
![1603331277515](image\1603331277515.png)


