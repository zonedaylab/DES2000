## flink-kafka-event-to-basedata
基于flink将kafka数据消息持久化到mysql数据库基础表中 kafka消息为entity对象

## 流程图
![1603331277515](..\image\flink-kafka-event-to-basedata.png)

代码步骤：
1. kafka中每来一条数据消息，就调用invoke()
2. 先判断数据库基础表是否存在，不存在就创建该表
3. 将数据消息持久化到数据库基础表中


