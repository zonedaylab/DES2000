package cn.zup.iot.timerdecision.dao;

import cn.zup.iot.timerdecision.model.PmDiagnosis;
import cn.zup.iot.timerdecision.model.PmStationGroup;
import cn.zup.iot.timerdecision.model.PmWarnRecord;
import cn.zup.iot.timerdecision.model.WeatherInfo;
import cn.zup.iot.timerdecision.service.settings.StationChannelId;
import cn.zup.iot.timerdecision.util.JdbcTemplateUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/***
 * 决策树接口
 * @author samson
 *
 */
public class DiagnosisDao{
	private JdbcTemplate jdbcTemplateDia= JdbcTemplateUtils.jdbcTemplatePms;

//	public JdbcTemplate getJdbcTemplateDia() {
//		return jdbcTemplateDia;
//	}
//
//	public void setJdbcTemplateDia(JdbcTemplate jdbcTemplateDia) {
//		this.jdbcTemplateDia = jdbcTemplateDia;
//	}

	/**
	 * 根据电站id获取所在分组下所有电站信息 
	 * @param stationId
	 * @return
	 */
	public List<PmStationGroup> getGroupStationList(Integer stationId) {
		List<PmStationGroup> pmStationGroupList = new ArrayList<PmStationGroup>();		
		String sqlString ; 
		sqlString=" select park.PARK_GROUP_ID,group_concat(park.PARK_ID) station_Ids,park.PARK_GROUP_NAME from " +
				" 	( select A.PARK_GROUP_ID,PARK_ID,A.PARK_GROUP_NAME from PM_PARK_GROUP_ITEM a left join PM_PARK_GROUP b on a.PARK_GROUP_ID = b.PARK_GROUP_ID )  park  " +
				"	where park.PARK_GROUP_ID in ( select A.PARK_GROUP_ID from PM_PARK_GROUP_ITEM a left join PM_PARK_GROUP b on a.PARK_GROUP_ID = b.PARK_GROUP_ID  " ;

		if(stationId!=null && stationId!=0)
		{
			sqlString += " where a.park_id  ="+stationId;
		}
		sqlString += " ) group by park.PARK_GROUP_ID,park.PARK_GROUP_NAME ";
		//System.out.println(sqlString);
		try {
			pmStationGroupList = jdbcTemplateDia.query(sqlString,
					new RowMapper<PmStationGroup>() {
						public PmStationGroup mapRow(ResultSet rs, int rowNum)
							throws SQLException {
							PmStationGroup pmStationGroup = new PmStationGroup();
							pmStationGroup.setPark_Group_Id(rs.getInt("PARK_GROUP_ID"));
							pmStationGroup.setStation_Ids(rs.getString("station_Ids"));
							pmStationGroup.setPark_Group_Name(rs.getString("PARK_GROUP_NAME"));
							return pmStationGroup;
						}
					});
		} catch (Exception e) {
			System.out.println("GetWarnRecordFlag()异常:" + e.toString());
			return null;
		}
		return pmStationGroupList;
	}
	/***
	 * 获取当天诊断实体信息  只判断日期 不判断小时
	 * @param deviceId
	 * @param dateString
	 * @return
	 */
	public PmDiagnosis getDayDiagnosisInfo(Integer deviceId,String dateString){

		List<PmDiagnosis> pmDiagnosisList = new ArrayList<PmDiagnosis>();		
		String sqlString ; 
		sqlString="  select result_Id, device_Id,diagnosis_Result,diagnosis_Time from PM_DIAGNOSIS where 1=1 " ;

		if(deviceId!=null && deviceId!=0)
		{
			sqlString += " and PM_DIAGNOSIS.device_Id="+deviceId;
		}
		if(!dateString.equals(""))
		{
			sqlString += " and DATE_FORMAT(PM_DIAGNOSIS.diagnosis_Time,'%Y-%m-%d')= '"+dateString+"' ";
		}
		//System.out.println(sqlString);
		try {
			pmDiagnosisList = jdbcTemplateDia.query(sqlString,
					new RowMapper<PmDiagnosis>() {
						public PmDiagnosis mapRow(ResultSet rs, int rowNum)
							throws SQLException {
							PmDiagnosis pmDiagnosis = new PmDiagnosis();
							pmDiagnosis.setResult_Id(rs.getInt("result_Id"));
							pmDiagnosis.setDevice_Id(rs.getInt("device_Id"));
							pmDiagnosis.setDiagnosis_Result(rs.getString("diagnosis_Result"));
							pmDiagnosis.setDiagnosis_Time(rs.getDate("diagnosis_Time"));
							return pmDiagnosis;
						}
					});
		} catch (Exception e) {
			System.out.println("GetWarnRecordFlag()异常:" + e.toString());
		}
		if(pmDiagnosisList.size()==0)
			return null;
		else
			return pmDiagnosisList.get(0);	
	}
	
