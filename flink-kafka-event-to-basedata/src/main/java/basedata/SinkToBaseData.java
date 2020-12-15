package basedata;

import cn.zup.iot.common.model.DataEvent;
import com.alibaba.druid.pool.DruidDataSource;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

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
        String createBrandDatabase = "CREATE TABLE "
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
        String checkTable="show tables like "+"'"+tablename+"'";
        connectionLs = dataSourceLs.getConnection();
        stmntLs = connectionLs.createStatement();
        resultSetLs =stmntLs.executeQuery(checkTable);
        if(resultSetLs.next()==false) {
            if(stmntLs.executeUpdate(createBrandDatabase)==0){
                System.out.println("create table success!");
            }
        }
        try {
            String datatime = String.valueOf(year)+"-"+String.format("%02d",month)+"-"+String.format("%02d",day)+" "+String.format("%02d",hour)+"-"+String.format("%02d",minute)+"-"+String.format("%02d",second);
//            String sql1 = "select DataTime from "+tablename+" where DataTime="+"'"+datatime+"'"+" and BuJianLeiXingID="+value.getComponentType()+" and BuJianID="+value.getComponentId()+" and BuJianCanShuID="+value.getComponentParamId()+" and ChangZhanID="+value.getStationId();
//            psLs =  connectionLs.prepareStatement(sql1);
//            resultSetLs = psLs.executeQuery();
//            //如果查询到有此数据，就更新
//            if(resultSetLs.next()==true){
//                String sql = "update "+tablename+" set "+value+"=?"+ " where DataTime="+"'"+datatime+"'"+ " and BuJianLeiXingID="+value.getComponentType()+" and BuJianID="+value.getComponentId()+" and BuJianCanShuID="+value.getComponentParamId()+" and ChangZhanID="+value.getStationId();
//                System.out.println(sql);
//                psLs =  connectionLs.prepareStatement(sql);
//                psLs.setDouble(1,Double.valueOf(value.getDataValue()));
//                psLs.executeUpdate();
//            }else{
//                long t9 = System.currentTimeMillis();
            String sql = "insert DELAYED into "+tablename+" (DataTime, BuJianLeiXingID, BuJianID, BuJianCanShuID, ChangZhanID,value,valueFlag) values(?,?,?,?,?,?,?)";
            System.out.println(sql);
            psLs =  connectionLs.prepareStatement(sql);
            psLs.setString(1,datatime);
            psLs.setInt(2,value.getComponentType());
            psLs.setInt(3,value.getComponentId());
            psLs.setInt(4,value.getComponentParamId());
            psLs.setInt(5,value.getStationId());
            psLs.setDouble(6,Double.valueOf(value.getDataValue()));
            psLs.setDouble(7,0);
            psLs.executeUpdate();
//            }
            psLs.close();
            resultSetLs.close();
            stmntLs.close();
            connectionLs.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        long t2 = System.currentTimeMillis();
        System.out.println("sink一次时间为："+(t2-t1));
    }

    private static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?useSSL=false&useUnicode=true&characterEncoding=UTF8&serverTimezone=GMT", "root", "iesapp");
        } catch (Exception e) {
            System.out.println("-----------mysql get connection has exception , msg = "+ e.getMessage());
        }
        return con;
    }
}