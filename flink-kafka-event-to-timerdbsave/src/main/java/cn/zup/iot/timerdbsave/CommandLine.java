package cn.zup.iot.timerdbsave;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CommandLine implements CommandLineRunner {
    @Autowired
    FlinkKafkaTimerdbsave flinkKafkaTimerdbsave;

    @Override
    public void run(String... args) throws Exception {
        flinkKafkaTimerdbsave.FlinkInit(args);
    }
}
