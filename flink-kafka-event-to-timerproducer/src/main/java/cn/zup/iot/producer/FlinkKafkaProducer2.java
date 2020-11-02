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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FlinkKafkaProducer2 {
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
                    //相比于上两个方法，它有以下好处：
                    //        1.相比于Timer的单线程，它是通过线程池的方式来执行任务的
                    //        2.可以很灵活的去设定第一次执行任务delay时间
                    //        3.提供了良好的约定，以便设定执行的时间间隔
                    Runnable runnable = new Runnable() {
                        public void run() {
                                sourceContext.collect(DataEvent.builder()
                                        //使用指定的字符生成5位长度的随机字符串
                                        //.componentType(Integer.parseInt(RandomStringUtils.randomNumeric(3))).build())
                                        .componentType(Integer.parseInt(RandomStringUtils.randomNumeric(3)))
                                        .componentParamId(Integer.parseInt(RandomStringUtils.randomNumeric(2)))
                                        .componentId(id++)
                                        //.componentId(Integer.parseInt(RandomStringUtils.randomNumeric(5))).build())
                                        .stationId(Integer.parseInt(RandomStringUtils.randomNumeric(5)))
                                     //   .eventTime(LocalDateTime.now())
                                        .dataValue("111")
                                        .build());
                                //生成当前时间
                                //.eventTime(LocalDateTime.now())
                                //生成随机数字字符串 ,3位数的
                            }
                    };
                    // // 做为并发工具类被引进的，这是最理想的定时任务实现方式。
                     ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
                    //第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
                    // 10：秒   5：秒
                    // 第一次执行的时间为10秒，然后每隔五秒执行一次
                    service.scheduleAtFixedRate(runnable, 1, 5, TimeUnit.SECONDS);
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
