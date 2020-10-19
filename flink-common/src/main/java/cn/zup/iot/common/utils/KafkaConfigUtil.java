package cn.zup.iot.common.utils;


import cn.zup.iot.common.constant.PropertiesConstants;
import org.apache.flink.api.java.utils.ParameterTool;

import java.util.Properties;

import static cn.zup.iot.common.constant.PropertiesConstants.*;


public class KafkaConfigUtil {

    /**
     * 设置基础的 Kafka 配置
     *
     * @return
     */
    public static Properties buildKafkaProps() {
        return buildKafkaProps(ParameterTool.fromSystemProperties());
    }

    /**
     * 设置 kafka 配置
     *
     * @param parameterTool
     * @return
     */
    public static Properties buildKafkaProps(ParameterTool parameterTool) {
        Properties props = parameterTool.getProperties();
        props.put("bootstrap.servers", parameterTool.get(PropertiesConstants.KAFKA_BROKERS, DEFAULT_KAFKA_BROKERS));
        props.put("zookeeper.connect", parameterTool.get(PropertiesConstants.KAFKA_ZOOKEEPER_CONNECT, DEFAULT_KAFKA_ZOOKEEPER_CONNECT));
        props.put("kafka.topic.id", parameterTool.get(PropertiesConstants.KAFKA_TOPIC_ID, DEFAULT_KAFKA_TOPIC_ID));
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "latest");
        return props;
    }

}
