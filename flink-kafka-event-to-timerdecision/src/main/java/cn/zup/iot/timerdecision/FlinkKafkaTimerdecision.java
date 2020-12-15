package cn.zup.iot.timerdecision;

import cn.zup.iot.common.constant.PropertiesConstants;
import cn.zup.iot.common.model.Timer;
import cn.zup.iot.common.utils.ExecutionEnvUtil;
import cn.zup.iot.timerdecision.service.DecisionEngineService;
import org.apache.flink.api.common.serialization.TypeInformationSerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import java.util.Properties;
import static cn.zup.iot.common.utils.KafkaConfigUtil.buildKafkaProps;

public class FlinkKafkaTimerdecision  {
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
            //1小时信号
            if(Timer.getTimerType()==2||Timer.getTimerType()==3||Timer.getTimerType()==4){
                //System.out.println("从kafka中获取"+Timer.getTimerTime());
                DecisionEngineService decisionEngineService = new DecisionEngineService();
                decisionEngineService.allDeviceDecision(Timer.getTimerType());
                return true;
            }
            return false;
        });
        env.execute("flink kafka To Decision");
    }
}
