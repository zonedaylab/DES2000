package cn.zup.iot.cleandata;

import cn.zup.iot.common.constant.PropertiesConstants;
import cn.zup.iot.common.model.DataEvent;
import cn.zup.iot.common.utils.ExecutionEnvUtil;
import com.alibaba.fastjson.JSON;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;

import java.util.Properties;

import static cn.zup.iot.common.utils.KafkaConfigUtil.buildKafkaProps;


public class FlinkKafkaToCleanData {
    public static void main(String[] args) throws Exception {
        final ParameterTool parameterTool = ExecutionEnvUtil.createParameterTool(args);
        StreamExecutionEnvironment env = ExecutionEnvUtil.prepare(parameterTool);
        env.setParallelism(1);
        //env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        //当checkpoint机制开启的时候，KafkaConsumer会定期把kafka的offset信息还有其他operator的状态信息一块保存起来。当job失败重启的时候，Flink会从最近一次的checkpoint中进行恢复数据，重新消费kafka中的数据。
        env.enableCheckpointing(5000);
        Properties props = buildKafkaProps(parameterTool);
        props.put("group.id", "flink kafka To cleandata");

        SingleOutputStreamOperator<DataEvent> dataStreamSource = env.addSource(new FlinkKafkaConsumer011<>(
                props.getProperty(PropertiesConstants.KAFKA_TOPIC_ID),   //这个 kafka topic_id取的是application.properties
                new SimpleStringSchema(),
                props)).setParallelism(1)
                .map(string -> JSON.parseObject(string, DataEvent.class)); //Fastjson 解析字符串成 DataEvent 对象
        dataStreamSource.print();

        dataStreamSource.addSink(new SinkToCelanData()); //数据 sink 到 mysql

        env.execute("flink kafka To cleandata");
    }
}
