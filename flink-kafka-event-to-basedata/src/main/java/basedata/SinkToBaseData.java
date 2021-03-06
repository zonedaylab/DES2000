package basedata;

import cn.zup.iot.common.model.DataEvent;
import com.alibaba.druid.pool.DruidDataSource;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.TimeZone;

/**
 * sink到基础表中
 * @author shishanli
 * @date 2021年1月3日20:15:53
 */
public class SinkToBaseData extends RichSinkFunction<DataEvent> {
    private DruidDataSource dataSourceLs;
    private Properties properties;
    private JdbcTemplate jdbcTemplateLs;

    /**
     * open() 方法中建立连接，这样不用每次 invoke 的时候都要建立连接和释放连接，每次运行时建立一次
     * @author 2021年1月5日00:42:57
     * @date 2021年1月5日00:43:42
     * @param parameters
     * @throws Exception
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        properties = new Properties();
        ClassLoader classLoader = SinkToBaseData.class.getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("application.properties");
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    /**
     * close()关闭连接
     * @author shishanli
     * @date 2021年1月5日00:44:26
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        super.close();
    }

    /**
     * kafka中有一条数据就会调用一次invoke()方法
     * @author shishanli
     * @date 2021年1月5日00:45:40
     * @param dataEvent
     * @param context
     * @throws Exception
     */
    @Override
    public void invoke(DataEvent dataEvent, Context context) throws Exception {
        long t1 = System.currentTimeMillis();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(dataEvent.getEventTime()), TimeZone.getDefault().toZoneId());
        int year = localDateTime.getYear();
        int month = localDateTime.getMonthValue();
        int day = localDateTime.getDayOfMonth();
        int hour = localDateTime.getHour();
        int minute = localDateTime.getMinute();
        int second = localDateTime.getSecond();
        String tablename = "basedata"+String.valueOf(year)+String.format("%02d",month);
        String createBrandDatabase = "CREATE TABLE IF NOT EXISTS "
                + tablename
                + "( DataTime datetime NOT NULL,"
                + "BuJianLeiXingID int(11) NOT NULL,"
                + "BuJianID int(11) NOT NULL,"
                + "BuJianCanShuID int(11) NOT NULL,"
                + "ChangZhanID int(11) NOT NULL,"
                + "value double DEFAULT NULL,"
                + "valueFlag int(11) DEFAULT NULL,"
                + "PRIMARY KEY (DataTime,BuJianLeiXingID,BuJianID,BuJianCanShuID,ChangZhanID)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
        //如果不存在该表就创建该表
        jdbcTemplateLs.execute(createBrandDatabase);
        String datatime = String.valueOf(year)+"-"+String.format("%02d",month)+"-"+String.format("%02d",day)+" "+String.format("%02d",hour)+"-"+String.format("%02d",minute)+"-"+String.format("%02d",second);
        insertBaseData(tablename,datatime,dataEvent);
        long t2 = System.currentTimeMillis();
        System.out.println("sink一次时间为："+(t2-t1));
    }

    /**
     * 将传来的数据插入到基础表中
     * @author shishanli
     * @date 2021年1月5日00:46:40
     * @param tablename
     * @param datatime
     * @param dataEvent
     */
    public void insertBaseData(String tablename,String datatime,DataEvent dataEvent){
        StringBuffer sql1 = new StringBuffer();
        sql1.append(" insert DELAYED into "+tablename);
        sql1.append("(DataTime, BuJianLeiXingID, BuJianID, BuJianCanShuID, ChangZhanID,value,valueFlag) values(?,?,?,?,?,?,?)");
        System.out.println(sql1);
        jdbcTemplateLs.update(String.valueOf(sql1),new PreparedStatementSetter(){
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1,datatime);
                ps.setInt(2,dataEvent.getComponentType());
                ps.setInt(3,dataEvent.getComponentId());
                ps.setInt(4,dataEvent.getComponentParamId());
                ps.setInt(5,dataEvent.getStationId());
                ps.setDouble(6,Double.valueOf(dataEvent.getDataValue()));
                ps.setDouble(7,0);
            }
        });
    }

}