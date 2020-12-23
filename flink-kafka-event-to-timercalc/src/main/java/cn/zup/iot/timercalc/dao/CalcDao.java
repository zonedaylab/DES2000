package cn.zup.iot.timercalc.dao;

import cn.zup.iot.timercalc.model.CalcConfig;
import cn.zup.iot.timercalc.model.CalcParamConfig;
import cn.zup.iot.timercalc.model.ChangeTable;
import cn.zup.iot.timercalc.util.JdbcTemplateUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/***
 * 
 * @author samson
 * @date 2019-03-21 15
 * @des 计算数据访问类
 *
 */
public class CalcDao {

	private JdbcTemplate jdbcTemplate_ccmservice = JdbcTemplateUtils.jdbcTemplatePms;

	/**
	 * @author samson
	 * @date 2019-03-21 15
	 * @des 获取所有的计算信息集合
	 * @param calcConfig 查询参数实体
	 * @return 
	 */
	public List<CalcConfig> getAllCalcInfo(CalcConfig calcConfig)
	{
		List<CalcConfig> calcConfigList = new ArrayList<CalcConfig>();		
    	StringBuffer sb= new StringBuffer();
    	sb.append(" select calcID,calcName,formula,deviceType,deviceID,deviceParam,changZhanID,calcInterval,targetTable,expression,startType,maxPositiveValue,minPositiveValue,maxNegativeValue,minNegativeValue,calcIntervalTime,weekflag "
				+ " from  calcConfig where 1=1 ") ;

		if(calcConfig!=null)
		{
			if(calcConfig.getStartType()!=null)
				sb.append(" and startType ="+calcConfig.getStartType() + " ");
		}
		//System.out.println(sb.toString());
		try {
			calcConfigList = jdbcTemplate_ccmservice.query(sb.toString(),
					new RowMapper<CalcConfig>() {
						public CalcConfig mapRow(ResultSet rs, int rowNum)
							throws SQLException {
							CalcConfig calcConfig = new CalcConfig();
							calcConfig.setCalcID(rs.getInt("calcID"));
							calcConfig.setCalcName(rs.getString("calcName"));
							calcConfig.setFormula(rs.getInt("formula"));
							calcConfig.setDeviceType(rs.getInt("deviceType"));
							calcConfig.setDeviceID(rs.getInt("deviceID"));
							calcConfig.setDeviceParam(rs.getInt("deviceParam"));
							calcConfig.setChangZhanID(rs.getInt("changZhanID"));
							calcConfig.setCalcInterval(rs.getInt("calcInterval"));
							calcConfig.setTargetTable(rs.getInt("targetTable"));
							calcConfig.setExpression(rs.getString("expression"));
							calcConfig.setStartType(rs.getInt("startType"));
							calcConfig.setMaxPositiveValue(rs.getDouble("maxPositiveValue"));
							calcConfig.setMinPositiveValue(rs.getDouble("minPositiveValue"));
							calcConfig.setMaxNegativeValue(rs.getDouble("maxNegativeValue"));
							calcConfig.setMinNegativeValue(rs.getDouble("minNegativeValue"));
							calcConfig.setCalcIntervalTime(rs.getInt("calcIntervalTime"));
							calcConfig.setWeekFlag(rs.getInt("weekflag"));
							return calcConfig;
						}
					});
		} catch (Exception e) {
			System.out.println("getAllCalcInfo()异常:" + e.toString());
			return null;
		}
		return calcConfigList;
	}
	
