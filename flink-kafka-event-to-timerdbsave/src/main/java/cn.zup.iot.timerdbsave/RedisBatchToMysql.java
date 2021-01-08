package cn.zup.iot.timerdbsave;

import com.alibaba.druid.pool.DruidDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.exceptions.JedisConnectionException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 *从redis取出数据后批量存储到清洗表
 * @author shishanli
 * @date 2021年1月3日22:06:49
 */
public class RedisBatchToMysql {

    private DruidDataSource dataSource = null;
    private Connection conn = null;
    private Statement stmt = null;
    private ResultSet resultSet =null;
    private PreparedStatement ps =null;
    private ResultSet  rs =null;
    private Properties properties = null;
    private JdbcTemplate jdbcTemplateLs = null;
    private Jedis jedis = null;
    static  Logger logger = LoggerFactory.getLogger(RedisBatchToMysql.class);

    /**
     *初始化Druid连接池和jdbcTemplate
     * @author shishanli
     * @date 2021年1月4日22:05:55
     */
    public  void jdbcTemplateConfig(){
        dataSource = new DruidDataSource();
        properties = new Properties();
        ClassLoader classLoader = RedisBatchToMysql.class.getClassLoader();
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

    /**
     * 从redis中获取所有的数据然后批量存储到mysql
     * @Author shishanli
     * @date 2020年12月23日16:53:10
     * @param localDateTime 产生5分钟信号的时间，需要存储到清洗表中
     * @return boolean
     * @throws SQLException
     */
    public  boolean getRedisBatchToMysql(LocalDateTime localDateTime) throws SQLException {
        jdbcTemplateConfig();
        jedis=new Jedis(properties.getProperty("redis.host"), Integer.parseInt(properties.getProperty("redis.port")));
        jedis.auth(properties.getProperty("redis.passwd"));
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
            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            conn.setAutoCommit(false);
            ScanParams scanParams = new ScanParams();
            scanParams.count(Integer.MAX_VALUE);
            //获取redis中所有的键
            ScanResult<String> scanResult = jedis.scan("0", scanParams);
            List<String> keys = scanResult.getResult();
            System.out.println("redis共取出数据"+keys.size()+"条");
            Iterator<String> it = keys.iterator();
            //对redis中所有的键迭代
            while (it.hasNext()) {
                String key = it.next();
                dataValue = jedis.get(key);
                String[] split = key.split(",");
                componentType = Integer.valueOf(split[0]);
                componentId = Integer.valueOf(split[1]);
                componentParamId = Integer.valueOf(split[2]);
                stationId = Integer.valueOf(split[3]);
                //如果查询到该表中符合条件的数据大于0条，就更新，否则就插入
                if(queryData(tablename,datatime,componentType,componentId,componentParamId,stationId)>0){
                    String sql = "update "+tablename+" set "+" H"+String.valueOf(hour)+"="+Double.valueOf(dataValue)+ " where RiQi="+"'"+datatime+"'"+ " and BuJianLeiXingID="+componentType+" and BuJianID="+componentId+" and BuJianCanShuID="+componentParamId+" and ChangZhanID="+stationId;
                    stmt.addBatch(sql);
                }else{
                    String sql = "insert DELAYED into "+tablename+" (RiQi, BuJianLeiXingID, BuJianID, BuJianCanShuID, ChangZhanID,"+" H"+String.valueOf(hour)+") values("+"'"+datatime+"'"+","+componentType+","+componentId+","+componentParamId+","+stationId+","+Double.valueOf(dataValue)+")";
                    stmt.addBatch(sql);
                }
            }
            stmt.executeBatch();
            conn.commit();
            System.out.println("提交成功!");
            conn.setAutoCommit(true);
        } catch (JedisConnectionException e) {
            logger.warn("redis连接异常，需要重新连接",e.getCause());
        } catch (SQLException  e){
            e.printStackTrace();
            try{
                if(!conn.isClosed()){
                    conn.rollback();
                    System.out.println("提交失败，回滚！");
                    conn.setAutoCommit(true);
                }

            }catch (SQLException e1){
                e1.printStackTrace();
            }finally {
                stmt.close();
                conn.close();
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        //是否该返回return true;
        return true;
    }

    /**
     * 查询清洗表中有几条符合条件的数据
     * @Author shishanli
     * @Date 2020年12月23日14:29:06
     * @param tablename 表名
     * @param datatime 符合清洗表的时间字符串
     * @param componentType 部件类型
     * @param componentId 部件Id
     * @param componentParamId 部件参数Id
     * @param stationId 场站Id
     * @return int
     */
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

}
