package cn.zup.iot.timercalc.dao;

import cn.zup.iot.timercalc.data_struct.cal_param;
import cn.zup.iot.timercalc.model.*;
import cn.zup.iot.timercalc.util.HisDataTimeAndValue;
import cn.zup.iot.timercalc.util.JdbcTemplateUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/***
 * 
 * @author samson
 * @date 2019-03-21 15
 * @des 获取数据访问类
 *
 */
public class DataDao {

	private JdbcTemplate jdbcTemplate_ccmservicems = JdbcTemplateUtils.jdbcTemplateLs;
	private CalcConfig calc= new CalcConfig();

	private static final int minuteParam = 300000;
	
	/**
	 * @author samson
	 * @des 根据三段式获取数据
	 * @param buJianType
	 * @param buJianID
	 * @param buJianCanshu
	 * @param infoType
	 * @param date
	 * @return
	 */
	public double getThreeParamData(int buJianType,int buJianID,int buJianCanshu,int infoType, Calendar date,Integer calcIntervalTime) {
		//获取当前的时间
		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis()-minuteParam); //获取前minuteParam数据
		//如果传过来的date不为空，就把today设置为传来的参数。
		if(date != null){
			today.setTimeInMillis(date.getTimeInMillis());
		}
		int today_year = today.get(Calendar.YEAR);
		int today_month = today.get(Calendar.MONTH) + 1;
//		int today_day = today.get(Calendar.DAY_OF_MONTH);
//		int today_hour = today.get(Calendar.HOUR_OF_DAY);
//		int today_minute = today.get(Calendar.MINUTE);
		int today_day = 3;
		int today_hour = 12;
		int today_minute = 5;
		//将today_minute 归为00,05,10,15,20
		today_minute -= today_minute%5;
		String year = String.valueOf(today_year);
	    String month = String.valueOf(today_month);
	    if(today_month<10)
	    	month = "0"+month;
	    String day = String.valueOf(today_day);
	    String xiaoshi = String.valueOf(today_hour);
	    String fen = String.valueOf(today_minute)+":00";
	    if(today_minute<10){
	    	fen="0"+today_minute+":00";
	    }
	    int xs= Integer.parseInt(xiaoshi);
	    xiaoshi= String.valueOf(xs);
	    List<DataValue> result  = new ArrayList<DataValue>();
	    String tableName = "";
    	StringBuffer sb= new StringBuffer();
		//根据infoType==1 就从ycdata表中选
	    if(infoType==1)
	    {
	    	tableName = "ycdata" + year + month;
	    	sb.append("select a.riqi,a.H"+xiaoshi+" as datavalue ");
	    	sb.append(" from ls."+tableName+" a  where "
	    			+ " a.BuJianLeiXingID = 20 "
	    	    	+ " and a.BuJianID in "+ "(select id from ms.xunidmnliang where " +
					"dybjtype = "+buJianType + " and dybjparam = "+buJianCanshu + " and dybjid = "+buJianID + " )"
	    	    	+ " and a.BuJianCanShuID = 2 ");
		    sb.append(" and a.riqi = '" + year + "-"+month+"-"+day+" 00:"+fen+"'  and 1=1 ");
		//根据infoType==2 就从kwhdata表中选
	    }else if(infoType==2)
  	    {
  	    	tableName = "kwhdata" + year + month;
  	    	sb.append("select a.riqi,a.H"+xiaoshi+" as datavalue ");
  	    	sb.append(" from ls."+tableName+" a where "
  	    			+ " a.BuJianLeiXingID = "+buJianType + " "
  	    	    	+ " and a.BuJianID = "+buJianID + " "
  	    	    	+ " and a.BuJianCanShuID = "+buJianCanshu + " ");
  		    sb.append(" and a.riqi = '" + year + "-"+month+"-"+day+" 00:"+fen+"'  and 1=1  ");
		//根据infoType==4 就从energydata表中选
  	    }else if(infoType==4) //energy表
  	    {
  	    	tableName = "energydata" + year + month;
  	    	sb.append("select datatime,value as datavalue");
  	    	sb.append(" from ls."+tableName+" where "
  	    			+ " BuJianLeiXingID = "+buJianType + " "
  	    	    	+ " and BuJianID = "+buJianID + " "
  	    	    	+ " and BuJianCanShuID = "+buJianCanshu + " ");
  		    sb.append(" and datatime = '" + year + "-"+month+"-" + day + " 00:00:00' and 1=1 ");
  	    }
	    //System.out.println(sb.toString());
 
	    try{
		    result = jdbcTemplate_ccmservicems.query(sb.toString(), new ResultSetExtractor<List<DataValue>>() {
		    public List<DataValue> extractData(ResultSet rs)  throws SQLException, DataAccessException {
				 List<DataValue> result = new ArrayList<DataValue>();  
				 while(rs.next()) {  
					DataValue row = new DataValue(); 
                    row.setValue(rs.getDouble(2));
                    result.add(row);  
				 }
				 return result;  
		     }});
	    }catch(Exception e){
    		e.printStackTrace();
	    	return 0;
    	}
	    if(result.size()==0){
	    	return 0;
	    }else{
		    return result.get(0).getValue();	    
	    }
	}
	
	/**
	 * @author ZhangSC
	 * @des 根据三段式获取数据
	 * @param buJianType
	 * @param buJianID
	 * @param buJianCanshu
	 * @param infoType
	 * @param date
	 * @return
	 */
	public List<Float> getThreeParamDataList(int buJianType,int buJianID,int buJianCanshu,int infoType, Calendar date,Integer calcIntervalTime) {

		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis()-minuteParam); //获取前minuteParam数据 
		if(date != null){
			today.setTimeInMillis(date.getTimeInMillis());
		}
		int today_year = today.get(Calendar.YEAR);
		int today_month = today.get(Calendar.MONTH) + 1;
