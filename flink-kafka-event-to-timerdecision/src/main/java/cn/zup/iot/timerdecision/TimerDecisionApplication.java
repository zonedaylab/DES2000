package cn.zup.iot.timerdecision;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TimerDecisionApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimerDecisionApplication.class, args);
    }

}
