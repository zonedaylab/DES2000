package cn.zup.iot.basedata;

import cn.zup.iot.common.model.DataEvent;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;

/**
 * Desc:
 * weixin: zhisheng_tian
 * blog: http://www.54tianzhisheng.cn/
 */
public class SinkToCelanData extends RichSinkFunction<DataEvent> {
    PreparedStatement ps;
    private Connection connection;

    /**
     * open() 方法中建立连接，这样不用每次 invoke 的时候都要建立连接和释放连接
     *
     * @param parameters
     * @throws Exception
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        connection = getConnection();
        StringBuffer sql = new StringBuffer("INSERT INTO kwhdata202010 "+
                " (riqi, BuJianLeiXingID, BuJianCanShuID, BuJianID, ChangZhanID, " +
                "H0) VALUES"
                + " (?, ?, ?, ?, ?, ?)");
//                + " (?, ?, ?, ?, ?, ?, 0, ?, 0 , ?, 0, ?, 0, ?, 0, ?, 0, ?, 0," +
//                " ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0," +
//                " ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0) ");
        ps = this.connection.prepareStatement(sql.toString());
    }

    @Override
    public void close() throws Exception {
        super.close();
        //关闭连接和释放资源
        if (connection != null) {
            connection.close();
        }
        if (ps != null) {
            ps.close();
        }
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
        //组装数据，执行插入操作
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = new Date(value.getEventTime()*1000L);
        System.out.printf("\n"+simpleDateFormat.format(date));

        ps.setTimestamp(1,new java.sql.Timestamp(value.getEventTime()*1000L));

        ps.setInt(2, value.getComponentType());
        ps.setInt(3, value.getComponentId());
        ps.setInt(4, value.getComponentParamId());
        ps.setInt(5, value.getStationId());

        ps.setDouble(6, Double.parseDouble(value.getDataValue()));
        ps.executeUpdate();
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