//		int today_day = today.get(Calendar.DAY_OF_MONTH);
//		int today_hour = today.get(Calendar.HOUR_OF_DAY);
//		int today_minute = today.get(Calendar.MINUTE);
		int today_day = 3;
		int today_hour = 12;
		int today_minute = 5;
		today_minute -= today_minute%5;
		String year = String.valueOf(today_year);
	    String month = String.valueOf(today_month);
	    if(today_month<10)
	    	month = "0"+month;
	    String day = String.valueOf(today_day);
	    String xiaoshi = String.valueOf(today_hour);
	    //源深项目修改为取24个整点数据
	    String fen = "00:00";
	 /*   if(today_minute<10){
	    	fen="0"+today_minute+":00";
	    }*/
	    int xs= Integer.parseInt(xiaoshi);
	    xiaoshi= String.valueOf(xs);
	    List<HisDataTimeAndValue> result  = new ArrayList<HisDataTimeAndValue>();
	    String tableName = "";
    	StringBuffer sb= new StringBuffer();
	    if(infoType==1)
	    {
	    	tableName = "ycdata" + year + month;
	    	sb.append("select riqi,h0,h1,h2,h3,h4,h5,h6,h7,h8,h9,h10,h11,h12,h13,h14,h15,h16,h17,h18,h19,h20,h21,h22,h23 ");
	    	sb.append(" from ls."+tableName+" a  where "
	    			+ " a.BuJianLeiXingID = "+buJianType + " "
	    	    	+ " and a.BuJianID = "+buJianID + " "
	    	    	+ " and a.BuJianCanShuID = "+buJianCanshu + " ");
		    sb.append(" and a.riqi = '" + year + "-"+month+"-"+day+" 00:"+fen+"'  and 1=1 ");
	    }else if(infoType==2)
  	    {
  	    	tableName = "kwhdata" + year + month;
  	    	sb.append("select riqi,h0,h1,h2,h3,h4,h5,h6,h7,h8,h9,h10,h11,h12,h13,h14,h15,h16,h17,h18,h19,h20,h21,h22,h23 ");
  	    	sb.append(" from ls."+tableName+" a where "
  	    			+ " a.BuJianLeiXingID = "+buJianType + " "
  	    	    	+ " and a.BuJianID = "+buJianID + " "
  	    	    	+ " and a.BuJianCanShuID = "+buJianCanshu + " ");
  		    sb.append(" and a.riqi = '" + year + "-"+month+"-"+day+" 00:"+fen+"'  and 1=1  ");
  	    }
	    try{
		    result = jdbcTemplate_ccmservicems.query(sb.toString(), new ResultSetExtractor<List<HisDataTimeAndValue>>() {
		    @Override
			public List<HisDataTimeAndValue> extractData(ResultSet rs)  throws SQLException, DataAccessException {
				 List<HisDataTimeAndValue> result = new ArrayList<HisDataTimeAndValue>();  
				 while(rs.next()) {
					HisDataTimeAndValue row = new HisDataTimeAndValue(); 
				    List<Float> valueList = new ArrayList<Float>();
				    Date date=rs.getTimestamp(1);
                    for(int i=0;i<=23;i++) {
                    	float value = rs.getFloat("h"+i);
                    	valueList.add(value);
                    }
                    row.setListValue(valueList);
                    result.add(row);
				 }
				 return result;  
		     }});
	    }catch(Exception e){
    		e.printStackTrace();
    	}
	    if(result.size() == 0)
		{
			return null;
		}else{
			return result.get(0).getListValue();
		}
	}
	/***
	 * 新增energydata月表
	 * @author samson
	 * @param energyData
	 * @return
	 */
	public String insertEnergyInfo(EnergyData energyData) {
		Calendar calendar = Calendar.getInstance();
		if(energyData.getDataTime() != null){
			calendar.setTimeInMillis(energyData.getDataTime().getTime());
		}
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		String months = ""+month, tbName = "ls.energydata";
		if(month<10) {
			months = "0"+month;
		}
		tbName = tbName + year + months;
		StringBuffer sql = new StringBuffer("INSERT INTO " + tbName + 
				" (hourPower, DataTime, BuJianLeiXingID, BuJianCanShuID, BuJianID, ChangZhanID, `value`, valueFlag) VALUES"
				+ " (0, ?, ?, ?, ?, ?, ?, 0);");
		String error = null;
		try {
			jdbcTemplate_ccmservicems.update(sql.toString(), energyData.getDataTime(),
					energyData.getBuJianLeiXingID(), 
					energyData.getBuJianCanShuID(),
					energyData.getBuJianID(),
					energyData.getChangZhanID(),
					energyData.getValue());
		} catch (Exception e) {
			error = "insertOpenUsePower：保存数据出错，"+e.getMessage()+"；设别id为："+energyData.getBuJianID()+"，部件参数："+energyData.getBuJianCanShuID();
		}
		return error;
	}
	
	/***
	 * 更新energydata月表
	 * @author samson
	 * @param energyData
	 * @return
	 */
	public String updateEnergyInfo(EnergyData energyData) {
		Calendar calendar = Calendar.getInstance();
		if(energyData.getDataTime() != null){
			calendar.setTimeInMillis(energyData.getDataTime().getTime());
		}
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		String months = ""+month, tbName = "ls.energydata";
		if(month<10) {
			months = "0"+month;
		}
		tbName = tbName + year + months;
		StringBuffer sql = new StringBuffer("UPDATE " + tbName + 
				" SET `value` = ? WHERE UNIX_TIMESTAMP(DataTime) = ? AND BuJianLeiXingID = ? "
				+ " AND BuJianCanShuID = ? AND BuJianID= ? AND ChangZhanID= ?  ;");
		String error = null;
		try {
			jdbcTemplate_ccmservicems.update(sql.toString(), energyData.getValue(),energyData.getDataTime().getTime()/1000,
					energyData.getBuJianLeiXingID(), 
					energyData.getBuJianCanShuID(),
					energyData.getBuJianID(),
					energyData.getChangZhanID());
		} catch (Exception e) {
			error = "updateOpenUsePower：更新数据出错，"+e.getMessage()+"；设别id为："+energyData.getBuJianID()+"，部件参数："+energyData.getBuJianCanShuID();
		}
		return error;
	}

	/***
	 * 查询energydata月表
	 * @author samson
	 * @param bjlxId
	 * @param bjcsId
	 * @param bjId
	 * @param czId
	 * @return
	 */
	public EnergyData getEnergyInfo(int bjlxId, int bjcsId, int bjId, int czId, Date date) {
		Calendar calendar = Calendar.getInstance();
		if(date != null){
			calendar.setTimeInMillis(date.getTime());
		}
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		String months = ""+month, tbName = "ls.energydata";
		if(month<10) {
			months = "0"+month;
		}
		tbName = tbName + year + months;
		StringBuffer sql = new StringBuffer("SELECT\r\n" + 
				"	hourPower,\r\n" + 
				"	DataTime,\r\n" + 
				"	BuJianLeiXingID,\r\n" + 
				"	BuJianCanShuID,\r\n" + 
				"	BuJianID,\r\n" + 
				"	ChangZhanID,\r\n" + 
				"	`value`,\r\n" + 
				"	valueFlag\r\n" + 
				"FROM " + tbName +" WHERE 1=1 ");
		if(bjlxId != 0)
			sql.append(" AND BuJianLeiXingID = ? ");
		if(bjcsId != 0)
			sql.append(" AND BuJianCanShuID = ? ");
		if(bjId != 0)
			sql.append(" AND BuJianID = ? ");
		if(czId != 0)
			sql.append(" AND ChangZhanID = ? ");
		if(date != null) {
			sql.append(" AND UNIX_TIMESTAMP(DataTime) = ? ");
		}
		Object[] params = {bjlxId, bjcsId, bjId, czId, date.getTime()/1000};
		EnergyData energyDate = jdbcTemplate_ccmservicems.query(sql.toString(), params, new ResultSetExtractor<EnergyData>(){

			@Override
			public EnergyData extractData(ResultSet rs) throws SQLException, DataAccessException {
				EnergyData energyDate = null;
				while(rs.next()) {
					energyDate = new EnergyData();
					if(rs.getObject("hourPower") != null)
						energyDate.setHourPower(rs.getInt("hourPower"));
					if(rs.getObject("DataTime") != null)
						energyDate.setDataTime(rs.getTimestamp("DataTime"));
					if(rs.getObject("BuJianLeiXingID") != null)
						energyDate.setBuJianLeiXingID(rs.getInt("BuJianLeiXingID"));
					if(rs.getObject("BuJianID") != null)
						energyDate.setBuJianID(rs.getInt("BuJianID"));
					if(rs.getObject("BuJianCanShuID") != null)
						energyDate.setBuJianCanShuID(rs.getInt("BuJianCanShuID"));
					if(rs.getObject("ChangZhanID") != null)
						energyDate.setChangZhanID(rs.getInt("ChangZhanID"));
					if(rs.getObject("value") != null)
						energyDate.setValue(rs.getDouble("value"));
					if(rs.getObject("valueFlag") != null)
						energyDate.setValueFlag(rs.getInt("valueFlag"));
				}
				return energyDate;
			}
			
		});
		
		return energyDate;
	}

	/**
	 * By ZhangSC
	 * @param calcData
	 * @param
	 * @return
	 */
	public boolean isNotNull(cal_param.calc_data data) {
		Calendar calendar = Calendar.getInstance();
		Date date=data.getDate();
		if(data.getDate() != null){
			calendar.setTimeInMillis(date.getTime());
		}
		int bujianleixingid = data.getDeviceType();
		int bujiancanshuid = data.getDeviceParam();
		int bujianid = data.getDeviceID();
		int changzhanid = data.getChangZhanID();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);


		String hours = hour+"";
		String months = ""+month;

		if(month<10) {
			months = "0"+month;
		}
		String days = "" + day;
		if(day<10){
			days = "0" + day;
		}
		String minutes = ""+minute;
		if(minute<10){
			minutes = "0" + minutes;
		}
		String riqi = year+"-"+months+"-"+days+" "+"00:"+minutes+":00";

		String tbName = "";
		StringBuffer sql = new StringBuffer(" ");


		if(data.getTargetTable()==1){
			tbName = "ls.ycdata" + year + months;
			sql.append("SELECT h" + hours +" as value" +
					" FROM " + tbName +" WHERE 1=1 ");
				sql.append(" AND BuJianLeiXingID = 20 AND BuJianCanShuID = 2");
			if(bujianid != 0)
				sql.append(" AND BuJianID in (select id from xunidmnliang where dybjtype = ? and" +
						" dybjparam = ? and dybjid = ?)");
			if(changzhanid != 0)
				sql.append(" AND ChangZhanID = ? ");
			if(date != null) {
				sql.append(" AND riqi = ? ");
			}
		}else if(data.getTargetTable()==2){
			tbName = "ls.kwhdata" + year + months;
			sql.append("SELECT h" + hours +" as value" +
					" FROM " + tbName +" WHERE 1=1 ");
			if(bujianleixingid != 0)
				sql.append(" AND BuJianLeiXingID = ? ");
			if(bujiancanshuid != 0)
				sql.append(" AND BuJianCanShuID = ? ");
			if(bujianid != 0)
				sql.append(" AND BuJianID = ? ");
			if(changzhanid != 0)
				sql.append(" AND ChangZhanID = ? ");
			if(date != null) {
				sql.append(" AND riqi = ? ");
			}
		}
		System.out.println(sql);

		Object[] params = {bujianleixingid,bujiancanshuid, bujianid, changzhanid, riqi};
		String value = jdbcTemplate_ccmservicems.query(sql.toString(), params, new ResultSetExtractor<String>(){
			@Override
			public String extractData(ResultSet resultSet) throws SQLException, DataAccessException {
				String value = null;
				while(resultSet.next()) {
					if(resultSet.getObject("value") != null)
						value = resultSet.getString("value");
				}
				return value;
			}
		});
		if(value==null||value==""){
			return false;
		}
		else{
			return true;
		}
	}
	/**
	 * By ZhangSC
	 * @param
	 * @param
	 * @return
	 */
	public boolean notNull(cal_param.calc_data data) {
		Calendar calendar = Calendar.getInstance();
		Date date=data.getDate();
		if(data.getDate() != null){
			calendar.setTimeInMillis(date.getTime());
		}
		int bujianleixingid = data.getDeviceType();
		int bujiancanshuid = data.getDeviceParam();
		int bujianid = data.getDeviceID();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);


		String hours = hour+"";
		String months = ""+month;

		if(month<10) {
			months = "0"+month;
		}
		String days = "" + day;
		if(day<10){
			days = "0" + day;
		}
		String minutes = ""+minute;
		if(minute<10){
			minutes = "0" + minutes;
		}
		String riqi = year+"-"+months+"-"+days+" "+"00:"+minutes+":00";

		String tbName = "";
		StringBuffer sql = new StringBuffer(" ");
		if(data.getTargetTable()==1){
			tbName = "ls.ycdata" + year + months;
		}else if(data.getTargetTable()==2){
			tbName = "ls.kwhdata" + year + months;
		}
		sql.append("SELECT h" + hours +" as value" +
				" FROM " + tbName +" WHERE 1=1 ");
		if(bujianleixingid != 0)
			sql.append(" AND BuJianLeiXingID = ? ");
		if(bujiancanshuid != 0)
			sql.append(" AND BuJianCanShuID = ? ");
			sql.append(" AND BuJianID = ? ");
		if(date != null) {
			sql.append(" AND riqi = ? ");
		}
		//System.out.println(sql);

		Object[] params = {bujianleixingid,bujiancanshuid, bujianid, riqi};
		String value = jdbcTemplate_ccmservicems.query(sql.toString(), params, new ResultSetExtractor<String>(){
			@Override
			public String extractData(ResultSet resultSet) throws SQLException, DataAccessException {
				String value = null;
				while(resultSet.next()) {
					if(resultSet.getObject("value") != null)
						value = resultSet.getString("value");
				}
				return value;
			}
		});
		if(value==null||value==""){
			return false;
		}
		else{
			return true;
		}
	}
	/***
	 * 更新kwh表
	 * @author ZhangSC
	 * @param calcData,date,value
	 * @return
	 */
	public String updateKwhData(cal_param.calc_data data) {
		Calendar calendar = Calendar.getInstance();
		Double value = data.getValue();
		Date date=data.getDate();
		if(date != null){
			calendar.setTimeInMillis(date.getTime());
		}
		int bujianleixingid = data.getDeviceType();
		int bujiancanshuid = data.getDeviceParam();
		int bujianid = data.getDeviceID();
		int changzhanid = data.getChangZhanID();


		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);


		String hours = hour+"";
		String months = ""+month, tbName = "";
		if(month<10) {
			months = "0"+month;
		}
		String days = "" + day;
		if(day<10){
			days = "0" + day;
		}
		String minutes = ""+minute;
		if(minute<10){
			minutes = "0" + minutes;
		}
		String riqi =year+"-"+months+"-"+days+" "+"00:"+minutes+":00";
		if(data.getTargetTable()==1){
			tbName = "ls.ycdata" + year + months;
		}else if(data.getTargetTable()==2){
			tbName = "ls.kwhdata" + year + months;
		}
		StringBuffer sql = new StringBuffer(" ");
		sql.append("update " + tbName +" set h"+ hours +" = ?" +
				" WHERE 1=1 ");
		if(bujianleixingid != 0)
			sql.append(" AND BuJianLeiXingID = ? ");
		if(bujiancanshuid != 0)
			sql.append(" AND BuJianCanShuID = ? ");
			sql.append(" AND BuJianID = ? ");
		if(changzhanid != 0)
			sql.append(" AND ChangZhanID = ? ");
		if(date != null) {
			sql.append(" AND riqi = ? ");
		}

		//System.out.println("时间：h"+hours+" "+riqi+"更新一条数据："+data.getCalcId()+"-"+ bujianleixingid + "-"+bujianid+"-"+bujiancanshuid);
		String error = null;
		try {
			jdbcTemplate_ccmservicems.update(sql.toString(), value,
					bujianleixingid,
					bujiancanshuid,
					bujianid,
					changzhanid,
					riqi
					);
		} catch (Exception e) {
			error = "updateOpenUsePower：更新数据出错，"+e.getMessage()+"；设备id为："+bujianid+"，部件参数："+bujiancanshuid;
		}
		return error;
	}
	/***
	 * 插入kwh表
	 * @author ZhangSC
	 * @param
	 * @return
	 */
	public String insertKwhData(cal_param.calc_data data) {
		Calendar calendar = Calendar.getInstance();
		Date date = data.getDate();
		if(date != null){
			calendar.setTimeInMillis(date.getTime());
		}
		int bujianleixingid = data.getDeviceType();
		int bujiancanshuid = data.getDeviceParam();
		int bujianid = data.getDeviceID();
		int changzhanid = data.getChangZhanID();

		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH)+1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		double [] hourArray = new double[24];
		for(int i=0;i<24;i++){
			if(i==hour){
				hourArray[i]=data.getValue();
			}else{
				hourArray[i]=0.0;
			}
		}
		String hours = hour+"";
		String months = ""+month, tbName = "";
		if(month<10) {
			months = "0"+month;
		}
		String days = "" + day;
		if(day<10){
			days = "0" + day;
		}
		String minutes = ""+minute;
		if(minute<10){
			minutes = "0" + minutes;
		}
		String riqi = year+"-"+months+"-"+days+" "+"00:"+minutes+":00";
		String error = "更新未成功";
		if(data.getTargetTable()==1){
			tbName = "ls.ycdata" + year + months;
		}else if(data.getTargetTable()==2){
			tbName = "ls.kwhdata" + year + months;
		}
		StringBuffer sql = new StringBuffer("INSERT INTO " + tbName +
				" (riqi, BuJianLeiXingID, BuJianCanShuID, BuJianID, ChangZhanID, " +
				"H0,H0Flag,H1,H1Flag,H2,H2Flag,H3,H3Flag,H4,H4Flag,H5,H5Flag,H6,H6Flag," +
				"H7,H7Flag,H8,H8Flag,H9,H9Flag,H10,H10Flag,H11,H11Flag,H12,H12Flag,H13,H13Flag," +
				"H14,H14Flag,H15,H15Flag,H16,H16Flag,H17,H17Flag,H18,H18Flag,H19,H19Flag,H20,H20Flag," +
				"H21,H21Flag,H22,H22Flag,H23,H23Flag) VALUES"
				+ " (?, ?, ?, ?, ?, ?, 0, ?, 0 , ?, 0, ?, 0, ?, 0, ?, 0, ?, 0," +
				" ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0," +
				" ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0);");
		//System.out.println("时间：h"+hours+" "+riqi+"插入一条数据："+data.getCalcId()+"-"+ bujianleixingid + "-"+bujianid+"-"+bujiancanshuid);

		try {
			jdbcTemplate_ccmservicems.update(sql.toString(), riqi,bujianleixingid,bujiancanshuid,bujianid, changzhanid, hourArray[0], hourArray[1],
					hourArray[2],hourArray[3], hourArray[4], hourArray[5],hourArray[6],hourArray[7],hourArray[8],hourArray[9],
					hourArray[10],hourArray[11],hourArray[12],hourArray[13],hourArray[14],hourArray[15],hourArray[16],hourArray[17],hourArray[18],
					hourArray[19],hourArray[20],hourArray[21],hourArray[22],hourArray[23]);
		} catch (Exception e) {
			error = "insertOpenUsePower：保存数据出错，"+e.getMessage()+"；设别id为："+bujianid+"，部件参数："+bujiancanshuid;
		}
		return error;
	}

	/***
	 * 根据基本三段式查找虚拟部件id
	 * @author ZhangSC
	 * @param
	 * @return
	 */
	public Integer getXnbjid(int buJianType,int buJianId,int bujiancanshuId) {
		StringBuffer sb = new StringBuffer();
		sb.append("select id from xunidmnliang where 1=1 ");
		if(buJianType!=0)
			sb.append(" and dybjtype = "+buJianType);
			sb.append(" and dybjid = "+buJianId);
		if(bujiancanshuId!=0)
			sb.append(" and dybjparam = "+bujiancanshuId);
		//System.out.println(sb);
		List<Integer> result = new ArrayList<Integer>();
		try{
			result = jdbcTemplate_ccmservicems.query(sb.toString(), new ResultSetExtractor<List<Integer>>() {
				public List<Integer> extractData(ResultSet rs)  throws SQLException, DataAccessException {
					List<Integer> result = new ArrayList<Integer>();
					while(rs.next()) {
						Integer value = rs.getInt("id");
						result.add(value);
					}
					return result;
				}});
		}catch(Exception e){
			e.printStackTrace();
		}
		if(result.size() == 0)
		{
			return 0;
		}else{
			return result.get(0);
		}
	}
	/***
	 * 根据基本三段式查找虚拟部件id
	 * @author ZhangSC
	 * @param buJianId;buJianType
	 * @return
	 */
	public Integer getChangZhanid(int buJianType,int buJianId) {
		StringBuffer sb = new StringBuffer();
		sb.append("select changzhanid from commdev where 1=1 ");
		if(buJianType!=0)
			sb.append(" and bjlxid = "+buJianType);
			sb.append(" and id = "+buJianId);
		//System.out.println(sb);
		List<Integer> result = new ArrayList<Integer>();
		try{
			result = jdbcTemplate_ccmservicems.query(sb.toString(), new ResultSetExtractor<List<Integer>>() {
				public List<Integer> extractData(ResultSet rs)  throws SQLException, DataAccessException {
					List<Integer> result = new ArrayList<Integer>();
					while(rs.next()) {
						Integer value = rs.getInt("changzhanid");
						result.add(value);
					}
					return result;
				}});
		}catch(Exception e){
			e.printStackTrace();
		}
		if(result.size() == 0)
		{
			return 0;
		}else{
			return result.get(0);
		}
	}



	public List<jk_Conn> getJkConnInfo(){
		List<jk_Conn> connList = new ArrayList<jk_Conn>();
		StringBuffer sb= new StringBuffer();
		sb.append("select conn_id,conn_name,req_url," +
				"req_param,bjtype,i_device_label,result_name,req_result from jk_conn where 1=1 and startType = 1;") ;
		try {
			connList = jdbcTemplate_ccmservicems.query(sb.toString(),
					new RowMapper<jk_Conn>() {
						public jk_Conn mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							jk_Conn jkConn = new jk_Conn();
							jkConn.setConn_id(rs.getInt("conn_id"));
							jkConn.setConn_name(rs.getString("conn_name"));
							jkConn.setReq_url(rs.getString("req_url"));
							jkConn.setReq_param(rs.getString("req_param"));
							jkConn.setBjtype(rs.getInt("bjtype"));
							jkConn.setI_device_label(rs.getString("i_device_label"));
							jkConn.setResult_name(rs.getString("result_name"));
							jkConn.setReq_result(rs.getString("req_result"));
							return jkConn;
						}
					});
		} catch (Exception e) {
			System.out.println("获取连接配置信息异常:" + e.toString());
			return null;
		}
		return connList;
	}
	public Map<String,String> getConnDeviceInfo(jk_Conn conn)
	{
		Map<String,String> connDeviceMap = new HashMap<String,String>();
		StringBuffer sb= new StringBuffer();
		sb.append("select i_device_code,bjid from conn_device where 1=1 ");

		if(conn!=null)
		{
			if(conn.getConn_id()!=null)
				sb.append("and conn_id ="+conn.getConn_id());
		}
		//System.out.println(sb.toString());
		try {
			connDeviceMap = jdbcTemplate_ccmservicems.query(sb.toString(),
					new ResultSetExtractor<Map<String,String>>() {
						public Map<String,String> extractData(ResultSet rs) throws SQLException,DataAccessException {
							Map<String,String> connDevice = new HashMap<String,String>();
							while(rs.next()) {
								String deviceCode = rs.getString("i_device_code");
								int deviceId = rs.getInt("bjid");
								connDevice.put(deviceCode,String.valueOf(deviceId));
							}
							return connDevice;
						}});
		} catch (Exception e) {
			System.out.println("getAllCalcInfo()异常:" + e.toString());
			return null;
		}
		return connDeviceMap;
	}
	public List<conn_param> getConnParamInfo(jk_Conn conn)
	{
		List<conn_param> connParamList = new ArrayList<conn_param>();
		StringBuffer sb= new StringBuffer();
		sb.append("select i_param_Label,bjparam,data_type,data_format,fCoef from conn_param where 1=1 ");

		if(conn!=null)
		{
			if(conn.getConn_id()!=null)
				sb.append("and conn_id ="+conn.getConn_id());
		}
		//System.out.println(sb.toString());
		try {
			connParamList = jdbcTemplate_ccmservicems.query(sb.toString(),
					new RowMapper<conn_param>() {
						public conn_param mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							conn_param connParam = new conn_param();
							connParam.setI_param_label(rs.getString("i_param_Label"));
							connParam.setBjparam(rs.getInt("bjparam"));
							connParam.setData_type(rs.getInt("data_type"));
							connParam.setData_format(rs.getInt("data_format"));
							connParam.setFCoef(rs.getDouble("fCoef"));
							return connParam;
						}
					});
		} catch (Exception e) {
			System.out.println("getAllCalcInfo()异常:" + e.toString());
			return null;
		}
		return connParamList;
	}
	public List<air_Push> getAirDataPushInfo()
	{
		List<air_Push> airPushList = new ArrayList<air_Push>();
		StringBuffer sb= new StringBuffer();
		sb.append("select id,i_data_label,pointNumber,data,data_source,i_data_scada,fCoef,maxPositiveValue,minPositiveValue,maxNegativeValue,minNegativeValue from air_push where 1=1;");
		//System.out.println(sb.toString());
		try {
			airPushList = jdbcTemplate_ccmservicems.query(sb.toString(),
					new RowMapper<air_Push>() {
						public air_Push mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							air_Push airPush = new air_Push();
							airPush.setId(rs.getInt("id"));
							airPush.setI_data_label(rs.getString("i_data_label"));
							airPush.setPointNumber(rs.getInt("pointNumber"));
							airPush.setData_source(rs.getInt("data_source"));
							airPush.setI_data_scada(rs.getString("i_data_scada"));
							airPush.setFCoef(rs.getDouble("fCoef"));
							airPush.setMaxPositiveValue(rs.getDouble("maxPositiveValue"));
							airPush.setMinPositiveValue(rs.getDouble("minPositiveValue"));
							airPush.setMaxNegativeValue(rs.getDouble("maxNegativeValue"));
							airPush.setMinNegativeValue(rs.getDouble("minNegativeValue"));
							return airPush;
						}
					});
		} catch (Exception e) {
			System.out.println("getAllCalcInfo()异常:" + e.toString());
			return null;
		}
		return airPushList;
	}

	/**
	 * Author By ZhangSC
	 * 国电智深
	 * 获取智深AirDB初始化接口信息
	 * @return
	 */
	public List<air_Conn> getAirConnInfo(){
		List<air_Conn> connList = new ArrayList<air_Conn>();
		StringBuffer sb= new StringBuffer();
		sb.append("select conn_id,conn_name,req_url,req_param,result_name from air_conn where 1=1 and startType = 1;") ;
		try {
			connList = jdbcTemplate_ccmservicems.query(sb.toString(),
					new RowMapper<air_Conn>() {
						public air_Conn mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							air_Conn airConn = new air_Conn();
							airConn.setConn_id(rs.getInt("conn_id"));
							airConn.setConn_name(rs.getString("conn_name"));
							airConn.setReq_url(rs.getString("req_url"));
							airConn.setReq_param(rs.getString("req_param"));
							airConn.setResult_name(rs.getString("result_name"));
							return airConn;
						}
					});
		} catch (Exception e) {
			System.out.println("获取连接配置信息异常:" + e.toString());
			return null;
		}
		return connList;
	}
	/**
	 * Author By ZhangSC
	 * 国电智深
	 * 通过电站id获取电站信息
	 * @return
	 */
	public List<StationInfo> getStationInfo(int changzhanId){
		List<StationInfo> stationInfoList = new ArrayList<StationInfo>();
		StringBuffer sb= new StringBuffer();
		sb.append("select ChangZhanID,StationName,StationCode,StationType,InstallCapacity,PoorNum,StationLat,StationLong,startDate,EndDate,OperationTime from " +
				"stationinfo where changzhanid ="+changzhanId) ;
		try {
			stationInfoList = jdbcTemplate_ccmservicems.query(sb.toString(),
					new RowMapper<StationInfo>() {
						public StationInfo mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							StationInfo stationInfo = new StationInfo();
							stationInfo.setChangZhanID(rs.getInt("ChangZhanID"));
							stationInfo.setStationName(rs.getString("StationName"));
							stationInfo.setStationCode(rs.getString("StationCode"));
							stationInfo.setInstallCapacity(rs.getFloat("InstallCapacity"));
							stationInfo.setOperationTime(rs.getDate("OperationTime"));
							return stationInfo;
						}
					});
		} catch (Exception e) {
			System.out.println("获取电站信息出错:" + e.toString());
			return null;
		}
		return stationInfoList;
	}
	/**
	 * Author By ZhangSC
	 * 国电智深
	 * 获取智深AirDB初始化参数对应表
	 * @return
	 */
	public Map<String,String> getAirParamInfo()
	{
		Map<String,String> airParamMap = new HashMap<String,String>();
		StringBuffer sb= new StringBuffer();
		sb.append("select i_data_label,i_data_scada,fCoef,maxPositiveValue,minPositiveValue,maxNegativeValue,minNegativeValue from air_param where 1=1;") ;
		try {
			airParamMap = jdbcTemplate_ccmservicems.query(sb.toString(),
					new ResultSetExtractor<Map<String,String>>() {
						public Map<String,String> extractData(ResultSet rs) throws SQLException,DataAccessException {
							Map<String,String> airParam = new HashMap<String,String>();
							while(rs.next()) {
								String dataLabel = rs.getString("i_data_label");
								String dataScada = rs.getString("i_data_scada");
								double fCoef = rs.getDouble("fCoef");
								double maxPos = rs.getDouble("maxPositiveValue");
								double minPos = rs.getDouble("minPositiveValue");
								double maxNeg = rs.getDouble("maxNegativeValue");
								double minNeg = rs.getDouble("minNegativeValue");
								String valueParam = dataScada+","+fCoef+","+maxPos+","+minPos;
								airParam.put(dataLabel,valueParam);
							}
							return airParam;
						}});
		} catch (Exception e) {
			System.out.println("getAllCalcInfo()异常:" + e.toString());
			return null;
		}
		return airParamMap;
	}

	/**
	 * Author By ZhangSC
	 * 国电智深
	 * 获取AirDb数据回传信息
	 * @return
	 */
	public Map<String,String> getAirDbPushInfo()
	{
		Map<String,String> airPushMap = new HashMap<String,String>();
		StringBuffer sb= new StringBuffer();
		sb.append("select id,i_data_label,pointNumber,data,i_data_scada,fCoef,maxPositiveValue,minPositiveValue,maxNegativeValue,minNegativeValue from air_push where 1=1;") ;
		try {
			airPushMap = jdbcTemplate_ccmservicems.query(sb.toString(),
					new ResultSetExtractor<Map<String,String>>() {
						public Map<String,String> extractData(ResultSet rs) throws SQLException,DataAccessException {
							Map<String,String> airPush = new HashMap<String,String>();
							while(rs.next()) {
								int id = rs.getInt("id");
								String pointNumber = rs.getString("pointNumber");
								String data = String.valueOf(rs.getFloat("data"));
								String dataLabel = rs.getString("i_data_label");
								String dataScada = rs.getString("i_data_scada");
								double fCoef = rs.getDouble("fCoef");
								double maxPos = rs.getDouble("maxPositiveValue");
								double minPos = rs.getDouble("minPositiveValue");
								double maxNeg = rs.getDouble("maxNegativeValue");
								double minNeg = rs.getDouble("minNegativeValue");
								airPush.put(pointNumber,data);
							}
							return airPush;
						}});
		} catch (Exception e) {
			System.out.println("getAllCalcInfo()异常:" + e.toString());
			return null;
		}
		return airPushMap;
	}
	/**
	 * Author By ZhangSC
	 * 国电智深
	 * 获取智深AirDB初始化参数对应表
	 * @return
	 */
	public Map<String,String> getAirParamExceptChangZhan()
	{
		Map<String,String> airParamMap = new HashMap<String,String>();
		StringBuffer sb= new StringBuffer();
		sb.append("select i_data_label,i_data_scada from air_param where 1=1;") ;
		try {
			airParamMap = jdbcTemplate_ccmservicems.query(sb.toString(),
					new ResultSetExtractor<Map<String,String>>() {
						public Map<String,String> extractData(ResultSet rs) throws SQLException,DataAccessException {
							Map<String,String> airParam = new HashMap<String,String>();
							while(rs.next()) {
								String dataLabel = rs.getString("i_data_label");
								String dataScada = rs.getString("i_data_scada");
								int num=0;
								while((dataScada.indexOf("-", num))!=-1){
									num=dataScada.indexOf("-",num)+1;
								}
								String sdataScada=(dataScada.subSequence(0,num-1)).toString();
								airParam.put(sdataScada,dataLabel);
							}
							return airParam;
						}});
		} catch (Exception e) {
			System.out.println("getAllCalcInfo()异常:" + e.toString());
			return null;
		}
		return airParamMap;
	}
	/**
	 * Author ZhangSC
	 * 获取虚拟单晶模拟量表map<三段式，xuniid>	 * @param conn
	 * @return
	 */
	public Map<String,Integer> getXuniMap()
	{
		Map<String,Integer> xuniMap = new HashMap<String,Integer>();
		StringBuffer sb= new StringBuffer();
		sb.append("select id,dybjType,dybjid,dybjparam,changzhanid from xunidmnliang where 1=1");

		try {
			xuniMap = jdbcTemplate_ccmservicems.query(sb.toString(),
					new ResultSetExtractor<Map<String,Integer>>() {
						public Map<String,Integer> extractData(ResultSet rs) throws SQLException,DataAccessException {
							Map<String,Integer> xuni = new HashMap<String,Integer>();
							while(rs.next()) {
								int xuniId = rs.getInt("id");
								int bjType = rs.getInt("dybjType");
								int bjId = rs.getInt("dybjid");
								int bjParam = rs.getInt("dybjparam");
								int changzhanId = rs.getInt("changzhanid");
								xuni.put("2-"+bjType+"-"+bjId+"-"+bjParam+"-"+changzhanId,xuniId);
							}
							return xuni;
						}});
		} catch (Exception e) {
			System.out.println("getAllCalcInfo()异常:" + e.toString());
			return null;
		}
		return xuniMap;
	}

	public String updateAirPushData(double value, String i_data_label){

		String error = "";
		String tbName = "air_Push";
		StringBuffer sql = new StringBuffer("update " + tbName +
				" set data = ? where i_data_label = ?");
		//System.out.println("时间：h"+hours+" "+riqi+"插入一条数据："+data.getCalcId()+"-"+ bujianleixingid + "-"+bujianid+"-"+bujiancanshuid);

		try {
			jdbcTemplate_ccmservicems.update(sql.toString(), value,i_data_label);
		} catch (Exception e) {
			error = "insertOpenUsePower：保存数据出错，"+e.getMessage()+"；部件参数："+i_data_label;
		}
		return error;
	}
	/**
	 * Author By ZhangSC
	 * 国电智深
	 * 获取智深AirDB全部参数信息
	 * @return
	 */
	public List<air_Param> getAirParamList(){
		List<air_Param> paramList = new ArrayList<air_Param>();
		StringBuffer sb= new StringBuffer();
		sb.append("select i_data_scada,i_data_label,fCoef,maxPositiveValue,minPositiveValue,maxNegativeValue,minNegativeValue,mingzi,i_data_type from air_param where 1=1;") ;
		try {
			paramList = jdbcTemplate_ccmservicems.query(sb.toString(),
					new RowMapper<air_Param>() {
						public air_Param mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							air_Param airParam = new air_Param();
							airParam.setI_data_label(rs.getString("i_data_label"));
							airParam.setI_data_scada(rs.getString("i_data_scada"));
							airParam.setMaxPositiveValue(rs.getDouble("maxPositiveValue"));
							airParam.setMinPositiveValue(rs.getDouble("minPositiveValue"));
							airParam.setMinNegativeValue(rs.getDouble("maxNegativeValue"));
							airParam.setMaxNegativeValue(rs.getDouble("maxNegativeValue"));
							airParam.setMingzi(rs.getString("mingzi"));
							airParam.setI_data_type(rs.getInt("i_data_type"));
							return airParam;
						}
					});
		} catch (Exception e) {
			System.out.println("获取连接配置信息异常:" + e.toString());
			return null;
		}
		return paramList;
	}
}
