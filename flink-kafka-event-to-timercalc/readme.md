#### flink-kafka-event-to-timercalc

基于flink的消费5分钟计算信号 数据来源：第一数据来源为读取redis数据；第二数据来源为清洗数据表；第三数据来源为基础数据表 最后将计算的结果存储到redis和清洗历史库中

## 流程图
![1603331277515](..\image\flink-kafka-event-to-timercalc.png)