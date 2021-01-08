package cn.zup.iot.timercalc.util;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 对jedisPool初始化
 * @author shishanli
 * @date 2021-1-4 00:17:05
 */
public class JedisUtil {
    static {
        Properties properties = new Properties();
        ClassLoader classLoader = JdbcTemplateUtils.class.getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("application.properties");
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(10);
        config.setMaxTotal(20);
        config.setMaxWaitMillis(1000);
        config.setTestOnBorrow(false);
        config.setTestOnReturn(true);
        // 配置、ip、端口、连接超时时间、密码、数据库编号（0~15）
        jedisPool = new JedisPool(config, properties.getProperty("redis.host"), Integer.parseInt(properties.getProperty("redis.port")),Integer.parseInt(properties.getProperty("redis.timeout")), properties.getProperty("redis.passwd"));
    }
    public static JedisPool jedisPool;


}
