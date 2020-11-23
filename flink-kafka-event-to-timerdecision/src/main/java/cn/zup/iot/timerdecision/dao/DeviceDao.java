/********************************************************************
 *	版权所有 (C) 2015-2020 积成能源
 *	保留所有版权
 *	
 *	作者：	liuxf
 *	日期：	2016-6-5
 *	摘要：	设备查询类
 *  功能：           查找区域、园区/场站、容器、设备、采集器
 *
 *********************************************************************/
package cn.zup.iot.timerdecision.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import cn.zup.iot.timerdecision.util.DataSourceUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import cn.zup.iot.timerdecision.model.Commdev;
import cn.zup.iot.timerdecision.model.PmWarnRecord;
import cn.zup.iot.timerdecision.model.StationInfo;
import cn.zup.iot.timerdecision.model.YXData;
import cn.zup.iot.timerdecision.service.settings.BJLX;
import cn.zup.iot.timerdecision.service.settings.HuaWeiSunParam;
import cn.zup.iot.timerdecision.service.settings.RegionType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Component
public class DeviceDao implements Serializable {
	private JdbcTemplate jdbcTemplate= new JdbcTemplate((new DataSourceUtils()).getDataSource1());

//	public JdbcTemplate getJdbcTemplate() {
//		return jdbcTemplate;
//	}
//
//	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
//		this.jdbcTemplate = jdbcTemplate;
//	}
	/**
     * 获取遥信数据
     * @param id
     * @return
     */
    public YXData getYXXNData(int id, String time)
	{
		List<YXData> lists = new ArrayList<YXData>();
		//将传过来的当前日期加15分钟
		SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		java.util.Date date;
		try {
			date = sdf.parse(time);
			Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
			today.setTimeInMillis(date.getTime() + 900);
			int time_year = today.get(Calendar.YEAR);
			int time_month = today.get(Calendar.MONTH);
			int time_day = today.get(Calendar.DAY_OF_MONTH);
			int time_hour = today.get(Calendar.HOUR_OF_DAY);
			int time_minute = today.get(Calendar.MINUTE);
			time_month +=1;
			time_minute -= time_minute%5;
			//将时间转为String类型
			String year = String.valueOf(time_year);
		    String month = String.valueOf(time_month);
		    if(time_month<10)
		    	month = "0"+month;
		    String day = String.valueOf(time_day);
		    if(time_day<10)
		    	day = "0"+day;
		    String hour = String.valueOf(time_hour);
		    if(time_hour>18)
				hour="18";
		    String Hour = "h"+hour;
		    String fen = String.valueOf(time_minute)+":00";
		    if(time_minute<10){
		    	fen="0"+time_minute+":00";
		    }
		    String tableName = "ycdata" + year + month;
		    StringBuffer sb= new StringBuffer();


	    	sb.append(" select a.*,b.StationName,b.StationCode,d.MingZi as cityName,e.MingZi as proviceName,g.voltage as voltage,h.circuit as circuit from xunizhtliang a  " +
	    				" left join stationinfo b on a.ChangZhanID=b.ChangZhanID  " +
	    				" left join changzhan c on c.id = b.ChangZhanID  " +

	    				" left join subnet_ems d on d.id = c.SUBNETID  " +//乡镇
	    				" left join subnet_ems e on e.id=d.parentId "+//	区县
	    				" left join subnet_ems ee on ee.id=e.parentId and ee.parentId=0  "+//市

	    				"left join (select "+Hour+" AS voltage from ls."+tableName+" where riqi = '"+ year +"-"+ month +"-"+ day +" 00:"+fen+"' AND BuJianLeiXingID = 20 AND BuJianCanShuID = 2 AND bujianid IN "+
	    				"( select id as bujianid from ms.xunidmnliang where dybjtype = "+BJLX.huaweinibianqi.getValue()+" and dybjid = "+
	    				"(select a.dybjid from xunizhtliang a where a.id = "+id+" )"+
	    				" and dybjparam = "+HuaWeiSunParam.adianya.getValue()+" )) g on 1=1 "+

	    				"left join (select "+Hour+" AS circuit from ls."+tableName+" where riqi = '"+ year +"-"+ month +"-"+ day +" 00:"+fen+"' AND BuJianLeiXingID = 20 AND BuJianCanShuID = 2 AND bujianid IN "+
	    				"( select id as bujianid from ms.xunidmnliang where dybjtype = "+BJLX.huaweinibianqi.getValue()+" and dybjid = "+
	    				"(select a.dybjid from xunizhtliang a where a.id = "+id+" )"+
	    				" and dybjparam = "+HuaWeiSunParam.adianliu.getValue()+" )) h on 1=1"+

	    				" where a.id = "+id);
//	    	System.out.println(sb.toString());
			try {
				lists = jdbcTemplate.query(sb.toString(),
						new RowMapper<YXData>() {
							public YXData mapRow(ResultSet rs, int rowNum)
									throws SQLException {
								YXData yxData = new YXData();
								yxData.setBuJianId(rs.getInt("DYBJID"));
								yxData.setBuJianCanShu(rs.getInt("DYBJParam"));
								yxData.setChangZhanId(rs.getInt("ChangZhanId"));
								yxData.setBuJianLeiXing(rs.getInt("DYBJType"));
								yxData.setStationName(rs.getString("StationName"));
								yxData.setStationCode(rs.getString("StationCode"));
								yxData.setCityName(rs.getString("cityName"));
								yxData.setProviceName(rs.getString("proviceName"));
								yxData.setVoltage(rs.getFloat("voltage"));
								yxData.setCircuit(rs.getFloat("circuit"));
								return yxData;
							}
						});
			} catch (Exception e) {
				System.out.println("GetArea()异常:" + e.toString());
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//int today_year = today.get(Calendar.YEAR);
		if(lists.size()>0)
			return lists.get(0);
		else
			return null;
	}


	/***
	 * 将pms中告警信息同步到monitor中
	 * @param pmWarnRecord
	 * @return
	 */
    public void InsertWarnRecordToMonitor(PmWarnRecord pmWarnRecord)
	{
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sql="";
		try {
			sql ="INSERT INTO WARNRECORD (WARN_RECORD_ID,EQUIPMENT_ID,ASSET_ID,REAL_WARN_ID,WARN_NAME,WARN_SET_ID,WARN_LEVEL" +
					",WARN_TYPE,STATION_ID,OCCUR_TIME,OCCUR_RECOVER,WARN_STATUS,STATION_NAME,EQUIPMENT_NAME,WARN_SOURCE,PROVINCENAME,CITYNAME" +
					",EQUIPMENT_CODE,SEND_STATE,VOLTAGE,CIRCUIT,RECOVER_WAY) VALUES(";
			sql +=pmWarnRecord.getWarn_Record_Id()==null?0+",": pmWarnRecord.getWarn_Record_Id()+",";//告警ID
			sql +=pmWarnRecord.getEquipment_Id()==null?0+",": pmWarnRecord.getEquipment_Id()+",";//设备ID
			sql +=pmWarnRecord.getAsset_Id()==null?0+",": pmWarnRecord.getAsset_Id()+",";// 资产ID
			sql +=pmWarnRecord.getReal_Warn_Id()==null?0+",": pmWarnRecord.getReal_Warn_Id()+",";//
			sql +=pmWarnRecord.getWarn_Name()==null?"'',": "'"+pmWarnRecord.getWarn_Name()+"',";//
			sql +=pmWarnRecord.getWarn_Set_Id()==null?0+",": pmWarnRecord.getWarn_Set_Id()+",";//
			sql +=pmWarnRecord.getWarn_Level()==null?0+",": pmWarnRecord.getWarn_Level()+",";//
			sql +=pmWarnRecord.getWarn_Type()==null?0+",": pmWarnRecord.getWarn_Type()+",";//
			sql +=pmWarnRecord.getStation_Id()==null?0+",": pmWarnRecord.getStation_Id()+",";//
			sql +=pmWarnRecord.getOccur_Time()==null?"str_to_date('2016-10-24 01:01:01','%Y-%m-%d %H:%i:%s'),": "str_to_date('"+sdf.format(pmWarnRecord.getOccur_Time())+"','%Y-%m-%d %H:%i:%s'),";//
			sql +=pmWarnRecord.getOccur_Recover()==null?"NULL,": "str_to_date('"+sdf.format(pmWarnRecord.getOccur_Recover())+"','%Y-%m-%d %H:%i:%s'),";//
			sql +=pmWarnRecord.getWarn_Status()==null?0: pmWarnRecord.getWarn_Status()+",";//
			sql +=pmWarnRecord.getStation_Name()==null?"'',": "'"+pmWarnRecord.getStation_Name()+"',";// 电站名称
			sql +=pmWarnRecord.getEquipment_Name()==null?"'',": "'"+pmWarnRecord.getEquipment_Name()+"',";// 设备名称
			sql +=pmWarnRecord.getWarn_Source()==null?0+",": pmWarnRecord.getWarn_Source()+",";// 告警来源
			sql +=pmWarnRecord.getProvinceName()==null?"'',": "'"+pmWarnRecord.getProvinceName()+"',";// 省名称
			sql +=pmWarnRecord.getCityName()==null?"'',": "'"+pmWarnRecord.getCityName()+"',";// 城市名称
			sql +=pmWarnRecord.getEquipment_Code()==null?"'',": "'"+pmWarnRecord.getEquipment_Code()+"',";// 设备编号
			sql +=pmWarnRecord.getSend_State()==null?0: pmWarnRecord.getSend_State()+",";// 发送状态
			sql +=pmWarnRecord.getVoltage()==null?0+",": pmWarnRecord.getVoltage()+",";//电压
			sql +=pmWarnRecord.getCircuit()==null?0+",": pmWarnRecord.getCircuit()+",";//电流
			sql +=pmWarnRecord.getRecover_Way()==null?0:0;//电流
			sql +=")";
			final String sqlStr = sql;
			System.out.println(sqlStr);
		 	jdbcTemplate.update(sqlStr);

		} catch (Exception e) {
			System.out.println("InsertWarnRecord()异常:" + e.toString());
		}
	}

	/***
	 * 更新数据库告警结束时间
	 * @param  warnRecordId
	 * @param  nowDate
	 * @return
	 */
	public void updateWarnRecordToMonitor(Integer warnRecordId, String nowDate)
	{
		String sql="";
		int recoverWay = 1;
		try {
			sql = "update warnrecord set occur_recover = str_to_date('"+nowDate+"','%Y-%m-%d %H:%i:%s'), warn_status = 5, recover_Way = '"+recoverWay+"', SEND_STATE = 2 where warn_record_id = '"+warnRecordId+"'";
			final String sqlStr = sql;
			System.out.println(sqlStr);
		 	jdbcTemplate.update(sqlStr);
		} catch (Exception e) {
			System.out.println("InsertWarnRecord()异常:" + e.toString());
		}
	}

	/**
	 * 获取区域下电站的详细信息
	 * mxf
	 * 2017年9月9日14:15:16
	 * @param regionType
	 * @param regionId
	 * @return
	 */
	public List<StationInfo> getStationInfolist(int regionType, int regionId){
		StringBuilder sb = new StringBuilder();
		sb.append(" select t1.ID,t1.MingZi,t2.InstallCapacity,t3.MingZi,t4.MingZi,t5.MingZi,t2.batch,t6.mingzi ,t2.memo from changzhan t1 " +
				" inner join stationinfo t2 on t1.ID=t2.ChangZhanID" +
				" inner join dianwang t6 on t1.netid=t6.id "+//关联所属单位
				" INNER join subnet_ems t3 on t1.SUBNETID = t3.ID "+//乡镇
					" inner join subnet_ems t4 on t3.parentId=t4.ID "+//区县
					" inner join subnet_ems t5 on t4.parentId = t5.ID "+//市
					" where t2.stationstat=4");
		if(regionType == RegionType.city.getValue()){//市
			sb.append(" and t5.id="+regionId);
		}else if(regionType == RegionType.county.getValue()){//区县
			sb.append(" and t4.id="+regionId);
		}else if(regionType == RegionType.town.getValue()){//乡镇
			sb.append(" and t3.id="+regionId);
		}else if(regionType == RegionType.village.getValue()){//村
			sb.append(" and t1.id="+regionId);
		}
		System.out.println(sb);
		return jdbcTemplate.query(sb.toString(),
	    		new RowMapper<StationInfo>(){
	    	public StationInfo mapRow(ResultSet rs,int index) throws SQLException {
	    		StationInfo stationInfo = new StationInfo();
	    		stationInfo.setChangZhanID(rs.getInt(1));//电站id
	    		stationInfo.setStationName(rs.getString(2));//电站名字
	    		stationInfo.setInstallCapacity(rs.getFloat(3));//电站装机容量
	    		stationInfo.setTownName(rs.getString(4));//所属乡镇
	    		stationInfo.setCountyName(rs.getString(5));//所属区县
	    		stationInfo.setCityName(rs.getString(6));//所属城市
	    		stationInfo.setBatch(rs.getInt(7));//批次
	    		stationInfo.setProject(rs.getString(8));//所属项目
	    		stationInfo.setMemo(rs.getString(9));//说明
	        	return stationInfo;
	    	}
	    });
	}

	/*
	 * desc:获取通用设备信息
	 * Author：samson
	 * Date: 2016.06.28
	 *
	 * */
    public List<Commdev> getCommdevInfo(String changzhanID,String bjlxID,String bjID) {
    	  String sqlStr = "select BJLXID,ID,ChangZhanID,MingZi,ShuoMing,'' Bujianleixingname,'' SubNetId from commdev where 1=1 ";
  		if(changzhanID != null && changzhanID != ""&& changzhanID !="0")
  			sqlStr += " and ChangZhanID ="+changzhanID;
  		if(bjlxID != null && bjlxID != ""&& bjlxID !="0")
  			sqlStr += " and BJLXID ="+bjlxID;
  		if(bjID != null && bjID != ""&& bjID !="0")
  			sqlStr += " and ID ="+bjID;
          return jdbcTemplate.query(sqlStr,
          		new RowMapper<Commdev>(){
  	        	public Commdev mapRow(ResultSet rs,int index) throws SQLException {
  	            	return new Commdev(rs.getInt("ID"),rs.getInt("BJLXID"),rs.getInt("ChangZhanID"),rs.getString("MingZi"),rs.getString("SubNetId"),rs.getString("Bujianleixingname"),rs.getString("ShuoMing"));
  	        	}
          });
	}

    /**
	 * 获取逆变器工作的组串
	 * mxf
	 * 2017年10月8日16:07:46
	 * @param bujianid
	 * @param arr
	 * @return
	 */
	public List<Integer> getWorkedZuChuan(Integer bujianid, List<Integer> arr) {
		System.out.println("------->" + bujianid) ;
		String sql = "select shuoming from devconfig where id=" + bujianid;
		List<String> lists = jdbcTemplate.query(sql,
				new RowMapper<String>() {
					public String mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						return rs.getString("shuoming");
					}
				});
		System.out.println("------>shuoming" + lists);
		if(lists == null || lists.size() == 0 ) {
			return arr;
		}else {
			if("".equals(lists.get(0)) || lists.get(0) == null || " ".equals(lists.get(0))) {
				return arr;
			}else {
				List<Integer> result = new LinkedList<Integer>();
				String[] shuomings = lists.get(0).split(",");
				for(int i=0;i<shuomings.length;i++) {
					for (int m=0;m<arr.size();m++) {
						if(arr.get(m).equals(Integer.valueOf(shuomings[i]))) {
							result.add(arr.get(m));
							break;
						}
					}
				}
				return result;
			}
		}
	}

	/**
	 * 获取区域下的逆变器
	 * @param regionType
	 * @param regionId
	 * @return
	 */
	public List<Commdev> getCommdevList(int regionType, int regionId) {
		StringBuffer sb = new StringBuffer();
		sb.append(" select c.BJLXID, c.id, c.changzhanid, c.shuoming from commdev c where c.bjlxid in (26, 28) and c.changzhanid in(");
		sb.append(" select t.id from changzhan t inner join subnet_ems t1 on t.subnetid = t1.id" );//关联乡镇
		sb.append(" inner join subnet_ems t2 on t1.parentid = t2.id ");//关联区县
		sb.append(" inner join subnet_ems t3 on t2.parentid = t3.id where 1=1");//关联城市

		if(regionType == RegionType.city.getValue()) {//市
			sb.append(" and t3.id=" + regionId);
		} else if (regionType == RegionType.county.getValue()) {
			sb.append(" and t2.id=" + regionId);
		} else if(regionType == RegionType.town.getValue()) {
			sb.append(" and t1.id=" + regionId);
		} else if(regionType == RegionType.village.getValue()) {
			sb.append(" and t.id=" + regionId);
		}

		sb.append(")");
		System.out.println(sb);
		List<Commdev> lists = jdbcTemplate.query(sb.toString(),
				new RowMapper<Commdev>() {
					public Commdev mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						Commdev commdev = new Commdev();
						commdev.setChangZhanID(rs.getInt("changzhanid"));
						commdev.setBJLXID(rs.getInt("bjlxid"));
						commdev.setID(rs.getInt("id"));
						commdev.setShuoMing(rs.getString("shuoming"));
						return commdev;
					}
				});
		return lists;
	}
	
}
