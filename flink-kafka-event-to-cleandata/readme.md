## flink-kafka-event-to-cleandata
基于flink将kafka数据消息持久化到清洗mysql数据库清洗表中 
kafka消息为entity对象

## 流程图
![1603331277515](..\image\flink-kafka-event-to-cleandata.png)

代码步骤：
1. 将bujiancanshu表中的数据存入hashMap中，其中bjlxID.ID作为键，DType作为值
2. 利用ComponentType.ComponentId拼接的键取hashMap的值，如果值为空，就什么也不执行，如果值=4，则令table=ycdata表，
如果值等于2，则令table=kwhdata表，其余的都令table=kwhdata表（只是为了测试）
3. 判断该表是否存在，如果不存在就创建表
4. 判断表中是否有该条数据，如果没有就插入，有就更新

