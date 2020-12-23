package cn.zup.iot.cleandata;

import cn.zup.iot.common.model.DataEvent;
import com.alibaba.druid.pool.DruidDataSource;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Desc:
 * weixin: zhisheng_tian
 * blog: http://www.54tianzhisheng.cn/
 */
public class SinkToCelanDataCopy extends RichSinkFunction<DataEvent> {
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
     *
     * @param parameters
     * @throws Exception
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        properties = new Properties();
        ClassLoader classLoader = SinkToCelanDataCopy.class.getClassLoader();
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
//        componentParamMap = new  HashMap<String,Integer>();
        componentParamMap = getDtype();

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
        //首先根据部件类型id、部件参数id从ms数据库里的bujiancanshu表查找Dtype,根据Dtype的的值选择插入ycdata表(4)，还是kwhdata表(2)。
        //插入数据表时根据DataEvent的时间，取localDate的分钟位为x，x=(x/5)*5,先判断这个DataEvent在数据库中有没有，如果有就更新，没有就插入
        long t3 = System.currentTimeMillis();
        int dtype = 0;
        if(componentParamMap.get(String.valueOf(value.getComponentType())+"."+String.valueOf(value.getComponentId()))==null){
            System.out.println(String.valueOf(value.getComponentType())+"."+String.valueOf(value.getComponentId()));
            dtype = 4;
        }else{
            dtype = componentParamMap.get(String.valueOf(value.getComponentType())+"."+String.valueOf(value.getComponentId()));
            //dtype =getDtype(value.getComponentType(),value.getComponentParamId());
        }

        long t4 = System.currentTimeMillis();
        System.out.println("从HashMap中取数据的时间："+(t4-t3));
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(value.getEventTime()), TimeZone.getDefault().toZoneId());
        System.out.println("信号当前的时间："+localDateTime);
        int year = localDateTime.getYear();
        int month = localDateTime.getMonthValue();
        int day = localDateTime.getDayOfMonth();
        int hour = localDateTime.getHour();
        int minute = (localDateTime.getMinute()/5)*5;
        //int second = localDateTime.getSecond();，令second默认为0
        int second = 0;
        String tablename= "";
        //创建数据库
        if(dtype==4){
            tablename = "ycdata"+String.valueOf(year)+String.format("%02d",month);
        }else {
            tablename = "kwhdata"+String.valueOf(year)+String.format("%02d",month);
        }
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
        connectionLs = dataSourceLs.getConnection();
        stmntLs = connectionLs.createStatement();
        resultSetLs =stmntLs.executeQuery(checkTable);
        if(resultSetLs.next()==false) {
            if(stmntLs.executeUpdate(createBrandDatabase)==0){
                System.out.println("create table success!");
            }
        }
        try {
            String datatime = String.valueOf(year)+"-"+String.format("%02d",month)+"-"+String.format("%02d",day)+" "+String.format("%02d",0)+"-"+String.format("%02d",minute)+"-"+String.format("%02d",second);
            String sql1 = "select RiQi from "+tablename+" where RiQi="+"'"+datatime+"'"+" and BuJianLeiXingID="+value.getComponentType()+" and BuJianID="+value.getComponentId()+" and BuJianCanShuID="+value.getComponentParamId()+" and ChangZhanID="+value.getStationId();
            psLs =  connectionLs.prepareStatement(sql1);
            resultSetLs = psLs.executeQuery();
            //如果查询到有此数据，就更新
            if(resultSetLs.next()==true){
                String sql = "update "+tablename+" set "+" H"+String.valueOf(hour)+"=?"+ " where RiQi="+"'"+datatime+"'"+ " and BuJianLeiXingID="+value.getComponentType()+" and BuJianID="+value.getComponentId()+" and BuJianCanShuID="+value.getComponentParamId()+" and ChangZhanID="+value.getStationId();
                System.out.println(sql);
                psLs =  connectionLs.prepareStatement(sql);
                psLs.setDouble(1,Double.valueOf(value.getDataValue()));
                psLs.executeUpdate();
            }else{
                insertCleanData(tablename,datatime,value,hour);
//                String sql = "insert DELAYED into "+tablename+" (RiQi, BuJianLeiXingID, BuJianID, BuJianCanShuID, ChangZhanID,"+" H"+String.valueOf(hour)+") values(?,?,?,?,?,?)";
//                System.out.println(sql);
//                psLs =  connectionLs.prepareStatement(sql);
//                psLs.setString(1,datatime);
//                psLs.setInt(2,value.getComponentType());
//                psLs.setInt(3,value.getComponentId());
//                psLs.setInt(4,value.getComponentParamId());
//                psLs.setInt(5,value.getStationId());
//                psLs.setDouble(6,Double.valueOf(value.getDataValue()));
//                psLs.executeUpdate();
            }
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


    public Map<String,Integer> getDtype() throws SQLException {
        Map<String,Integer> resultMap = new  HashMap<String,Integer>();
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(" SELECT bjlxID,ID,DType from bujiancanshu");
            resultMap = jdbcTemplateMs.query(sb.toString(), new ResultSetExtractor<Map<String,Integer>>() {
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
    public void insertCleanData(String tablename,String datatime,DataEvent value,int hour){
        StringBuffer sql = new StringBuffer();
        sql.append(" insert DELAYED into "+tablename);
        sql.append("(RiQi, BuJianLeiXingID, BuJianID, BuJianCanShuID, ChangZhanID, H"+String.valueOf(hour));
        sql.append(") values(?,?,?,?,?,?)");
        System.out.println(sql);
        jdbcTemplateLs.update(String.valueOf(sql),new PreparedStatementSetter(){

            public void setValues(PreparedStatement ps) throws SQLException {

                ps.setString(1,datatime);
                ps.setInt(2,value.getComponentType());
                ps.setInt(3,value.getComponentId());
                ps.setInt(4,value.getComponentParamId());
                ps.setInt(5,value.getStationId());
                ps.setDouble(6,Double.valueOf(value.getDataValue()));
            }
        });
    }
    public void updateCleanData(String tablename,String datatime,DataEvent value,int hour){
        StringBuffer sql = new StringBuffer();
        sql.append(" update "+tablename);
        sql.append(" set H"+String.valueOf(hour));
        sql.append("=? where RiQi = "+datatime);
        sql.append(" and BuJianLeiXingID = "+value.getComponentType());
        sql.append(" and BuJianID = "+value.getComponentId());
        sql.append(" and BuJianCanShuID = "+value.getComponentParamId());
        sql.append(" and ChangZhanID = "+value.getStationId());
        System.out.println(sql);
        jdbcTemplateLs.update(String.valueOf(sql),new PreparedStatementSetter(){

            public void setValues(PreparedStatement ps) throws SQLException {

                ps.setDouble(1,Double.valueOf(value.getDataValue()));
            }
        });
    }


    public  int getDtype(int BuJianLXId,int BuJianParamId) throws SQLException {
        int dtype = 0;
        connectionMs = dataSourceMs.getConnection();
        stmtMs = connectionMs.createStatement();
        String sql = "SELECT DType from bujiancanshu WHERE bjlxID ="+BuJianLXId+" and ID ="+BuJianParamId;
        // 执行查询
        resultSetMs = stmtMs.executeQuery(sql);
        while(resultSetMs.next()){
            dtype = resultSetMs.getInt("DType");
        }
        stmtMs.close();
        connectionMs.close();
        return dtype;
    }


}