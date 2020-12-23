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
 * Desc:
 * weixin: zhisheng_tian
 * blog: http://www.54tianzhisheng.cn/
 */
public class SinkToBaseData extends RichSinkFunction<DataEvent> {
    private DruidDataSource dataSourceLs;
    private Connection connectionLs;
    private PreparedStatement psLs;
    private Statement stmntLs;
    private ResultSet resultSetLs;
    private Properties properties;
    private JdbcTemplate jdbcTemplateLs;

    /**
     * open() 方法中建立连接，这样不用每次 invoke 的时候都要建立连接和释放连接
     *
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

    @Override
    public void close() throws Exception {
        super.close();
    }

    /**
     * 每条数据的插入都要调用一次 invoke() 方法
     *
     * @param value
     * @param context
     * @throws Exception
     */
    @Override
    public void invoke(DataEvent value, Context context) throws Exception {
        long t1 = System.currentTimeMillis();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(value.getEventTime()), TimeZone.getDefault().toZoneId());
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
        jdbcTemplateLs.execute(createBrandDatabase);
        String datatime = String.valueOf(year)+"-"+String.format("%02d",month)+"-"+String.format("%02d",day)+" "+String.format("%02d",hour)+"-"+String.format("%02d",minute)+"-"+String.format("%02d",second);
        insertBaseData(tablename,datatime,value);
        long t2 = System.currentTimeMillis();
        System.out.println("sink一次时间为："+(t2-t1));
    }

    public void insertBaseData(String tablename,String datatime,DataEvent value){
        StringBuffer sql1 = new StringBuffer();
        sql1.append(" insert DELAYED into "+tablename);
        sql1.append("(DataTime, BuJianLeiXingID, BuJianID, BuJianCanShuID, ChangZhanID,value,valueFlag) values(?,?,?,?,?,?,?)");
        System.out.println(sql1);
        jdbcTemplateLs.update(String.valueOf(sql1),new PreparedStatementSetter(){
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1,datatime);
                ps.setInt(2,value.getComponentType());
                ps.setInt(3,value.getComponentId());
                ps.setInt(4,value.getComponentParamId());
                ps.setInt(5,value.getStationId());
                ps.setDouble(6,Double.valueOf(value.getDataValue()));
                ps.setDouble(7,0);
            }
        });
    }

}