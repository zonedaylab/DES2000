package cn.zup.iot.basedata;

import cn.zup.iot.cleandata.SinkToCelanData;
import cn.zup.iot.common.constant.PropertiesConstants;
import cn.zup.iot.common.model.DataEvent;
import cn.zup.iot.common.utils.ExecutionEnvUtil;
import com.alibaba.fastjson.JSON;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;

import java.util.Properties;

import static cn.zup.iot.common.utils.KafkaConfigUtil.buildKafkaProps;


public class FlinkKafkaToBaseData {
    public static void main(String[] args) throws Exception {
        final ParameterTool parameterTool = ExecutionEnvUtil.createParameterTool(args);
        StreamExecutionEnvironment env = ExecutionEnvUtil.prepare(parameterTool);
        env.enableCheckpointing(50);
        Properties props = buildKafkaProps(parameterTool);
        props.put("group.id", "flink kafka To cleandata");
        SingleOutputStreamOperator<DataEvent> dataStreamSource = env.addSource(new FlinkKafkaConsumer011<>(
                props.getProperty(PropertiesConstants.KAFKA_TOPIC_ID),   //这个 kafka topic 需要和上面的工具类的 topic 一致
                new SimpleStringSchema(),
                props)).setParallelism(1)
                .map(string -> JSON.parseObject(string, DataEvent.class)); //Fastjson 解析字符串成 DataEvent 对象

        dataStreamSource.print();
        dataStreamSource.addSink(new SinkToCelanData()); //数据 sink 到 mysql



        env.execute("flink kafka To cleandata");
    }
}
