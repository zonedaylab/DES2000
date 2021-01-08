package cn.zup.iot.cleandata;

import cn.zup.iot.common.model.DataEvent;
import com.alibaba.druid.pool.DruidDataSource;
import javassist.compiler.ast.Stmnt;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/**
 * sink到清洗表中
 * @author shishanli
 * @date 2021年1月3日20:15:53
 */
public class SinkToCelanData extends RichSinkFunction<DataEvent> {
    private DruidDataSource dataSourceLs;
    private Connection connectionLs;
    private PreparedStatement psLs;
    private Statement  stmntLs;
    private ResultSet resultSetLs;
    private JdbcTemplate jdbcTemplateLs;
    private DruidDataSource dataSourceMs;
    private Connection connectionMs;
    private Statement stmtMs;
    private ResultSet resultSetMs;
    private Properties properties;
    private Map<String,Integer>  componentParamMap;
    private JdbcTemplate jdbcTemplateMs;


    /**
     * open() 方法中建立连接，这样不用每次 invoke 的时候都要建立连接和释放连接
     * @author shishanli
     * @date 2021年1月5日00:48:29
     * @param parameters
     * @throws Exception
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        properties = new Properties();
        ClassLoader classLoader = SinkToCelanData.class.getClassLoader();
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
        dataSourceMs = new DruidDataSource();
        dataSourceMs.setUrl(properties.getProperty("ms.url"));
        dataSourceMs.setUsername(properties.getProperty("ms.username"));
        dataSourceMs.setPassword(properties.getProperty("ms.passwd"));
        dataSourceMs.setDriverClassName(properties.getProperty("ms.driverClassName"));
        dataSourceMs.setInitialSize(Integer.valueOf(properties.getProperty("ms.initialSize")));
        dataSourceMs.setMinIdle(Integer.valueOf(properties.getProperty("ms.minIdle")));
        dataSourceMs.setMaxActive(Integer.valueOf(properties.getProperty("ms.maxActive")));
        dataSourceMs.setMaxWait(Integer.valueOf(properties.getProperty("ms.maxWait")));
        jdbcTemplateLs = new JdbcTemplate(dataSourceLs);
        jdbcTemplateMs = new JdbcTemplate(dataSourceMs);
        //将bujiancanshu表中的数据存入hashMap中
        componentParamMap = getDtype();

    }

    /**
     * close()关闭连接
     * @author shishanli
     * @date 2021年1月5日00:49:02
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        super.close();
    }

    /**
     * 每条kafka消息的插入都要调用一次 invoke() 方法
     * @author shishanli
     * @date 2021年1月5日00:49:17
     * @param dataEvent
     * @param context
     * @throws Exception
     */
    @Override
    public void invoke(DataEvent dataEvent, Context context) throws Exception {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(dataEvent.getEventTime()), TimeZone.getDefault().toZoneId());
        int year = localDateTime.getYear();
        int month = localDateTime.getMonthValue();
        String tablename= "";
        //如果为空，就什么也不执行
        if(componentParamMap.get(String.valueOf(dataEvent.getComponentType())+"."+String.valueOf(dataEvent.getComponentId()))==null){

        }
        //如果在hashMap中查到ComponentType.ComponentId的值为4，令tablename=ycdata
        else if(componentParamMap.get(String.valueOf(dataEvent.getComponentType())+"."+String.valueOf(dataEvent.getComponentId()))==4){
            tablename = "ycdata"+String.valueOf(year)+String.format("%02d",month);
            executeTable(tablename,dataEvent,localDateTime);
        }
        //如果在hashMap中查到ComponentType.ComponentId的值为2，令tablename=kwhdata
        else if(componentParamMap.get(String.valueOf(dataEvent.getComponentType())+"."+String.valueOf(dataEvent.getComponentId()))==2){
            tablename = "kwhdata"+String.valueOf(year)+String.format("%02d",month);
            executeTable(tablename,dataEvent,localDateTime);
        }
        else{
            tablename = "kwhdata"+String.valueOf(year)+String.format("%02d",month);
            executeTable(tablename,dataEvent,localDateTime);
        }
    }

    /**
     * 目的是为了解耦，从hashMap取出的值等于2或等于4时才会继续执行下面操作
     * @author shishanli
     * @date 2021年1月5日00:49:34
     * @param tablename
     * @param dataEvent
     * @param localDateTime
     */
    public void executeTable(String tablename,DataEvent dataEvent,LocalDateTime localDateTime){
        int year = localDateTime.getYear();
        int month = localDateTime.getMonthValue();
        int day = localDateTime.getDayOfMonth();
        int hour = localDateTime.getHour();
        //x=(x/5)*5表示将信号时间归为0,5,10,15。。。
        int minute = (localDateTime.getMinute()/5)*5;
        int second = 0;
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
        String datatime = String.valueOf(year)+"-"+String.format("%02d",month)+"-"+String.format("%02d",day)+" "+String.format("%02d",0)+"-"+String.format("%02d",minute)+"-"+String.format("%02d",second);
        //如果查询到表中有此数据，就更新
        if(queryData(tablename,datatime,dataEvent)>0){
            updateCleanData(tablename,datatime,dataEvent,hour);
        }else{
            insertCleanData(tablename,datatime,dataEvent,hour);
        }
    }

    /**
     * 将bujiancanshu表中的数据存入hashMap中,其中bjlxID.ID作为键，DType作为值,根据Dtype值的不同插入不同的清洗表
     * @author shishanli
     * @date 2021年1月5日00:49:53
     * @return Map<String,Integer>
     * @throws SQLException
     */
    public Map<String,Integer> getDtype() throws SQLException {
        Map<String,Integer> resultMap = new  HashMap<String,Integer>();
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(" SELECT bjlxID,ID,DType from bujiancanshu");
            resultMap = jdbcTemplateMs.query(sb.toString(), new ResultSetExtractor<Map<String,Integer>>() {
                @Override
                public Map<String,Integer> extractData(ResultSet rs) throws SQLException, DataAccessException {
                    Map<String, Integer> result = new HashMap<String, Integer>();
                    while (rs.next()) {
                        result.put(String.valueOf(rs.getInt("bjlxID"))+"."+String.valueOf(rs.getInt("ID")),rs.getInt("DType"));
                    }
                    return result;
                }
            });
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
            return null;
        }
        return resultMap;
    }

    /**
     * 查询表中是否存在这条数据
     * @author shishanli
     * @date 2021年1月5日00:49:53
     * @param tablename
     * @param datatime
     * @param dataEvent
     * @return int
     */
    public int queryData(String tablename,String datatime,DataEvent dataEvent){
        StringBuffer sql = new StringBuffer();
        sql.append(" select count(*) from "+tablename);
        sql.append(" where RiQi='"+datatime);
        sql.append("' and BuJianLeiXingID="+dataEvent.getComponentType());
        sql.append(" and BuJianID="+dataEvent.getComponentId());
        sql.append(" and BuJianCanShuID="+dataEvent.getComponentParamId());
        sql.append(" and ChangZhanID="+dataEvent.getStationId());
        int count = jdbcTemplateLs.queryForObject(sql.toString(), Integer.class);
        return count;
    }

    /**
     * 插入数据到清洗表中
     * @author shishanli
     * @date 2021年1月5日00:49:53
     * @param tablename
     * @param datatime
     * @param dataEvent
     * @param hour
     */
    public void insertCleanData(String tablename,String datatime,DataEvent dataEvent,int hour){
        StringBuffer sql = new StringBuffer();
        sql.append(" insert DELAYED into "+tablename);
        sql.append("(RiQi, BuJianLeiXingID, BuJianID, BuJianCanShuID, ChangZhanID, H"+String.valueOf(hour));
        sql.append(") values(?,?,?,?,?,?)");
        System.out.println(sql);
        jdbcTemplateLs.update(String.valueOf(sql),new PreparedStatementSetter(){

            @Override
            public void setValues(PreparedStatement ps) throws SQLException {

                ps.setString(1,datatime);
                ps.setInt(2,dataEvent.getComponentType());
                ps.setInt(3,dataEvent.getComponentId());
                ps.setInt(4,dataEvent.getComponentParamId());
                ps.setInt(5,dataEvent.getStationId());
                ps.setDouble(6,Double.valueOf(dataEvent.getDataValue()));
            }
        });
    }

    /**
     * 更新数据到清洗表中
     * @author shishanli
     * @date 2021年1月5日00:49:53
     * @param tablename
     * @param datatime
     * @param dataEvent
     * @param hour
     */
    public void updateCleanData(String tablename,String datatime,DataEvent dataEvent,int hour){
        StringBuffer sql = new StringBuffer();
        sql.append(" update "+tablename);
        sql.append(" set H"+String.valueOf(hour));
        sql.append("=? where RiQi = '"+datatime);
        sql.append("' and BuJianLeiXingID = "+dataEvent.getComponentType());
        sql.append(" and BuJianID = "+dataEvent.getComponentId());
        sql.append(" and BuJianCanShuID = "+dataEvent.getComponentParamId());
        sql.append(" and ChangZhanID = "+dataEvent.getStationId());
        System.out.println(sql);
        jdbcTemplateLs.update(String.valueOf(sql),new PreparedStatementSetter(){

            @Override
            public void setValues(PreparedStatement ps) throws SQLException {

                ps.setDouble(1,Double.valueOf(dataEvent.getDataValue()));
            }
        });
    }

}