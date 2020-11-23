package cn.zup.iot.timerdecision.util;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

public class DataSourceUtils {
    public    DataSource getDataSource1(){
        //jdbcTemplate_mc
        DruidDataSource dataSource1 = new DruidDataSource();
        dataSource1.setUrl("jdbc:mysql://localhost:3306/ms?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC");
        dataSource1.setUsername("root");
        dataSource1.setPassword("123456");
        dataSource1.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource1.setInitialSize(20);
        dataSource1.setMinIdle(3);
        dataSource1.setMaxActive(30);
        dataSource1.setMaxWait(60000);
        dataSource1.setTimeBetweenEvictionRunsMillis(60000);
        dataSource1.setMinEvictableIdleTimeMillis(30000);
        dataSource1.setValidationQuery("validationQuery: select 'x'");
        dataSource1.setTestWhileIdle(true);
        dataSource1.setTestOnBorrow(false);
        dataSource1.setTestOnReturn(false);
        dataSource1.setPoolPreparedStatements(true);
        dataSource1.setMaxPoolPreparedStatementPerConnectionSize(20);
        return dataSource1;
    };
    public DataSource getDataSource2(){
        //bpmJdbcTemplate_mc
        DruidDataSource dataSource2 = new DruidDataSource();
        dataSource2.setUrl("jdbc:mysql://localhost:3306/pms?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC");
        dataSource2.setUsername("root");
        dataSource2.setPassword("123456");
        dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource2.setInitialSize(20);
        dataSource2.setMinIdle(3);
        dataSource2.setMaxActive(30);
        dataSource2.setMaxWait(60000);
        dataSource2.setTimeBetweenEvictionRunsMillis(60000);
        dataSource2.setMinEvictableIdleTimeMillis(30000);
        dataSource2.setValidationQuery("validationQuery: select 'x'");
        dataSource2.setTestWhileIdle(true);
        dataSource2.setTestOnBorrow(false);
        dataSource2.setTestOnReturn(false);
        dataSource2.setPoolPreparedStatements(true);
        dataSource2.setMaxPoolPreparedStatementPerConnectionSize(20);
        return dataSource2;
    };
}
