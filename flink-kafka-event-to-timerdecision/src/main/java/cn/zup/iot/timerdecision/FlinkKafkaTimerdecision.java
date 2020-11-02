package cn.zup.iot.timerdecision;

import cn.zup.iot.common.constant.PropertiesConstants;
import cn.zup.iot.common.model.Timer;
import cn.zup.iot.common.constant.Signal;
import cn.zup.iot.common.utils.ExecutionEnvUtil;
import org.apache.flink.api.common.serialization.TypeInformationSerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Properties;

import static cn.zup.iot.common.utils.KafkaConfigUtil.buildKafkaProps;

public class FlinkKafkaTimerdecision {
    public static void main(String[] args) throws Exception {
        //获取所有参数
        final ParameterTool parameterTool = ExecutionEnvUtil.createParameterTool(args);
        ////获得上下文环境
        StreamExecutionEnvironment env = ExecutionEnvUtil.prepare(parameterTool);
        env.setParallelism(1);
        Properties props = buildKafkaProps(parameterTool);
        props.put("group.id", "flink kafka To Mysql");
        FlinkKafkaConsumer011<Timer> consumer = new FlinkKafkaConsumer011<Timer>(props.getProperty(PropertiesConstants.KAFKA_TOPIC_ID),
                new TypeInformationSerializationSchema<Timer>(TypeInformation.of(Timer.class), env.getConfig()),
                props);
        //当信号为2时
        SingleOutputStreamOperator<Timer> filterSource = env.addSource(consumer).filter(Timer -> {
            if(Timer.getTimerType()==Signal.Five_Minute_Signal){
                //flink从redis中获取数据作为source源
                System.out.println("五分钟存盘");
                LocalDateTime localDateTime = LocalDateTime.now();
                if(GetRedisToMysql.GetRedis(localDateTime)){

                }else {
                    System.out.println("Redis里没有数据");
                }
            }
            return false;
        });
        env.execute("flink kafka To Mysql");
    }

}
