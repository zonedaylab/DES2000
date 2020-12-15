package cn.zup.iot.timerdbsave;

import cn.zup.iot.common.constant.PropertiesConstants;
import com.alibaba.druid.pool.DruidDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class GetRedisToMysql{

    private DruidDataSource dataSource;
    private Connection conn = null;
    private Statement stmt = null;
    private ResultSet resultSet =null;
    private PreparedStatement ps =null;
    private ResultSet  rs =null;
    private Properties properties = null;
    static  Logger logger = LoggerFactory.getLogger(GetRedisToMysql.class);
    static  final long SLEEP_MILLION=5000;
    public  void DatadataSourceConfig(){
        dataSource = new DruidDataSource();
        properties = new Properties();
        ClassLoader classLoader = BatchToMysql.class.getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("application.properties");
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataSource.setUrl("jdbc:mysql://localhost:3306/ls?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setInitialSize(20);
        dataSource.setMinIdle(3);
        dataSource.setMaxActive(20);
        dataSource.setMaxWait(60000);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(30000);
        dataSource.setValidationQuery("select 'x'");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
    };
    public   boolean GetRedis(LocalDateTime localDateTime){
        DatadataSourceConfig();
        Jedis jedis =null;
        jedis=new Jedis(properties.getProperty("redis.host"), Integer.parseInt(properties.getProperty("redis.port")));
//        jedis.auth(PropertiesConstants.REDIS_PASSWD);
        try{
            //获取redis中所有的键
            Set<String> keys = jedis.keys("*");
            if(keys.isEmpty()){
                return false;
            }
            System.out.println("redis共取出数据"+keys.size()+"条");
            Iterator<String> it = keys.iterator();
            int year = localDateTime.getYear();
            int month = localDateTime.getMonthValue();
            //创建数据库
            String tablename = "kwhdata"+String.valueOf(year)+String.format("%02d",month);
            String createBrandDatabase = "CREATE TABLE "
                    + tablename
                    + "( RiQi datetime NOT NULL DEFAULT '1970-02-01 00:00:00',"
                    + "BuJianLeiXingID tinyint(3) unsigned NOT NULL DEFAULT '0',"
                    + "BuJianID int(10) unsigned NOT NULL DEFAULT '0',"
                    + "BuJianCanShuID tinyint(3) unsigned NOT NULL DEFAULT '0',"
                    + "ChangZhanID int(10) unsigned NOT NULL DEFAULT '0',"
                    + "H0 double NOT NULL DEFAULT '0',"
                    + "H0Flag tinyint(3) unsigned NOT NULL DEFAULT '0',"
                    + "H1 double NOT NULL DEFAULT '0',"
                    + "H1Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H2 double NOT NULL DEFAULT '0', H2Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H3 double NOT NULL DEFAULT '0', H3Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H4 double NOT NULL DEFAULT '0', H4Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H5 double NOT NULL DEFAULT '0', H5Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H6 double NOT NULL DEFAULT '0', H6Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H7 double NOT NULL DEFAULT '0', H7Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H8 double NOT NULL DEFAULT '0', H8Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H9 double NOT NULL DEFAULT '0', H9Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H10 double NOT NULL DEFAULT '0', H10Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H11 double NOT NULL DEFAULT '0', H11Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H12 double NOT NULL DEFAULT '0', H12Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H13 double NOT NULL DEFAULT '0', H13Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H14 double NOT NULL DEFAULT '0', H14Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H15 double NOT NULL DEFAULT '0', H15Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H16 double NOT NULL DEFAULT '0', H16Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H17 double NOT NULL DEFAULT '0', H17Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H18 double NOT NULL DEFAULT '0', H18Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H19 double NOT NULL DEFAULT '0', H19Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H20 double NOT NULL DEFAULT '0', H20Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H21 double NOT NULL DEFAULT '0', H21Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H22 double NOT NULL DEFAULT '0', H22Flag tinyint(3) unsigned NOT NULL DEFAULT '0', H23 double NOT NULL DEFAULT '0', H23Flag tinyint(3) unsigned NOT NULL DEFAULT '0', PRIMARY KEY (RiQi,BuJianLeiXingID,BuJianID,BuJianCanShuID,ChangZhanID)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
            String checkTable="show tables like "+"'"+tablename+"'";
            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            resultSet =stmt.executeQuery(checkTable);
            if(resultSet.next()==false) {
                if(stmt.executeUpdate(createBrandDatabase)==0){
                    System.out.println("create table success!");
                }
            }
            resultSet.close();
            stmt.close();
            conn.close();
            while (it.hasNext()) {
                String key = it.next();
                String value = jedis.get(key);
                String[] split = key.split(",");
                int componentType = Integer.valueOf(split[0]);
                int componentId = Integer.valueOf(split[1]);
                int componentParamId = Integer.valueOf(split[2]);
                int stationId = Integer.valueOf(split[3]);
                // System.out.println("componentType"+ componentType+"componentId"+componentId+"componentParamId"+componentParamId+"stationId"+stationId+"date:"+localDateTime+ "key: " + key + ",--value: " + value);
                long t2 = System.currentTimeMillis();
                // System.out.println(t2-t1);
                ToMysql(componentType,componentId,componentParamId,stationId,value,localDateTime);
            }
        } catch (JedisConnectionException e) {
            logger.warn("redis连接异常，需要重新连接",e.getCause());
            jedis=new Jedis(properties.getProperty("redis.host"), Integer.parseInt(properties.getProperty("redis.port")));
            jedis.auth(PropertiesConstants.REDIS_PASSWD);
        } catch (Exception e){
            logger.warn("source 数据源异常\",e.getCause()");
        }
        return true;
    }
    public   void  ToMysql(int componentType,int componentId,int componentParamId,int stationId,String dataValue,LocalDateTime localDateTime) {
        int year = localDateTime.getYear();
        int month = localDateTime.getMonthValue();
        int day = localDateTime.getDayOfMonth();
        int hour = localDateTime.getHour();
        int minute = localDateTime.getMinute();
        //int second = localDateTime.getSecond();，令second默认为0
        int second = 0;
        String tablename = "kwhdata"+String.valueOf(year)+String.format("%02d",month);
        try {
            conn = dataSource.getConnection();
            String datatime = String.valueOf(year)+"-"+String.format("%02d",month)+"-"+String.format("%02d",day)+" "+String.format("%02d",0)+"-"+String.format("%02d",minute)+"-"+String.format("%02d",second);
            String sql1 = "select RiQi from "+tablename+" where RiQi="+"'"+datatime+"'"+" and BuJianLeiXingID="+componentType+" and BuJianID="+componentId+" and BuJianCanShuID="+componentParamId+" and ChangZhanID="+stationId;
            ps =  conn.prepareStatement(sql1);
            rs = ps.executeQuery();
            //如果查询到有此数据，就更新
            if(rs.next()==true){
                String sql = "update "+tablename+" set "+" H"+String.valueOf(hour)+"=?"+ " where RiQi="+"'"+datatime+"'"+ " and BuJianLeiXingID="+componentType+" and BuJianID="+componentId+" and BuJianCanShuID="+componentParamId+" and ChangZhanID="+stationId;
                ps =  conn.prepareStatement(sql);
                ps.setDouble(1,Double.valueOf(dataValue));
                ps.executeUpdate();
            }else{
                long t9 = System.currentTimeMillis();
                String sql = "insert DELAYED into "+tablename+" (RiQi, BuJianLeiXingID, BuJianID, BuJianCanShuID, ChangZhanID,"+" H"+String.valueOf(hour)+") values(?,?,?,?,?,?)";
                ps =  conn.prepareStatement(sql);
                ps.setString(1,datatime);
                ps.setInt(2,componentType);
                ps.setInt(3,componentId);
                ps.setInt(4,componentParamId);
                ps.setInt(5,stationId);
                ps.setDouble(6,Double.valueOf(dataValue));
                ps.executeUpdate();
            }
            conn.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}
