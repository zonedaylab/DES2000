package cn.zup.iot.timerdecision.util;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JdbcTemplateUtils {
    static {
        Properties properties = new Properties();
        ClassLoader classLoader = JdbcTemplateUtils.class.getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("application.properties");
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        DruidDataSource dataSourceMs = new DruidDataSource();
        dataSourceMs.setUrl(properties.getProperty("ms.url"));
        dataSourceMs.setUsername(properties.getProperty("ms.username"));
        dataSourceMs.setPassword(properties.getProperty("ms.passwd"));
        dataSourceMs.setDriverClassName(properties.getProperty("ms.driverClassName"));
        dataSourceMs.setInitialSize(Integer.valueOf(properties.getProperty("ms.initialSize")));
        dataSourceMs.setMinIdle(Integer.valueOf(properties.getProperty("ms.minIdle")));
        dataSourceMs.setMaxActive(Integer.valueOf(properties.getProperty("ms.maxActive")));
        dataSourceMs.setMaxWait(Integer.valueOf(properties.getProperty("ms.maxWait")));
        jdbcTemplateMs= new JdbcTemplate(dataSourceMs);
        DruidDataSource dataSourcePms = new DruidDataSource();
        dataSourcePms.setUrl(properties.getProperty("pms.url"));
        dataSourcePms.setUsername(properties.getProperty("pms.username"));
        dataSourcePms.setPassword(properties.getProperty("pms.passwd"));
        dataSourcePms.setDriverClassName(properties.getProperty("pms.driverClassName"));
        dataSourcePms.setInitialSize(Integer.valueOf(properties.getProperty("pms.initialSize")));
        dataSourcePms.setMinIdle(Integer.valueOf(properties.getProperty("pms.minIdle")));
        dataSourcePms.setMaxActive(Integer.valueOf(properties.getProperty("pms.maxActive")));
        dataSourcePms.setMaxWait(Integer.valueOf(properties.getProperty("pms.maxWait")));
        jdbcTemplatePms = new JdbcTemplate(dataSourcePms);
    }
    public static JdbcTemplate jdbcTemplateMs;
    public static JdbcTemplate jdbcTemplatePms;
}
