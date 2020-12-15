/********************************************************************
 *	版权所有 (C) 2015-2020 积成能源
 *	保留所有版权
 *	
 *	作者：	samson
 *	日期：	2016-11-15
 *	摘要：	资产类
 *
 *********************************************************************/
package cn.zup.iot.timerdecision.dao;

import cn.zup.iot.timerdecision.model.PmWarnRecord;
import cn.zup.iot.timerdecision.model.YXData;
import cn.zup.iot.timerdecision.service.settings.WarnLevel;
import cn.zup.iot.timerdecision.service.settings.WarnSource;
import cn.zup.iot.timerdecision.util.JdbcTemplateUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ScadaWarnPushDao{
  
	private JdbcTemplate jdbcTemplateWarn = JdbcTemplateUtils.jdbcTemplatePms;

//	public JdbcTemplate getJdbcTemplateWarn() {
//		return jdbcTemplateWarn;
//	}
//
//	public void setJdbcTemplateWarn(JdbcTemplate jdbcTemplateWarn) {
//		this.jdbcTemplateWarn = jdbcTemplateWarn;
//	}

	/***
	 * 获取告警更新信息
	 * @param yx
	 * @return
	 */
	public PmWarnRecord GetWarnRecordFlag(YXData yx)
	{

		List<PmWarnRecord> listPmWarnRecord = new ArrayList<PmWarnRecord>();
		String sqlString ;
		sqlString=" SELECT A.WARN_ID,A.WARN_NAME,A.WARN_CONTENTS,B.WARN_SET_ID,C.WARN_SET_NAME,C.WARN_LEVEL,C.WARN_TYPE,D.STATION_NAME, " +
		  		  " D.STATION_ID,D.REAL_WARN_ID,D.WARN_RECORD_ID,E.ASSET_ID,E.ASSET_NAME,E.ASSET_CODE,E.EQUIPMENT_ID    " +
				  " FROM PM_WARN_SOURCE A  " +
				  " LEFT JOIN PM_WARN_RELATION B ON A.WARN_SOURCE_ID = b.WARN_SOURCE_ID  "+
				  " LEFT JOIN PM_WARN_SET C ON C.WARN_SET_ID = B.WARN_SET_ID "+
				  " LEFT JOIN ENERGY_ASSETS E on E.ASSET_MODEL_ID = C.MATERIAL_MODEL_ID " +
				  " LEFT JOIN PM_WARN_RECORD D ON D.WARN_SET_ID = C.WARN_SET_ID  AND D.WARN_STATUS = 1  AND D.EQUIPMENT_ID = E.EQUIPMENT_ID where 1=1  " ;

		if(yx.getBuJianCanShu() !=0)
			sqlString += " and A.WARN_ID = "+ yx.getBuJianCanShu();
		if(yx.getBuJianLeiXing() !=0)
			sqlString += " and A.PARTS_TYPE = "+ yx.getBuJianLeiXing();
		if(yx.getBuJianId() !=0)
			sqlString += " and E.EQUIPMENT_ID = "+ yx.getBuJianId();

		//System.out.println(sqlString);
		try {
			listPmWarnRecord = jdbcTemplateWarn.query(sqlString,
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
	 * 插入警告信息
	 * @param pmWarnRecord
	 * @return
	 */
	public boolean InsertWarnRecordtemp(PmWarnRecord pmWarnRecord)
	{
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sql="";
		sql =" SEQ_PM_WARN_RECORD.nextval,";
		sql +=pmWarnRecord.getEquipment_Id()==null?0+",": pmWarnRecord.getEquipment_Id()+",";//设备ID
		sql +=pmWarnRecord.getAsset_Id()==null?0+",": pmWarnRecord.getAsset_Id()+",";// 资产ID
		sql +=pmWarnRecord.getReal_Warn_Id()==null?0+",": pmWarnRecord.getReal_Warn_Id()+",";//
		sql +=pmWarnRecord.getWarn_Name()==null?"'',": "'"+pmWarnRecord.getWarn_Name()+"',";//
		sql +=pmWarnRecord.getWarn_Set_Id()==null?0+",": pmWarnRecord.getWarn_Set_Id()+",";//
		sql +=pmWarnRecord.getWarn_Level()==null?0+",": pmWarnRecord.getWarn_Level()+",";//
		sql +=pmWarnRecord.getWarn_Type()==null?0+",": pmWarnRecord.getWarn_Type()+",";//
		sql +=pmWarnRecord.getStation_Id()==null?0+",": pmWarnRecord.getStation_Id()+",";//
		sql +=pmWarnRecord.getOccur_Time()==null?"to_date('2016-10-24 01:01:01','yyyy-mm-dd hh24:mi:ss'),": "to_date('"+sdf.format(pmWarnRecord.getOccur_Time())+"','yyyy-mm-dd hh24:mi:ss'),";//
		sql +=pmWarnRecord.getOccur_Recover()==null?"NULL,": "to_date('"+sdf.format(pmWarnRecord.getOccur_Recover())+"','yyyy-mm-dd hh24:mi:ss'),";//
		sql +=pmWarnRecord.getWarn_Status()==null?0: pmWarnRecord.getWarn_Status()+",";//
		sql +=pmWarnRecord.getStation_Name()==null?"'',": "'"+pmWarnRecord.getStation_Name()+"',";// 电站名称
		sql +=pmWarnRecord.getEquipment_Name()==null?"'',": "'"+pmWarnRecord.getEquipment_Name()+"',";// 设备名称
		sql +=pmWarnRecord.getWarn_Source()==null?0+",": pmWarnRecord.getWarn_Source()+",";// 告警来源
		sql +=pmWarnRecord.getProvinceName()==null?"'',": "'"+pmWarnRecord.getProvinceName()+"',";// 省名称
		sql +=pmWarnRecord.getCityName()==null?"'',": "'"+pmWarnRecord.getCityName()+"',";// 城市名称
		sql +=pmWarnRecord.getEquipment_Code()==null?"'',": "'"+pmWarnRecord.getEquipment_Code()+"',";// 设备编号
		sql +=pmWarnRecord.getSend_State()==null?0: pmWarnRecord.getSend_State();// 发送状态

		final String sqlStr = " INSERT INTO PM_WARN_RECORD VALUES("+sql+") ";
		KeyHolder key=new GeneratedKeyHolder();
		this.jdbcTemplateWarn.update(new PreparedStatementCreator(){
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {
				PreparedStatement preState=con.prepareStatement(sqlStr);
				return preState;
			}
		},key);
		pmWarnRecord.setWarn_Record_Id(key.getKey().intValue());


		return true;
	}
	/**
	 *
	 * @return
	 */

	public List<PmWarnRecord> GetWarnRecordList()
	{
		List<PmWarnRecord> listPmWarnRecordList = new ArrayList<PmWarnRecord>();
    	StringBuffer sb= new StringBuffer();
    	sb.append(" select warn_record_id, equipment_id, asset_id, real_Warn_Id,warn_name, warn_set_id, warn_level, warn_type, station_id,occur_time,occur_recover, " +
				"		warn_status, station_name,equipment_name, warn_source, provincename,cityname, equipment_code, send_state, voltage, circuit " +
				"	from pm_warn_record " +
				"	where warn_status = 1 and (recover_way is null or recover_way = 0) ");
    	sb.append(" and warn_level < "+WarnLevel.transferClose.getValue()); //自动告警只处理告警级别为一般告警 电站告警 外网告警
    	sb.append(" and WARN_SOURCE = "+WarnSource.scada.getValue()); //自动告警处理 来源为scada的数据
//		System.out.println(sqlString);
		try {
			listPmWarnRecordList = jdbcTemplateWarn.query(sb.toString(),
					new RowMapper<PmWarnRecord>() {
						public PmWarnRecord mapRow(ResultSet rs, int rowNum)
							throws SQLException {
							PmWarnRecord warnRecord = new PmWarnRecord();
							warnRecord.setWarn_Record_Id(rs.getInt("WARN_RECORD_ID"));
							warnRecord.setEquipment_Id(rs.getInt("EQUIPMENT_ID"));
							warnRecord.setAsset_Id(rs.getInt("ASSET_ID"));
							warnRecord.setReal_Warn_Id(rs.getInt("REAL_WARN_ID"));
							warnRecord.setWarn_Name(rs.getString("WARN_NAME"));
							warnRecord.setWarn_Set_Id(rs.getInt("WARN_SET_ID"));
							warnRecord.setWarn_Level(rs.getInt("WARN_LEVEL"));
							warnRecord.setWarn_Type(rs.getInt("WARN_TYPE"));
							warnRecord.setStation_Id(rs.getInt("STATION_ID"));
							warnRecord.setOccur_Time(rs.getDate("OCCUR_TIME"));
							warnRecord.setOccur_Recover(rs.getDate("OCCUR_RECOVER"));
							warnRecord.setWarn_Status(rs.getInt("WARN_STATUS"));
							warnRecord.setStation_Name(rs.getString("STATION_NAME"));
							warnRecord.setEquipment_Name(rs.getString("EQUIPMENT_NAME"));
							warnRecord.setWarn_Source(rs.getInt("WARN_SOURCE"));
							warnRecord.setProvinceName(rs.getString("PROVINCENAME"));
							warnRecord.setCityName(rs.getString("CITYNAME"));
							warnRecord.setEquipment_Code(rs.getString("EQUIPMENT_CODE"));
							warnRecord.setSend_State(rs.getInt("SEND_STATE"));
							warnRecord.setVoltage(rs.getFloat("VOLTAGE"));
							warnRecord.setCircuit(rs.getFloat("CIRCUIT"));
							return warnRecord;
						}
					});
		} catch (Exception e) {
			System.out.println("GetWarnRecordFlag()异常:" + e.toString());
		}
			return listPmWarnRecordList;
	}
	/**
	 * @Author ZhangSC
	 * 根据bujianId 查询物资信息
	 * @param
	 * @return
	 */

	public List<PmWarnRecord> GetEnergyAssetsList()
	{
		List<PmWarnRecord> listPmWarnRecordList = new ArrayList<PmWarnRecord>();
		StringBuffer sb= new StringBuffer();
		sb.append(" select warn_record_id, equipment_id, asset_id, real_Warn_Id,warn_name, warn_set_id, warn_level, warn_type, station_id,occur_time,occur_recover, " +
				"		warn_status, station_name,equipment_name, warn_source, provincename,cityname, equipment_code, send_state, voltage, circuit " +
				"	from pm_warn_record " +
				"	where warn_status = 1 and (recover_way is null or recover_way = 0) ");
		sb.append(" and warn_level < "+WarnLevel.transferClose.getValue()); //自动告警只处理告警级别为一般告警 电站告警 外网告警
		sb.append(" and WARN_SOURCE = "+WarnSource.scada.getValue()); //自动告警处理 来源为scada的数据
		//		//		System.out.println(sqlString);
		try {
			listPmWarnRecordList = jdbcTemplateWarn.query(sb.toString(),
					new RowMapper<PmWarnRecord>() {
						public PmWarnRecord mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							PmWarnRecord warnRecord = new PmWarnRecord();
							warnRecord.setWarn_Record_Id(rs.getInt("WARN_RECORD_ID"));
							warnRecord.setEquipment_Id(rs.getInt("EQUIPMENT_ID"));
							warnRecord.setAsset_Id(rs.getInt("ASSET_ID"));
							warnRecord.setReal_Warn_Id(rs.getInt("REAL_WARN_ID"));
							warnRecord.setWarn_Name(rs.getString("WARN_NAME"));
							warnRecord.setWarn_Set_Id(rs.getInt("WARN_SET_ID"));
							warnRecord.setWarn_Level(rs.getInt("WARN_LEVEL"));
							warnRecord.setWarn_Type(rs.getInt("WARN_TYPE"));
							warnRecord.setStation_Id(rs.getInt("STATION_ID"));
							warnRecord.setOccur_Time(rs.getDate("OCCUR_TIME"));
							warnRecord.setOccur_Recover(rs.getDate("OCCUR_RECOVER"));
							warnRecord.setWarn_Status(rs.getInt("WARN_STATUS"));
							warnRecord.setStation_Name(rs.getString("STATION_NAME"));
							warnRecord.setEquipment_Name(rs.getString("EQUIPMENT_NAME"));
							warnRecord.setWarn_Source(rs.getInt("WARN_SOURCE"));
							warnRecord.setProvinceName(rs.getString("PROVINCENAME"));
							warnRecord.setCityName(rs.getString("CITYNAME"));
							warnRecord.setEquipment_Code(rs.getString("EQUIPMENT_CODE"));
							warnRecord.setSend_State(rs.getInt("SEND_STATE"));
							warnRecord.setVoltage(rs.getFloat("VOLTAGE"));
							warnRecord.setCircuit(rs.getFloat("CIRCUIT"));
							return warnRecord;
						}
					});
		} catch (Exception e) {
			System.out.println("GetWarnRecordFlag()异常:" + e.toString());
		}
		return listPmWarnRecordList;
	}

	/**
	 * 诊断结果转告警信息获取
	 * @author Lzq
	 * @return
	 * 2019年7月29日 上午10:53:54
	 */
	public List<PmWarnRecord> GetWarntrueflagList(int stationId,int equipmentId)
	{
		List<PmWarnRecord> listPmWarnRecordList = new ArrayList<PmWarnRecord>();
    	StringBuffer sb= new StringBuffer();
    	sb.append(" select warn_record_id, equipment_id, asset_id, real_Warn_Id,warn_name, warn_set_id, warn_level, warn_type, station_id,occur_time,occur_recover, " +
				"		warn_status, station_name,equipment_name, warn_source, provincename,cityname, equipment_code, send_state, voltage, circuit " +
				"	from pm_warn_record " +
				"	where warn_status = 1 and (recover_way is null or recover_way = 0) ");//告警未恢复标记
    	//sb.append(" and warn_level = 5 and warn_type = 2 "); //运维提示,手动告警
    	sb.append(" and warn_type = 2 "); //运维提示,手动告警
    	sb.append(" and equipment_id = "+equipmentId); //厂站Id
    	/**
		 * warntrueflag为告警flag
		 * 根据电站ID、厂站类型和手动推送告警类型去获取
		 * pm_warn_record和warnrecord表中WARN_TYPE=2为手动告警，1为自动告警
		 * warn_level=5运维提示
		 * equipment_id厂站Id
		 * strResultCode=6为决策6分组电量对比
		 */

		System.out.println(sb.toString());
		try {
			listPmWarnRecordList = jdbcTemplateWarn.query(sb.toString(),
					new RowMapper<PmWarnRecord>() {
						public PmWarnRecord mapRow(ResultSet rs, int rowNum)
							throws SQLException {
							PmWarnRecord warnRecord = new PmWarnRecord();
							warnRecord.setWarn_Record_Id(rs.getInt("WARN_RECORD_ID"));
							warnRecord.setEquipment_Id(rs.getInt("EQUIPMENT_ID"));
							warnRecord.setAsset_Id(rs.getInt("ASSET_ID"));
							warnRecord.setReal_Warn_Id(rs.getInt("REAL_WARN_ID"));
							warnRecord.setWarn_Name(rs.getString("WARN_NAME"));
							warnRecord.setWarn_Set_Id(rs.getInt("WARN_SET_ID"));
							warnRecord.setWarn_Level(rs.getInt("WARN_LEVEL"));
							warnRecord.setWarn_Type(rs.getInt("WARN_TYPE"));
							warnRecord.setStation_Id(rs.getInt("STATION_ID"));
							warnRecord.setOccur_Time(rs.getDate("OCCUR_TIME"));
							warnRecord.setOccur_Recover(rs.getDate("OCCUR_RECOVER"));
							warnRecord.setWarn_Status(rs.getInt("WARN_STATUS"));
							warnRecord.setStation_Name(rs.getString("STATION_NAME"));
							warnRecord.setEquipment_Name(rs.getString("EQUIPMENT_NAME"));
							warnRecord.setWarn_Source(rs.getInt("WARN_SOURCE"));
							warnRecord.setProvinceName(rs.getString("PROVINCENAME"));
							warnRecord.setCityName(rs.getString("CITYNAME"));
							warnRecord.setEquipment_Code(rs.getString("EQUIPMENT_CODE"));
							warnRecord.setSend_State(rs.getInt("SEND_STATE"));
							warnRecord.setVoltage(rs.getFloat("VOLTAGE"));
							warnRecord.setCircuit(rs.getFloat("CIRCUIT"));
							return warnRecord;
						}
					});
		} catch (Exception e) {
			System.out.println("GetWarnRecordFlag()异常:" + e.toString());
		}
			return listPmWarnRecordList;
	}

	public List<PmWarnRecord> GetWarnOperateRecordList(String Code)
	{
		List<PmWarnRecord> listPmWarnRecordList = new ArrayList<PmWarnRecord>();
		String sqlString ;
		sqlString=" select warn_record_id, equipment_id, asset_id, real_Warn_Id,warn_name, warn_set_id, warn_level, warn_type, station_id,occur_time,occur_recover, " +
				"		warn_status, station_name,equipment_name, warn_source, provincename,cityname, equipment_code, send_state, voltage, circuit from pm_warn_record " +
				"	where warn_status = 1 and warn_level = "+WarnLevel.stationOperation.getValue()+" and (recover_way is null or recover_way = 0)";

		if(!Code.equals(""))
			sqlString += " and REAL_WARN_ID in("+Code+") ";
//		System.out.println(sqlString);
		try {
			listPmWarnRecordList = jdbcTemplateWarn.query(sqlString,
					new RowMapper<PmWarnRecord>() {
						public PmWarnRecord mapRow(ResultSet rs, int rowNum)
							throws SQLException {
							PmWarnRecord warnRecord = new PmWarnRecord();
							warnRecord.setWarn_Record_Id(rs.getInt("WARN_RECORD_ID"));
							warnRecord.setEquipment_Id(rs.getInt("EQUIPMENT_ID"));
							warnRecord.setAsset_Id(rs.getInt("ASSET_ID"));
							warnRecord.setReal_Warn_Id(rs.getInt("REAL_WARN_ID"));
							warnRecord.setWarn_Name(rs.getString("WARN_NAME"));
							warnRecord.setWarn_Set_Id(rs.getInt("WARN_SET_ID"));
							warnRecord.setWarn_Level(rs.getInt("WARN_LEVEL"));
							warnRecord.setWarn_Type(rs.getInt("WARN_TYPE"));
							warnRecord.setStation_Id(rs.getInt("STATION_ID"));
							warnRecord.setOccur_Time(rs.getDate("OCCUR_TIME"));
							warnRecord.setOccur_Recover(rs.getDate("OCCUR_RECOVER"));
							warnRecord.setWarn_Status(rs.getInt("WARN_STATUS"));
							warnRecord.setStation_Name(rs.getString("STATION_NAME"));
							warnRecord.setEquipment_Name(rs.getString("EQUIPMENT_NAME"));
							warnRecord.setWarn_Source(rs.getInt("WARN_SOURCE"));
							warnRecord.setProvinceName(rs.getString("PROVINCENAME"));
							warnRecord.setCityName(rs.getString("CITYNAME"));
							warnRecord.setEquipment_Code(rs.getString("EQUIPMENT_CODE"));
							warnRecord.setSend_State(rs.getInt("SEND_STATE"));
							warnRecord.setVoltage(rs.getFloat("VOLTAGE"));
							warnRecord.setCircuit(rs.getFloat("CIRCUIT"));
							return warnRecord;
						}
					});
		} catch (Exception e) {
			System.out.println("GetWarnRecordFlag()异常:" + e.toString());
		}
			return listPmWarnRecordList;
	}
	/**
	 *
	 * @return
	 */

	public List<PmWarnRecord> GetStatusWarnRecordList()
	{
		List<PmWarnRecord> listPmWarnRecordList = new ArrayList<PmWarnRecord>();
    	StringBuffer sb= new StringBuffer();
    	sb.append(" select warn_record_id, equipment_id, asset_id, real_Warn_Id,warn_name, warn_set_id, warn_level, warn_type, station_id,occur_time,occur_recover, " +
				"		warn_status, station_name,equipment_name, warn_source, provincename,cityname, equipment_code, send_state, voltage, circuit " +
				"	from pm_warn_record " +
				"	where warn_status = 1 and (recover_way is null or recover_way = 0) ");
    	sb.append(" and warn_level = "+WarnLevel.transferClose.getValue()); //自动告警只处理告警级别为一般告警 电站告警 外网告警
    	sb.append(" and WARN_SOURCE = "+WarnSource.handOperate.getValue()); //自动告警处理 来源为scada的数据
		System.out.println(sb);
		try {
			listPmWarnRecordList = jdbcTemplateWarn.query(sb.toString(),
					new RowMapper<PmWarnRecord>() {
						public PmWarnRecord mapRow(ResultSet rs, int rowNum)
							throws SQLException {
							PmWarnRecord warnRecord = new PmWarnRecord();
							warnRecord.setWarn_Record_Id(rs.getInt("WARN_RECORD_ID"));
							warnRecord.setEquipment_Id(rs.getInt("EQUIPMENT_ID"));
							warnRecord.setAsset_Id(rs.getInt("ASSET_ID"));
							warnRecord.setReal_Warn_Id(rs.getInt("REAL_WARN_ID"));
							warnRecord.setWarn_Name(rs.getString("WARN_NAME"));
							warnRecord.setWarn_Set_Id(rs.getInt("WARN_SET_ID"));
							warnRecord.setWarn_Level(rs.getInt("WARN_LEVEL"));
							warnRecord.setWarn_Type(rs.getInt("WARN_TYPE"));
							warnRecord.setStation_Id(rs.getInt("STATION_ID"));
							warnRecord.setOccur_Time(rs.getDate("OCCUR_TIME"));
							warnRecord.setOccur_Recover(rs.getDate("OCCUR_RECOVER"));
							warnRecord.setWarn_Status(rs.getInt("WARN_STATUS"));
							warnRecord.setStation_Name(rs.getString("STATION_NAME"));
							warnRecord.setEquipment_Name(rs.getString("EQUIPMENT_NAME"));
							warnRecord.setWarn_Source(rs.getInt("WARN_SOURCE"));
							warnRecord.setProvinceName(rs.getString("PROVINCENAME"));
							warnRecord.setCityName(rs.getString("CITYNAME"));
							warnRecord.setEquipment_Code(rs.getString("EQUIPMENT_CODE"));
							warnRecord.setSend_State(rs.getInt("SEND_STATE"));
							warnRecord.setVoltage(rs.getFloat("VOLTAGE"));
							warnRecord.setCircuit(rs.getFloat("CIRCUIT"));
							return warnRecord;
						}
					});
		} catch (Exception e) {
			System.out.println("GetWarnRecordFlag()异常:" + e.toString());
		}
			return listPmWarnRecordList;
	}
	/**
	 *
	 * @param warn_level 告警等级；4为通讯关闭
	 * @param warn_source 告警来源 (3为诊断树来源，1为scada来源，2为人工告警)
	 * @param warn_type 告警类型 1为自动告警，2为手动告警
	 * @return
	 */

	public List<PmWarnRecord> GetDeviceWarnRecordList(int equipment_id, int warn_level, int warn_source, int warn_type)
	{
		List<PmWarnRecord> listPmWarnRecordList = new ArrayList<PmWarnRecord>();
		StringBuffer sb= new StringBuffer();
		sb.append(" select warn_record_id, equipment_id, asset_id, real_Warn_Id,warn_name, warn_set_id, warn_level, warn_type, station_id,occur_time,occur_recover, " +
				"		warn_status, station_name,equipment_name, warn_source, provincename,cityname, equipment_code, send_state, voltage, circuit " +
				"	from pm_warn_record " +
				"	where warn_status = 1 and (recover_way is null or recover_way = 0) ");
		if(equipment_id!=0){
			sb.append(" and equipment_id = "+equipment_id);
		}
		if(warn_level!=0)
			sb.append(" and warn_level = "+warn_level);
		if(warn_source!=0)
			sb.append(" and warn_source = "+warn_source);
		if(warn_type!=0)
			sb.append(" and warn_type = "+warn_type);
		System.out.println(sb);
		try {
			listPmWarnRecordList = jdbcTemplateWarn.query(sb.toString(),
					new RowMapper<PmWarnRecord>() {
						public PmWarnRecord mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							PmWarnRecord warnRecord = new PmWarnRecord();
							warnRecord.setWarn_Record_Id(rs.getInt("WARN_RECORD_ID"));
							warnRecord.setEquipment_Id(rs.getInt("EQUIPMENT_ID"));
							warnRecord.setAsset_Id(rs.getInt("ASSET_ID"));
							warnRecord.setReal_Warn_Id(rs.getInt("REAL_WARN_ID"));
							warnRecord.setWarn_Name(rs.getString("WARN_NAME"));
							warnRecord.setWarn_Set_Id(rs.getInt("WARN_SET_ID"));
							warnRecord.setWarn_Level(rs.getInt("WARN_LEVEL"));
							warnRecord.setWarn_Type(rs.getInt("WARN_TYPE"));
							warnRecord.setStation_Id(rs.getInt("STATION_ID"));
							warnRecord.setOccur_Time(rs.getDate("OCCUR_TIME"));
							warnRecord.setOccur_Recover(rs.getDate("OCCUR_RECOVER"));
							warnRecord.setWarn_Status(rs.getInt("WARN_STATUS"));
							warnRecord.setStation_Name(rs.getString("STATION_NAME"));
							warnRecord.setEquipment_Name(rs.getString("EQUIPMENT_NAME"));
							warnRecord.setWarn_Source(rs.getInt("WARN_SOURCE"));
							warnRecord.setProvinceName(rs.getString("PROVINCENAME"));
							warnRecord.setCityName(rs.getString("CITYNAME"));
							warnRecord.setEquipment_Code(rs.getString("EQUIPMENT_CODE"));
							warnRecord.setSend_State(rs.getInt("SEND_STATE"));
							warnRecord.setVoltage(rs.getFloat("VOLTAGE"));
							warnRecord.setCircuit(rs.getFloat("CIRCUIT"));
							return warnRecord;
						}
					});
		} catch (Exception e) {
			System.out.println("GetWarnRecordFlag()异常:" + e.toString());
		}
		return listPmWarnRecordList;
	}


	/**
	 * @return
	 */

	public List<PmWarnRecord> GetDeviceStatusWarnRecordList()
	{
		List<PmWarnRecord> listPmWarnRecordList = new ArrayList<PmWarnRecord>();
		StringBuffer sb= new StringBuffer();
		sb.append(" select warn_record_id, equipment_id, asset_id, real_Warn_Id,warn_name, warn_set_id, warn_level, warn_type, station_id,occur_time,occur_recover, " +
				"		warn_status, station_name,equipment_name, warn_source, provincename,cityname, equipment_code, send_state, voltage, circuit " +
				"	from pm_warn_record " +
				"	where warn_status = 1 and (recover_way is null or recover_way = 0) ");
		sb.append(" and warn_level = "+WarnLevel.transferClose.getValue()); //自动告警只处理告警级别为一般告警 电站告警 外网告警
		sb.append(" and WARN_SOURCE = "+1); //来源为决策树改为scada告警。
		System.out.println(sb);
		try {
			listPmWarnRecordList = jdbcTemplateWarn.query(sb.toString(),
					new RowMapper<PmWarnRecord>() {
						public PmWarnRecord mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							PmWarnRecord warnRecord = new PmWarnRecord();
							warnRecord.setWarn_Record_Id(rs.getInt("WARN_RECORD_ID"));
							warnRecord.setEquipment_Id(rs.getInt("EQUIPMENT_ID"));
							warnRecord.setAsset_Id(rs.getInt("ASSET_ID"));
							warnRecord.setReal_Warn_Id(rs.getInt("REAL_WARN_ID"));
							warnRecord.setWarn_Name(rs.getString("WARN_NAME"));
							warnRecord.setWarn_Set_Id(rs.getInt("WARN_SET_ID"));
							warnRecord.setWarn_Level(rs.getInt("WARN_LEVEL"));
							warnRecord.setWarn_Type(rs.getInt("WARN_TYPE"));
							warnRecord.setStation_Id(rs.getInt("STATION_ID"));
							warnRecord.setOccur_Time(rs.getTimestamp("OCCUR_TIME"));
							warnRecord.setOccur_Recover(rs.getTimestamp("OCCUR_RECOVER"));
							warnRecord.setWarn_Status(rs.getInt("WARN_STATUS"));
							warnRecord.setStation_Name(rs.getString("STATION_NAME"));
							warnRecord.setEquipment_Name(rs.getString("EQUIPMENT_NAME"));
							warnRecord.setWarn_Source(rs.getInt("WARN_SOURCE"));
							warnRecord.setProvinceName(rs.getString("PROVINCENAME"));
							warnRecord.setCityName(rs.getString("CITYNAME"));
							warnRecord.setEquipment_Code(rs.getString("EQUIPMENT_CODE"));
							warnRecord.setSend_State(rs.getInt("SEND_STATE"));
							warnRecord.setVoltage(rs.getFloat("VOLTAGE"));
							warnRecord.setCircuit(rs.getFloat("CIRCUIT"));
							return warnRecord;
						}
					});
		} catch (Exception e) {
			System.out.println("GetWarnRecordFlag()异常:" + e.toString());
		}
		return listPmWarnRecordList;
	}
	/***
	 * 插入警告信息
	 * @param pmWarnRecord
	 * @return
	 */

	public PmWarnRecord InsertWarnRecord(PmWarnRecord pmWarnRecord)
	{
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sql="";
		try {
			sql ="INSERT INTO PM_WARN_RECORD (EQUIPMENT_ID,ASSET_ID,REAL_WARN_ID,WARN_NAME,WARN_SET_ID,WARN_LEVEL" +
					",WARN_TYPE,STATION_ID,OCCUR_TIME,OCCUR_RECOVER,WARN_STATUS,STATION_NAME,EQUIPMENT_NAME,WARN_SOURCE,PROVINCENAME,CITYNAME" +
					",EQUIPMENT_CODE,SEND_STATE,VOLTAGE,CIRCUIT,RECOVER_WAY) VALUES(";
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
			sql +=pmWarnRecord.getRecover_Way()==null?0:0;//恢复方式
			sql +=")";
			final String sqlStr = sql;
//			System.out.println(sqlStr);
		 	jdbcTemplateWarn.update(sqlStr);

		 	Integer Id =0;
	    	StringBuffer sb= new StringBuffer();
	    	sb.append(" select max(WARN_RECORD_ID) as Id  from PM_WARN_RECORD  ");
		    //System.out.println(sb.toString());
		    Id = jdbcTemplateWarn.query(sb.toString(), new ResultSetExtractor<Integer>() {
		    public Integer extractData(ResultSet rs)  throws SQLException, DataAccessException {
		    	Integer result =0;
				 while(rs.next()) {
					 result = rs.getInt("Id");
				 }
				 return result;
		     }});
		    pmWarnRecord.setWarn_Record_Id(Id);
		} catch (Exception e) {
			System.out.println("InsertWarnRecord()异常:" + e.toString());
		}
		return pmWarnRecord;
	}


	/***
	 * 更新数据库告警结束时间
	 * @param warnRecordId
	 * @param nowDate
	 * @return
	 */
	public void updateWarnRecord(Integer warnRecordId , String nowDate)
	{
		String sql="";
		int recoverWay = 1;
		try {
			sql = "update pm_warn_record set occur_recover = str_to_date('"+nowDate+"','%Y-%m-%d %H:%i:%s'), warn_status = 5, recover_Way = '"+recoverWay+"', SEND_STATE = 2 where warn_record_id = '"+warnRecordId+"'";
			final String sqlStr = sql;
//			System.out.println(sqlStr);
		 	jdbcTemplateWarn.update(sqlStr);
		} catch (Exception e) {
			System.out.println("InsertWarnRecord()异常:" + e.toString());
		}
	}


	/***
	 * 插入警告信息
	 * @param pmWarnRecord
	 * @return
	 */

	public PmWarnRecord InsertAnalyzeEvent(PmWarnRecord pmWarnRecord)
	{
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String sql="";
		try {
			sql ="INSERT INTO pm_analyze_event (WARN_RECORD_ID,EVENT_STATE,EQUIPMENT_ID,ASSET_ID,REAL_WARN_ID,WARN_NAME,WARN_SET_ID,WARN_LEVEL" +
					",WARN_TYPE,STATION_ID,OCCUR_TIME,OCCUR_RECOVER,WARN_STATUS,STATION_NAME,EQUIPMENT_NAME,WARN_SOURCE,PROVINCENAME,CITYNAME" +
					",EQUIPMENT_CODE,SEND_STATE,VOLTAGE,CIRCUIT,RECOVER_WAY) VALUES(NULL,1,";
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
			sql +=pmWarnRecord.getRecover_Way()==null?0:0;//恢复方式
			sql +=")";
			final String sqlStr = sql;
			System.out.println(sqlStr);
		 	jdbcTemplateWarn.update(sqlStr);
		 	
		} catch (Exception e) {
			System.out.println("InsertWarnRecord()异常:" + e.toString());
		}
		return pmWarnRecord;
	}
}
