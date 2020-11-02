package cn.zup.iot.common.model;

import cn.zup.iot.common.model.DataEvent;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;


public class SelectMysql {
   public static HashMap<String,String> ConnMysql(){
        HashMap<String,String> hashMap = new HashMap<String, String>();
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?useSSL=false&useUnicode=true&characterEncoding=UTF8&serverTimezone=GMT", "root", "iesapp");
        } catch (Exception e) {
            System.out.println("-----------mysql get connection has exception , msg = "+ e.getMessage());
        }
        //从数据库中查询
        String sql="select name,age from users where id=?";
        PreparedStatement pstmt= null;
        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, 1);
            ResultSet rs=pstmt.executeQuery();
            while(rs.next()){
                String name=rs.getString(1);
                int age=rs.getInt("age");
                System.out.println(name+":"+age);
            }
            rs.close();
            pstmt.close();
            con.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return hashMap;
    }
}