	/**
	 * 更新诊断实体信息
	 * @param diagnosis
	 */
	public void editDiagnosis(PmDiagnosis diagnosis) {
		String sql="";
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		int recoverWay = 1;
		try {
			sql = "update PM_DIAGNOSIS set diagnosis_Time = str_to_date('"+sdf.format(diagnosis.getDiagnosis_Time())+"','%Y-%m-%d %H:%i:%s'),diagnosis_Result =  '"+diagnosis.getDiagnosis_Result()+"',diagnosis_Code = '"+diagnosis.getDiagnosis_Code()+"' where result_Id = '"+diagnosis.getResult_Id()+"'";
			final String sqlStr = sql;
//			System.out.println(sqlStr);
			jdbcTemplateDia.update(sqlStr);
		} catch (Exception e) {
			System.out.println("editDiagnosis()异常:" + e.toString());
		}		
	}

	/**
	 * 新增诊断实体信息
	 * @param diagnosis
	 */
	public void addDiagnosis(PmDiagnosis diagnosis) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		String sql="";
		sql +=diagnosis.getCity_Name()==null?"'',":"'"+ diagnosis.getCity_Name()+"',";// 
		sql +=diagnosis.getDevice_Id()==null?0+",": diagnosis.getDevice_Id()+",";//  
		sql +=diagnosis.getDevice_Name()==null?"'',":"'"+ diagnosis.getDevice_Name()+"',";// 
		sql +=diagnosis.getDevice_Type()==null?0+",": diagnosis.getDevice_Type()+",";//  
		sql +=diagnosis.getDiagnosis_Code()==null?"'',":"'"+ diagnosis.getDiagnosis_Code()+"',";// 
		sql +=diagnosis.getDiagnosis_Result()==null?"'',":"'"+ diagnosis.getDiagnosis_Result()+"',";// 
		sql +=diagnosis.getDiagnosis_Time()==null?"NULL,": "str_to_date('"+sdf.format(diagnosis.getDiagnosis_Time())+"','%Y-%m-%d %H:%i:%s'),";// 
		sql +=diagnosis.getProvince_Name()==null?"'',":"'"+ diagnosis.getProvince_Name()+"',";// 
		sql +=diagnosis.getReal_Reason()==null?"'',":"'"+ diagnosis.getReal_Reason()+"',";// 
		sql +=diagnosis.getReg_Date()==null?"NULL,": "str_to_date('"+sdf.format(diagnosis.getReg_Date())+"','%Y-%m-%d %H:%i:%s'),";// 
		sql +=diagnosis.getReg_Person()==null?"''":"'"+ diagnosis.getReg_Person();
		
		
		try {
			sql = " INSERT INTO PM_DIAGNOSIS (city_Name, device_Id, device_Name, device_Type, diagnosis_Code, diagnosis_Result, diagnosis_Time, province_Name, real_Reason, reg_Date, reg_Person) VALUES("+sql+") ";
			final String sqlStr = sql;
//			System.out.println(sqlStr);
			jdbcTemplateDia.update(sqlStr);
		} catch (Exception e) {
			System.out.println("addDiagnosis()异常:" + e.toString());
		}	
	}

	/***
	 * 获取告警更新信息
	 *  By ZhangSC
	 * P:此函数的传参warnSourceId为初始化该节点时的warnsourceID,即pm_warn_source表中的warn_id,即设备参数
	 *           code暂时不用来存库
	 *
	 * @return
	 */
	public PmWarnRecord GetDeviceWarnRecordFlag(String code,int DeviceId,int warnSourceId,int parts_type)
	{
		
		List<PmWarnRecord> listPmWarnRecord = new ArrayList<PmWarnRecord>();		
		String sqlString ; 
		sqlString=" SELECT A.WARN_ID,A.WARN_NAME,A.WARN_CONTENTS,B.WARN_SET_ID,C.WARN_SET_NAME,C.WARN_LEVEL,C.WARN_TYPE,D.STATION_NAME, " +
		  		  " D.STATION_ID,D.REAL_WARN_ID,D.WARN_RECORD_ID,E.ASSET_ID,E.ASSET_NAME,E.ASSET_CODE,E.EQUIPMENT_ID    " +
				  " FROM PM_WARN_SOURCE A  " +
				  " LEFT JOIN PM_WARN_RELATION B ON A.WARN_SOURCE_ID = b.WARN_SOURCE_ID  "+
				  " LEFT JOIN PM_WARN_SET C ON C.WARN_SET_ID = B.WARN_SET_ID "+
				  " LEFT JOIN ENERGY_ASSETS E on E.ASSET_MODEL_ID = C.MATERIAL_MODEL_ID " + 
				  " LEFT JOIN PM_WARN_RECORD D ON D.WARN_SET_ID = C.WARN_SET_ID  AND D.WARN_STATUS = 1  " +
				  "AND D.EQUIPMENT_ID = E.EQUIPMENT_ID AND D.REAL_WARN_ID= "+warnSourceId +
				  " WHERE A.WARN_ID >="+StationChannelId.CHANNEL_ID.getValue()+" and A.PARTS_TYPE = "+ parts_type + " and E.EQUIPMENT_ID = "+ DeviceId ;
		if(warnSourceId !=0)
			sqlString += " and A.WARN_ID = "+ warnSourceId;
		System.out.println(sqlString);
		try {
			listPmWarnRecord = jdbcTemplateDia.query(sqlString,
					new RowMapper<PmWarnRecord>() {
						public PmWarnRecord mapRow(ResultSet rs, int rowNum)
							throws SQLException {
							PmWarnRecord warnRecord = new PmWarnRecord();
							warnRecord.setWarn_Record_Id(rs.getInt("WARN_RECORD_ID")); //如果为null则表示添加 如果不为空则不进行操作 
							warnRecord.setEquipment_Id(rs.getInt("EQUIPMENT_ID"));
							warnRecord.setEquipment_Code(rs.getString("ASSET_CODE"));
							warnRecord.setEquipment_Name(rs.getString("ASSET_NAME"));
							warnRecord.setAsset_Id(rs.getInt("ASSET_ID"));
							warnRecord.setReal_Warn_Id(rs.getInt("WARN_ID"));
							warnRecord.setWarn_Name(rs.getString("WARN_NAME"));
							warnRecord.setWarn_Level(rs.getInt("WARN_LEVEL"));
							warnRecord.setWarn_Type(rs.getInt("WARN_TYPE"));
							warnRecord.setWarn_Set_Id(rs.getInt("WARN_SET_ID"));
							return warnRecord;
						}
					});
		} catch (Exception e) {
			System.out.println("GetWarnRecordFlag()异常:" + e.toString());
		}
		if(listPmWarnRecord.size()==0)
			return null;
		else
			return listPmWarnRecord.get(0);	
	}


	/***
	 * 获取告警更新信息
	 * @param code
	 * @param DeviceId
	 * @param warnId
	 * @return
	 */
	public PmWarnRecord GetWarnRecordFlag(String code,int DeviceId,int warnId)
	{

		List<PmWarnRecord> listPmWarnRecord = new ArrayList<PmWarnRecord>();
		String sqlString ;
		sqlString=" SELECT A.WARN_ID,A.WARN_NAME,A.WARN_CONTENTS,B.WARN_SET_ID,C.WARN_SET_NAME,C.WARN_LEVEL,C.WARN_TYPE,D.STATION_NAME, " +
				" D.STATION_ID,D.REAL_WARN_ID,D.WARN_RECORD_ID,E.ASSET_ID,E.ASSET_NAME,E.ASSET_CODE,E.EQUIPMENT_ID    " +
				" FROM PM_WARN_SOURCE A  " +
				" LEFT JOIN PM_WARN_RELATION B ON A.WARN_SOURCE_ID = b.WARN_SOURCE_ID  "+
				" LEFT JOIN PM_WARN_SET C ON C.WARN_SET_ID = B.WARN_SET_ID "+
				" LEFT JOIN ENERGY_ASSETS E on E.ASSET_MODEL_ID = C.MATERIAL_MODEL_ID " +
				" LEFT JOIN PM_WARN_RECORD D ON D.WARN_SET_ID = C.WARN_SET_ID  AND D.WARN_STATUS = 1  " +
				"AND D.EQUIPMENT_ID = "+ DeviceId +" and D.REAL_WARN_ID= "+code+" where 1=1  " +
				" AND A.WARN_ID >="+StationChannelId.CHANNEL_ID.getValue()+" and A.PARTS_TYPE = 6 " ;
		if(warnId !=0)
			sqlString += " and A.WARN_ID = "+ warnId;
		System.out.println(sqlString);
		try {
			listPmWarnRecord = jdbcTemplateDia.query(sqlString,
					new RowMapper<PmWarnRecord>() {
						public PmWarnRecord mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							PmWarnRecord warnRecord = new PmWarnRecord();
							warnRecord.setWarn_Record_Id(rs.getInt("WARN_RECORD_ID")); //如果为null则表示添加 如果不为空则不进行操作
							warnRecord.setEquipment_Id(rs.getInt("EQUIPMENT_ID"));
							warnRecord.setEquipment_Code(rs.getString("ASSET_CODE"));
							warnRecord.setEquipment_Name(rs.getString("ASSET_NAME"));
							warnRecord.setAsset_Id(rs.getInt("ASSET_ID"));
							warnRecord.setReal_Warn_Id(rs.getInt("WARN_ID"));
							warnRecord.setWarn_Name(rs.getString("WARN_NAME"));
							warnRecord.setWarn_Level(rs.getInt("WARN_LEVEL"));
							warnRecord.setWarn_Type(rs.getInt("WARN_TYPE"));
							warnRecord.setWarn_Set_Id(rs.getInt("WARN_SET_ID"));
							return warnRecord;
						}
					});
		} catch (Exception e) {
			System.out.println("GetWarnRecordFlag()异常:" + e.toString());
		}
		if(listPmWarnRecord.size()==0)
			return null;
		else
			return listPmWarnRecord.get(0);
	}
	/***
	 * 获取大于时间参数的天气信息 返回日期以天为单位
	 * @param dateStr 开始时间
	 * @return
	 */
	public List<WeatherInfo> GetWeatherDateList(String dateStr)
	{
		List<WeatherInfo> listWeatherInfo = new ArrayList<WeatherInfo>();		
		String sqlString ; 
		sqlString=" SELECT DATE_FORMAT(A.time,'%Y-%m-%d %H:%i:%s') as time,A.weatherTypeFa,A.weatherTypeFb FROM ( SELECT DATE_FORMAT(TIME, '%Y-%m-%d') as time,city_Code, city_Name, " +
		  		  " humidity, temperature, weatherTypeFa, weatherTypeFb, weatherTypeName, wind FROM PM_WEATHERINFO where 1=1    " +
				  " and time >= DATE_FORMAT('"+dateStr+"','%Y-%m-%d %H:%i:%s') and WEATHERTYPEFA = 0 and WEATHERTYPEFB=0  and CITY_CODE = '101120101'  )  " +
				  " A GROUP BY A.time,A.weatherTypeFa,A.weatherTypeFb order by A.time desc ";
				
//		System.out.println(sqlString);
		try {
			listWeatherInfo = jdbcTemplateDia.query(sqlString,
					new RowMapper<WeatherInfo>() {
						public WeatherInfo mapRow(ResultSet rs, int rowNum)
							throws SQLException {
							WeatherInfo weatherInfo = new WeatherInfo();
							weatherInfo.setTime(rs.getDate("time"));
							weatherInfo.setWeatherTypeFa(rs.getInt("weatherTypeFa"));
							weatherInfo.setWeatherTypeFb(rs.getInt("weatherTypeFb"));
							return weatherInfo;
						}
					});
		} catch (Exception e) {
			System.out.println("GetWarnRecordFlag()异常:" + e.toString());
			return null;
		}
		return listWeatherInfo;
	}
	
	/**
	 * 获取所有电站分组信息
	 * mxf
	 * 2017年10月8日08:46:29
	 * @param parentGroupId
	 * @return
	 */
	public List<PmStationGroup> getSubGroup(int parentGroupId){
	
		List<PmStationGroup> groupList = new ArrayList<PmStationGroup>();
		StringBuilder sb = new StringBuilder();
		sb.append(" select * from PM_PARK_GROUP where 1=1");
		sb.append(" and park_Parent_Group_Id =" + parentGroupId);
		sb.append(" order by replace(replace(replace(replace(replace(replace(replace(replace(replace(PARK_GROUP_NAME,'一','1'),'二','2'),'三','3'),'四','4'),'五','5'),'六','6'),'七','7'),'八','8'),'九','9')");
		
		try {
			groupList = jdbcTemplateDia.query(sb.toString(),
					new RowMapper<PmStationGroup>() {
						public PmStationGroup mapRow(ResultSet rs, int rowNum)
							throws SQLException {
							PmStationGroup pmStationGroup = new PmStationGroup();
							pmStationGroup.setPark_Group_Id(rs.getInt("park_group_id"));
							return pmStationGroup;
						}
					});
		} catch (Exception e) {
			System.err.println("【查询所有电站分组】 获取分组信息异常:" + e.toString());
			return null;
		}
		return groupList;
	}
	
	/**
	 * 根据组id获取组下的电站集合
	 * mxf
	 * 2017年10月8日08:46:55
	 * @param groupId
	 * @return
	 */
	public List<PmStationGroup> getStationListByGroup(Integer groupId) {
		List<PmStationGroup> stationList = new ArrayList<PmStationGroup>();	
		String sql = "select PARK_GROUP_ID,PARK_ID station_Ids,PARK_GROUP_NAME,capacity from PM_PARK_GROUP_ITEM where PARK_GROUP_ID = " + groupId;
		try {
			stationList = jdbcTemplateDia.query(sql,
					new RowMapper<PmStationGroup>() {
						public PmStationGroup mapRow(ResultSet rs, int rowNum)
							throws SQLException {
							PmStationGroup pmStationGroup = new PmStationGroup();
							pmStationGroup.setPark_Group_Id(rs.getInt("park_group_id"));
							pmStationGroup.setStation_Ids(String.valueOf(rs.getInt("station_Ids")));
							pmStationGroup.setPark_Group_Name(rs.getString("PARK_GROUP_NAME"));
							pmStationGroup.setCapacity(rs.getInt("capacity"));
							return pmStationGroup;
						}
					});
		} catch (Exception e) {
			System.err.println("【查询分组下的电站集合】 获取分组电站集合异常:" + e.toString());
			return null;
		}
		return stationList;
	}

}
