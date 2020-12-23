package cn.zup.iot.timercalc.util;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisUtil {
    static {
        JedisPoolConfig config = new JedisPoolConfig(); // Jedis连接池
        config.setMaxIdle(10); // 最大空闲连接数
        config.setMaxTotal(20);// 最大连接数
        config.setMaxWaitMillis(1000); // 获取连接是的最大等待时间，如果超时就抛出异常
        config.setTestOnBorrow(false);// 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
        config.setTestOnReturn(true);
        jedisPool = new JedisPool(config, "127.0.0.1", 6379, 500); // 配置、ip、端口、连接超时时间、密码、数据库编号（0~15）
    }
    public static JedisPool jedisPool;


}
