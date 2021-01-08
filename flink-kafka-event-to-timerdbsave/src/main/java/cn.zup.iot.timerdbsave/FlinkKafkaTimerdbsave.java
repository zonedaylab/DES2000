package cn.zup.iot.timerdbsave;
import cn.zup.iot.common.constant.PropertiesConstants;
import cn.zup.iot.common.model.Timer;
import cn.zup.iot.common.utils.ExecutionEnvUtil;
import org.apache.flink.api.common.serialization.TypeInformationSerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;

import java.util.Properties;
import static cn.zup.iot.common.utils.KafkaConfigUtil.buildKafkaProps;

/**
 * 基于flink定时消息生成的5分钟存盘信号,执行从redis取出数据后批量存到清洗表语句
 * 数据来源：第一数据来源为读取redis数据；第二数据来源为清洗数据表；第三数据来源为基础数据表
 * @author 史善力
 * @date 2020年12月23日14:10:17
 */
public class FlinkKafkaTimerdbsave{

    public static void main(String[] args) throws Exception {
        //获取所有参数
        final ParameterTool parameterTool = ExecutionEnvUtil.createParameterTool(args);
        //获得上下文环境
        StreamExecutionEnvironment env = ExecutionEnvUtil.prepare(parameterTool);
        env.setParallelism(1);
        Properties props = buildKafkaProps(parameterTool);
        //作为一个消费者从kafka特定topic中取信号
        FlinkKafkaConsumer011<Timer> consumer = new FlinkKafkaConsumer011<Timer>(props.getProperty(PropertiesConstants.KAFKA_TOPIC_ID),
                new TypeInformationSerializationSchema<Timer>(TypeInformation.of(Timer.class), env.getConfig()),
                props);
        SingleOutputStreamOperator<Timer> filterSource = env.addSource(consumer).filter(Timer -> {
            //每当kafka中有一个5分钟消息来到就把Redis数据批量存储到mysql
            if(Timer.getTimerType()==2){
                System.out.println("开始5分钟存盘");
                long t00 = System.currentTimeMillis();
                RedisBatchToMysql redisBatchToMysql = new RedisBatchToMysql();
                if(redisBatchToMysql.getRedisBatchToMysql(Timer.getTimerTime())){
                    System.out.println("存盘成功");
                    long t01 = System.currentTimeMillis();
                    System.out.println("共花费"+ (t01-t00)+"ms");
                }else {
                    System.out.println("Redis里没有数据");
                }
                return  true;
            }
            return false;
        });
        env.execute("flink kafka from Redis To Mysql");
    }

}
