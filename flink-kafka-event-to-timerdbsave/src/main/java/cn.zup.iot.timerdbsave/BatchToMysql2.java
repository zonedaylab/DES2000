package cn.zup.iot.timerdbsave;

import com.alibaba.druid.pool.DruidDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class BatchToMysql2 {

    private DruidDataSource dataSource;
    private Connection conn = null;
    private Statement stmt = null;
    private ResultSet resultSet =null;
    private PreparedStatement ps =null;
    private ResultSet  rs =null;
    private Properties properties = null;
    private JdbcTemplate jdbcTemplateLs;
    static  Logger logger = LoggerFactory.getLogger(GetRedisToMysql.class);
    static  final long SLEEP_MILLION=5000;
    public  void DatadataSourceConfig(){
        dataSource = new DruidDataSource();
        properties = new Properties();
        ClassLoader classLoader = BatchToMysql2.class.getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("application.properties");
        try {
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
        dataSource.setUrl(properties.getProperty("ls.url"));
        dataSource.setUsername(properties.getProperty("ls.username"));
        dataSource.setPassword(properties.getProperty("ls.passwd"));
        dataSource.setDriverClassName(properties.getProperty("ls.driverClassName"));
        dataSource.setInitialSize(Integer.valueOf(properties.getProperty("ls.initialSize")));
        dataSource.setMinIdle(Integer.valueOf(properties.getProperty("ls.minIdle")));
        dataSource.setMaxActive(Integer.valueOf(properties.getProperty("ls.maxActive")));
        dataSource.setMaxWait(Integer.valueOf(properties.getProperty("ls.maxWait")));
        jdbcTemplateLs = new JdbcTemplate(dataSource);
    };
    public   boolean GetRedis(LocalDateTime localDateTime) throws SQLException {
        DatadataSourceConfig();
        Jedis jedis =null;
        jedis=new Jedis(properties.getProperty("redis.host"), Integer.parseInt(properties.getProperty("redis.port")));
//        jedis.auth(PropertiesConstants.REDIS_PASSWD);
        try{
            int year = localDateTime.getYear();
            int month = localDateTime.getMonthValue();
            int day = localDateTime.getDayOfMonth();
            int hour = localDateTime.getHour();
            int minute = localDateTime.getMinute();
            int second = 0;
            int componentType,componentId,componentParamId,stationId=0;
            String dataValue="";
            String datatime = String.valueOf(year)+"-"+String.format("%02d",month)+"-"+String.format("%02d",day)+" "+String.format("%02d",0)+"-"+String.format("%02d",minute)+"-"+String.format("%02d",second);
            //如果表不存在就创建表
            String tablename = "kwhdata"+String.valueOf(year)+String.format("%02d",month);
            String createBrandDatabase = "CREATE TABLE IF NOT EXISTS "
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
            jdbcTemplateLs.execute(createBrandDatabase);
//            conn = dataSource.getConnection();
//            stmt = conn.createStatement();
//            conn.setAutoCommit(false);
            //获取redis中所有的键
            ScanParams scanParams = new ScanParams();
            scanParams.count(Integer.MAX_VALUE);
            ScanResult<String> scanResult = jedis.scan("0", scanParams);
            List<String> keys = scanResult.getResult();
            System.out.println("redis共取出数据"+keys.size()+"条");
            Iterator<String> it = keys.iterator();
            List<BatchUpdaeParm> sqlInsertList = new ArrayList<BatchUpdaeParm>();
            List<BatchUpdaeParm> sqlUpdateList = new ArrayList<BatchUpdaeParm>();
            BatchUpdaeParm batchUpdaeParm = null;
            while (it.hasNext()) {
                String key = it.next();
                dataValue = jedis.get(key);
                String[] split = key.split(",");
                componentType = Integer.valueOf(split[0]);
                componentId = Integer.valueOf(split[1]);
                componentParamId = Integer.valueOf(split[2]);
                stationId = Integer.valueOf(split[3]);
                batchUpdaeParm = new BatchUpdaeParm(hour,datatime,componentType,componentId,componentParamId,stationId,Double.valueOf(dataValue));
                //如果查询到有此数据，就更新
                if(queryData(tablename,datatime,componentType,componentId,componentParamId,stationId)>0){
                    sqlUpdateList.add(batchUpdaeParm);
                }else{
                    sqlInsertList.add(batchUpdaeParm);
                }
            }
            batchUpdatesqlList(tablename,sqlUpdateList);
            batchInsertsqlList(tablename,sqlInsertList);
        } catch (JedisConnectionException e) {
            logger.warn("redis连接异常，需要重新连接",e.getCause());
            jedis=new Jedis(properties.getProperty("redis.host"), Integer.parseInt(properties.getProperty("redis.port")));
//            jedis.auth(PropertiesConstants.REDIS_PASSWD);
        }
        return true;
    }
    public int queryData(String tablename, String datatime, int componentType,int componentId, int componentParamId,int stationId){
        StringBuffer sql = new StringBuffer();
        sql.append(" select count(*) from "+tablename);
        sql.append(" where RiQi='"+datatime);
        sql.append("' and BuJianLeiXingID="+componentType);
        sql.append(" and BuJianID="+componentId);
        sql.append(" and BuJianCanShuID="+componentParamId);
        sql.append(" and ChangZhanID="+stationId);
        int count = jdbcTemplateLs.queryForObject(sql.toString(), Integer.class);
        return count;
    }

    public void batchUpdatesqlList(String tablename,List list) {
        String sql = "update "+tablename+" set ?=? where RiQi=? and BuJianLeiXingID=? and BuJianID=? and BuJianCanShuID=? and ChangZhanID=?";
        jdbcTemplateLs.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return list.size();
                //这个方法设定更新记录数，通常List里面存放的都是我们要更新的，所以返回list.size();
            }
            public void setValues(PreparedStatement ps, int i)throws SQLException {
                BatchUpdaeParm batchUpdaeParm = (BatchUpdaeParm) list.get(i);
                ps.setString(1, "H"+String.valueOf(batchUpdaeParm.getHour()));
                ps.setDouble(2, batchUpdaeParm.getDataValue());
                ps.setString(3, batchUpdaeParm.getDataTime());
                ps.setInt(4, batchUpdaeParm.getComponentType());
                ps.setInt(5, batchUpdaeParm.getComponentId());
                ps.setInt(6, batchUpdaeParm.getComponentParamId());
                ps.setInt(7, batchUpdaeParm.getStationId());
            }
        });
    }
    public void batchInsertsqlList(String tablename,List list) {
        //H1,H2还是H24是变化的，list里面的每一个都不一样
        String sql = "insert DELAYED into "+tablename+" (RiQi, BuJianLeiXingID, BuJianID, BuJianCanShuID, ChangZhanID,?) values(?,?,?,?,?,?)";
        jdbcTemplateLs.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return list.size();
                //这个方法设定更新记录数，通常List里面存放的都是我们要更新的，所以返回list.size();
            }
            //PreparedStatement只能用来为参数（如参数值）设置动态参数，即用?占位，不可用于表名、字段名等
            public void setValues(PreparedStatement ps, int i)throws SQLException {
                BatchUpdaeParm batchUpdaeParm = (BatchUpdaeParm) list.get(i);
                ps.setString(1, String.valueOf("H"+String.valueOf(batchUpdaeParm.getHour())));
                ps.setString(2, batchUpdaeParm.getDataTime());
                ps.setInt(3, batchUpdaeParm.getComponentType());
                ps.setInt(4, batchUpdaeParm.getComponentId());
                ps.setInt(5, batchUpdaeParm.getComponentParamId());
                ps.setInt(6, batchUpdaeParm.getStationId());
                ps.setDouble(7, batchUpdaeParm.getDataValue());
            }
        });
    }


}
