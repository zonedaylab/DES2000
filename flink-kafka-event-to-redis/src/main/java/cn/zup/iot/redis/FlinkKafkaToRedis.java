package cn.zup.iot.redis;

import cn.zup.iot.common.constant.PropertiesConstants;
import cn.zup.iot.common.model.DataEvent;
import cn.zup.iot.common.utils.ExecutionEnvUtil;
import cn.zup.iot.common.utils.KafkaConfigUtil;
import cn.zup.iot.redis.sinks.RedisSinkMapper;
import org.apache.flink.api.common.serialization.TypeInformationSerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;

import java.util.Properties;

public class FlinkKafkaToRedis {

    public static void main(String[] args) throws Exception {
        final ParameterTool parameterTool = ExecutionEnvUtil.createParameterTool(args);
        StreamExecutionEnvironment env = ExecutionEnvUtil.prepare(parameterTool);
        env.setParallelism(1);
        Properties props = KafkaConfigUtil.buildKafkaProps(parameterTool);
        props.put("group.id", "flink kafka To Redis");

        FlinkKafkaConsumer011<DataEvent> consumer = new FlinkKafkaConsumer011<DataEvent>(props.getProperty(PropertiesConstants.KAFKA_TOPIC_ID),
                new TypeInformationSerializationSchema<DataEvent>(TypeInformation.of(DataEvent.class),env.getConfig()), props);

        //单节点 Redis
        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder().setHost(parameterTool.get("redis.host")).build();

        env.addSource(consumer).addSink(new RedisSink(conf, new RedisSinkMapper())).setParallelism(parameterTool.getInt(PropertiesConstants.STREAM_SINK_PARALLELISM, 1));

        env.execute("flink kafka To Redis");
    }


}
