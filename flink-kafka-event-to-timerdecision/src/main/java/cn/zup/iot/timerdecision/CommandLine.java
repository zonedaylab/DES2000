package cn.zup.iot.timerdecision;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CommandLine implements CommandLineRunner {

    @Autowired(required=false)
    FlinkKafkaTimerdecision flinkKafkaTimerdecision;

    @Override
    public void run(String... args) throws Exception {
        flinkKafkaTimerdecision.FlinkInit(args);
    }
}
