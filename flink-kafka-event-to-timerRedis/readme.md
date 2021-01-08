#### flink-kafka-event-to-reids

基于flink将kafka消息存到redis中

## 流程图
![1603331277515](..\image\flink-kafka-event-to-reids.png)

在flink环境下，将kafka topic中的数据消息作为数据源 将redis作为目的源，然后将kafka中的
每一条数据消息都sink到redis集群中