package cn.zup.iot.producer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import cn.zup.iot.common.model.DataEvent;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.flink.api.common.serialization.TypeInformationSerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import cn.zup.iot.common.constant.PropertiesConstants;
import cn.zup.iot.common.model.Timer;
import cn.zup.iot.common.utils.ExecutionEnvUtil;
import cn.zup.iot.common.utils.KafkaConfigUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlinkKafkaProducer {
    //创建新的随机数生成器
    public static final Random random = new Random();
    public  static  int  id=1;
    public static void main(String[] args) throws Exception,InterruptedException{
        //获取所有参数
        final ParameterTool parameterTool = ExecutionEnvUtil.createParameterTool(args);
        //获得上下文环境
        StreamExecutionEnvironment env = ExecutionEnvUtil.prepare(parameterTool);
        //设置基本的kafka配置
        Properties props = KafkaConfigUtil.buildKafkaProps(parameterTool);
        env.setParallelism(1);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12); // 控制时
        calendar.set(Calendar.MINUTE, 0);    // 控制分
        calendar.set(Calendar.SECOND, 0);    // 控制秒
        //Flink 中你可以使用 StreamExecutionEnvironment.addSource(sourceFunction) 来为你的程序添加数据来源。
        DataStreamSource<Timer> dataStreamSource = env.addSource(new SourceFunction<Timer>() {
            @Override
            public void run(SourceContext<Timer> sourceContext) throws Exception {
                while(true) {
                                Thread.sleep(1000);
                                Long second = LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8"));
                                if(second%60==0){
                                    sourceContext.collect(Timer.builder()
                                            .timerType(3)
                                            .timerTime(LocalDateTime.now())
                                            .timerName("1分钟存盘")
                                            .build());
                                }
                                if(second%300==0){
                                    sourceContext.collect(Timer.builder()
                                            .timerType(1)
                                            .timerTime(LocalDateTime.now())
                                            .timerName("5分钟存盘")
                                            .build());
                                    System.out.println(LocalDateTime.now());
                                }
                                if(second%(60*60)==0){
                                    sourceContext.collect(Timer.builder()
                                            .timerType(2)
                                            .timerTime(LocalDateTime.now())
                                            .timerName("1小时存盘")
                                            .build());
                                }
                                if(second%(60*60)*24==0){
                                    sourceContext.collect(Timer.builder()
                                            .timerType(5)
                                            .timerTime(LocalDateTime.now())
                                            .timerName("1天存盘")
                                            .build());
                                }
                }
            }
            @Override
            public void cancel() {}

            });
        //sink 有点把数据存储到kafka的意思，下面有kafka的地址和topicic
        dataStreamSource.addSink(new FlinkKafkaProducer011<Timer>(
            //KAFKA_BROKERS = "kafka.brokers";"localhost:9092"
            props.getProperty(PropertiesConstants.KAFKA_BROKERS),
            // DEFAULT_KAFKA_TOPIC_ID = "event_test";kafka.topic.id=event_test
            props.getProperty(PropertiesConstants.KAFKA_TOPIC_ID),
            new TypeInformationSerializationSchema<Timer>(TypeInformation.of(Timer.class), env.getConfig()))).name("flink-kafka-random-event-Producer");
        dataStreamSource.print();
        try {
            env.execute("flink-kafka-random-event-Producer");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
