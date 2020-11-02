package cn.zup.iot.producer;

import cn.zup.iot.common.constant.PropertiesConstants;
import cn.zup.iot.common.model.DataEvent;
import cn.zup.iot.common.utils.ExecutionEnvUtil;
import cn.zup.iot.common.utils.KafkaConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.flink.api.common.serialization.TypeInformationSerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class FlinkKafkaProducer1 {
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
        DataStreamSource<DataEvent> dataStreamSource = env.addSource(new SourceFunction<DataEvent>() {
            @Override
            public void run(SourceContext<DataEvent> sourceContext) throws Exception {
              //  while (true) {
                    Date time = calendar.getTime();     // 得出执行任务的时间,此处为今天的12：00：00
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        public void run() {
                            //    sourceContext.collect(Timer.builder()
                              //          .timerType(1)
                               //         .timerTime(LocalDateTime.now())
                                //        .timerName("10秒存盘")
                                 //       .build());
                                //生成当前时间
                                //.eventTime(LocalDateTime.now())
                                //生成随机数字字符串 ,3位数的

                        }
                    }, 0, 5000);
                // Thread.sleep(10000);

                //}
            }
            @Override
            public void cancel() {}

            });
        //sink 有点把数据存储到kafka的意思，下面有kafka的地址和topicic
        dataStreamSource.addSink(new FlinkKafkaProducer011<DataEvent>(
            //KAFKA_BROKERS = "kafka.brokers";"localhost:9092"
            props.getProperty(PropertiesConstants.KAFKA_BROKERS),
            // DEFAULT_KAFKA_TOPIC_ID = "event_test";kafka.topic.id=event_test
            props.getProperty(PropertiesConstants.KAFKA_TOPIC_ID),
            new TypeInformationSerializationSchema<DataEvent>(TypeInformation.of(DataEvent.class), env.getConfig()))).name("flink-kafka-random-event-Producer");
        dataStreamSource.print();
        try {
            env.execute("flink-kafka-random-event-Producer");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
