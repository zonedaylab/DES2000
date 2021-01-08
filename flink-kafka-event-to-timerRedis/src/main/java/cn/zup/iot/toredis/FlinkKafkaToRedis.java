package cn.zup.iot.toredis;

import cn.zup.iot.common.constant.PropertiesConstants;
import cn.zup.iot.common.model.DataEvent;
import cn.zup.iot.common.utils.ExecutionEnvUtil;
import cn.zup.iot.toredis.sinks.RedisSinkMapper;
import com.alibaba.fastjson.JSON;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static cn.zup.iot.common.utils.KafkaConfigUtil.buildKafkaProps;

/**
 * 基于flink将kafka消息存到redis中
 * @Author 史善力
 * @date 2020年12月23日19:10:20
 */
public class FlinkKafkaToRedis {

    public static void main(String[] args) throws Exception {
        //获取所有参数
        final ParameterTool parameterTool = ExecutionEnvUtil.createParameterTool(args);
        ////获得上下文环境
        StreamExecutionEnvironment env = ExecutionEnvUtil.prepare(parameterTool);
        env.setParallelism(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        env.enableCheckpointing(5000);
        Properties props = buildKafkaProps(parameterTool);
        props.put("group.id", "flink kafka To redis");

        SingleOutputStreamOperator<DataEvent> dataStreamSource = env.addSource(new FlinkKafkaConsumer011<>(
                props.getProperty(PropertiesConstants.KAFKA_TOPIC_ID),
                new SimpleStringSchema(),
                props)).setParallelism(1)
                ////Fastjson 解析字符串成 DataEvent 对象
                .map(string -> JSON.parseObject(string, DataEvent.class));
        dataStreamSource.print();
        Properties properties = new Properties();
        ClassLoader classLoader = FlinkKafkaToRedis.class.getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("application.properties");
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder().setHost(properties.getProperty("redis.host")).setPort(Integer.parseInt(properties.getProperty("redis.port"))).setPassword(properties.getProperty("redis.passwd")).build();
        dataStreamSource.print();
        //sink到redis中
        dataStreamSource.addSink(new RedisSink<DataEvent>(conf, new RedisSinkMapper()));
        env.execute("flink kafka To Redis");

    }

}
