package cn.zup.iot.timercalc;
import static cn.zup.iot.common.utils.KafkaConfigUtil.buildKafkaProps;
import java.util.Properties;
import cn.zup.iot.timercalc.service.CCalThread;
import org.apache.flink.api.common.serialization.TypeInformationSerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import cn.zup.iot.common.constant.PropertiesConstants;
import cn.zup.iot.common.model.Timer;
import cn.zup.iot.common.utils.ExecutionEnvUtil;

/**
 * 基于flink的消费5分钟计算信号,进行计算
 * @author shishanli
 * @date 2021年1月3日22:45:07
 */
public class FlinkKafkaTimercalc {

    public static void main(String[] args) throws Exception {
        //获取所有参数
        final ParameterTool parameterTool = ExecutionEnvUtil.createParameterTool(args);
        ////获得上下文环境
        StreamExecutionEnvironment env = ExecutionEnvUtil.prepare(parameterTool);
        env.setParallelism(1);
        Properties props = buildKafkaProps(parameterTool);
        //从kafka里获取定时信号
        FlinkKafkaConsumer011<Timer> consumer = new FlinkKafkaConsumer011<Timer>(props.getProperty(PropertiesConstants.KAFKA_TOPIC_ID),
                new TypeInformationSerializationSchema<Timer>(TypeInformation.of(Timer.class), env.getConfig()),
                props);
        SingleOutputStreamOperator<Timer> filterSource = env.addSource(consumer).filter(Timer -> {
            //过滤出信号为3，即5分钟信号
            if(Timer.getTimerType()==1){
                System.out.println("开始计算服务");
                long t1 = System.currentTimeMillis();
                CCalThread cCalThread = new CCalThread();
                cCalThread.timingCalc(Timer.getTimerTime());
                long t2 = System.currentTimeMillis();
                System.out.println("计算服务共运行"+(t2-t1));
                return  true;
            }
            return false;
        });
        env.execute("flink kafka To Redis and mysql");
    }

}
