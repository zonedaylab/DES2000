package cn.zup.iot.timercalc;
import static cn.zup.iot.common.utils.KafkaConfigUtil.buildKafkaProps;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.Properties;

import akka.japi.tuple.Tuple3;
import cn.zup.iot.common.model.DataEvent;
import lombok.val;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.TypeInformationSerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.util.StringUtils;
import cn.zup.iot.common.constant.PropertiesConstants;
import cn.zup.iot.common.model.Timer;
import cn.zup.iot.common.utils.ExecutionEnvUtil;

public class FlinkKafkaTimercalc {

    public static void main(String[] args) throws Exception {
        //获取所有参数
        final ParameterTool parameterTool = ExecutionEnvUtil.createParameterTool(args);
        ////获得上下文环境
        StreamExecutionEnvironment env = ExecutionEnvUtil.prepare(parameterTool);
        env.setParallelism(1);
        // env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);
        //当checkpoint机制开启的时候，KafkaConsumer会定期把kafka的offset信息还有其他operator的状态信息一块保存起来。当job失败重启的时候，Flink会从最近一次的checkpoint中进行恢复数据，重新消费kafka中的数据。
        // env.enableCheckpointing(5000);
        Properties props = buildKafkaProps(parameterTool);
        props.put("group.id", "flink kafka To Mysql");

        FlinkKafkaConsumer011<Timer> consumer = new FlinkKafkaConsumer011<Timer>(props.getProperty(PropertiesConstants.KAFKA_TOPIC_ID),
                new TypeInformationSerializationSchema<Timer>(TypeInformation.of(Timer.class), env.getConfig()),
                props);

        //addSource - 添加一个新的 source function。例如，你可以 addSource(new FlinkKafkaConsumer011<>(…)) 以从 Apache Kafka 读取数据
        SingleOutputStreamOperator<Timer> filterSource = env.addSource(consumer).filter(Timer -> {
            if(Timer.getTimerType()==1){
                System.out.println("执行db清洗和计算服务");
                System.out.println(Timer.toString());
            }
            return false;
        });
        //4.输出
        // 控制台输出
        // filterSource.print();
        env.execute("flink kafka To Mysql");

    }

}
