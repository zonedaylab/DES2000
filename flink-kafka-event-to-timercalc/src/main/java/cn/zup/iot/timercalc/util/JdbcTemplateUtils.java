package cn.zup.iot.timercalc.util;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 完成了druid数据源的初始化并给jdbcTemplate赋值
 * @author shishanli
 * @date 2021年1月4日00:04:18
 */
public class JdbcTemplateUtils {
    public static DruidDataSource dataSourceMs;
    public static DruidDataSource dataSourcePms;
    public static DruidDataSource dataSourceLs;
    static {
        Properties properties = new Properties();
        ClassLoader classLoader = JdbcTemplateUtils.class.getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("application.properties");
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataSourceMs = new DruidDataSource();
        dataSourceMs.setUrl(properties.getProperty("ms.url"));
        dataSourceMs.setUsername(properties.getProperty("ms.username"));
        dataSourceMs.setPassword(properties.getProperty("ms.passwd"));
        dataSourceMs.setDriverClassName(properties.getProperty("ms.driverClassName"));
        dataSourceMs.setInitialSize(Integer.valueOf(properties.getProperty("ms.initialSize")));
        dataSourceMs.setMinIdle(Integer.valueOf(properties.getProperty("ms.minIdle")));
        dataSourceMs.setMaxActive(Integer.valueOf(properties.getProperty("ms.maxActive")));
        dataSourceMs.setMaxWait(Integer.valueOf(properties.getProperty("ms.maxWait")));
        jdbcTemplateMs= new JdbcTemplate(dataSourceMs);
        dataSourcePms = new DruidDataSource();
        dataSourcePms.setUrl(properties.getProperty("pms.url"));
        dataSourcePms.setUsername(properties.getProperty("pms.username"));
        dataSourcePms.setPassword(properties.getProperty("pms.passwd"));
        dataSourcePms.setDriverClassName(properties.getProperty("pms.driverClassName"));
        dataSourcePms.setInitialSize(Integer.valueOf(properties.getProperty("pms.initialSize")));
        dataSourcePms.setMinIdle(Integer.valueOf(properties.getProperty("pms.minIdle")));
        dataSourcePms.setMaxActive(Integer.valueOf(properties.getProperty("pms.maxActive")));
        dataSourcePms.setMaxWait(Integer.valueOf(properties.getProperty("pms.maxWait")));
        jdbcTemplatePms = new JdbcTemplate(dataSourcePms);
        dataSourceLs = new DruidDataSource();
        dataSourceLs.setUrl(properties.getProperty("ls.url"));
        dataSourceLs.setUsername(properties.getProperty("ls.username"));
        dataSourceLs.setPassword(properties.getProperty("ls.passwd"));
        dataSourceLs.setDriverClassName(properties.getProperty("ls.driverClassName"));
        dataSourceLs.setInitialSize(Integer.valueOf(properties.getProperty("ls.initialSize")));
        dataSourceLs.setMinIdle(Integer.valueOf(properties.getProperty("ls.minIdle")));
        dataSourceLs.setMaxActive(Integer.valueOf(properties.getProperty("ls.maxActive")));
        dataSourceLs.setMaxWait(Integer.valueOf(properties.getProperty("ls.maxWait")));
        jdbcTemplateLs = new JdbcTemplate(dataSourceLs);
    }
    public static JdbcTemplate jdbcTemplateMs;
    public static JdbcTemplate jdbcTemplatePms;
    public static JdbcTemplate jdbcTemplateLs;

}