	/**
	 * @author samson
	 * @date 2019-03-21 15
	 * @des 获取所有的计算参数信息集合
	 * @param calcParamConfig 查询参数实体
	 * @return
	 */
	public List<CalcParamConfig> getAllCalcParamsInfo(CalcParamConfig calcParamConfig)
	{
		List<CalcParamConfig> calcParamConfigList = new ArrayList<CalcParamConfig>();
    	StringBuffer sb= new StringBuffer();
    	sb.append(" select calcID,calcID,paramOrder,dataType,deviceType,deviceID,deviceParam,paramStatisType,fCoef,fOffset "
				+ " from  calcparamconfig where 1=1 ") ;

		if(calcParamConfig!=null)
		{
			if(calcParamConfig.getCalcID()!=null)
				sb.append(" and calcID ="+calcParamConfig.getCalcID() + " ");
			if(calcParamConfig.getDeviceType()!=null && calcParamConfig.getDeviceType()!=0)
				sb.append(" and deviceType ="+calcParamConfig.getDeviceType() + " ");
			if(calcParamConfig.getDeviceID()!=null && calcParamConfig.getDeviceID()!=0)
				sb.append(" and deviceID ="+calcParamConfig.getDeviceID() + " ");
			if(calcParamConfig.getDeviceParam()!=null && calcParamConfig.getDeviceParam()!=0)
				sb.append(" and deviceParam ="+calcParamConfig.getDeviceParam() + " ");
		}
		//System.out.println(sb.toString());
		try {
			calcParamConfigList = jdbcTemplate_ccmservice.query(sb.toString(),
					new RowMapper<CalcParamConfig>() {
						public CalcParamConfig mapRow(ResultSet rs, int rowNum)
							throws SQLException {
							CalcParamConfig calcParamConfig = new CalcParamConfig();
							calcParamConfig.setCalcID(rs.getInt("calcID"));
							calcParamConfig.setParamOrder(rs.getInt("paramOrder"));
							calcParamConfig.setDataType(rs.getInt("dataType"));
							calcParamConfig.setDeviceType(rs.getInt("deviceType"));
							calcParamConfig.setDeviceID(rs.getInt("deviceID"));
							calcParamConfig.setDeviceParam(rs.getInt("deviceParam"));
							calcParamConfig.setParamStatisType(rs.getInt("paramStatisType"));
							calcParamConfig.setfCoef(rs.getDouble("fCoef"));
							calcParamConfig.setfOffset(rs.getDouble("fOffset"));
							return calcParamConfig;
						}
					});
		} catch (Exception e) {
			System.out.println("getAllCalcParamsInfo()异常:" + e.toString());
			return null;
		}
		return calcParamConfigList;
	}

	public List<ChangeTable> getAllChangeTableInfo(ChangeTable changeTable)
	{
		List<ChangeTable> changeTableList = new ArrayList<ChangeTable>();		
    	StringBuffer sb= new StringBuffer();
    	sb.append(" select changeId,changName,deviceType,deviceID,deviceParam,oldValue,NewValue,calcCoef,memo,changeState "
    			+ "from changetable where 1=1 ") ;

		if(changeTable!=null)
		{
			if(changeTable.getChangeId()!=null)
				sb.append(" and changeId ="+changeTable.getChangeId() + " ");
			if(changeTable.getChangeState()!=null)
				sb.append(" and changeState ="+changeTable.getChangeState() + " ");
		}
		//System.out.println(sb.toString());
		try {
			changeTableList = jdbcTemplate_ccmservice.query(sb.toString(),
					new RowMapper<ChangeTable>() {
						public ChangeTable mapRow(ResultSet rs, int rowNum)
							throws SQLException {
							ChangeTable changeTable = new ChangeTable();
							changeTable.setChangeId(rs.getInt("changeId"));
							changeTable.setChangName(rs.getString("changName")); 
							changeTable.setDeviceType(rs.getInt("deviceType"));
							changeTable.setDeviceID(rs.getInt("deviceID"));
							changeTable.setDeviceParam(rs.getInt("deviceParam"));
							changeTable.setOldValue(rs.getDouble("oldValue"));
							changeTable.setNewValue(rs.getDouble("newValue"));
							changeTable.setCalcCoef(rs.getDouble("calcCoef"));
							changeTable.setMemo(rs.getString("memo"));
							changeTable.setChangeState(rs.getInt("changeState"));
							return changeTable;
						}
					});
		} catch (Exception e) {
			System.out.println("getAllChangeTableInfo()异常:" + e.toString());
			return null;
		}
		return changeTableList;
	}
	

	public String updateCalcParamConfig(CalcParamConfig calcParamConfig) {
		
		StringBuffer sql = new StringBuffer("UPDATE calcparamconfig " +
				" SET fCoef = ?,fOffset= ? WHERE calcID = ?  ;");
		String error = null;
		try {
			jdbcTemplate_ccmservice.update(sql.toString(),
					calcParamConfig.getfCoef(), 
					calcParamConfig.getfOffset(),
					calcParamConfig.getCalcID());
		} catch (Exception e) {
			error = "updateCalcParamConfig：更新数据出错，"+e.getMessage()+"；设别id为："+calcParamConfig.getCalcID();
		}
		return error;
	}

	public String updateChangeTable(ChangeTable changeTable) {
		
		StringBuffer sql = new StringBuffer("UPDATE changetable " +
				" SET changeState = ? WHERE changeId = ?  ;");
		String error = null;
		try {
			jdbcTemplate_ccmservice.update(sql.toString(),
					changeTable.getChangeState(), 
					changeTable.getChangeId());
		} catch (Exception e) {
			error = "updateChangeTable：更新数据出错，"+e.getMessage()+"；设别id为："+changeTable.getChangeId();
		}
		return error;
	}
}
