package cn.zup.iot.timerdecision.dao;

import cn.zup.iot.timerdecision.model.*;
import cn.zup.iot.timerdecision.service.settings.BJLX;
import cn.zup.iot.timerdecision.service.settings.ChangZhanParam;
import cn.zup.iot.timerdecision.util.CommonUtil;
import cn.zup.iot.timerdecision.util.JdbcTemplateUtils;
import cn.zup.iot.timerdecision.util.farmat.MathUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/***
 * 决策树接口
 * @author samson
 *
 */
public class DecisionDao {
	//需要显示的电站状态
	private String stationStat = "4"; //代表已监控电站

	private JdbcTemplate jdbcTemplateDeci = JdbcTemplateUtils.jdbcTemplateMs;

//	public JdbcTemplate getjdbcTemplateDeci() {
//		return jdbcTemplateDeci;
//	}
//
//	public void setJdbcTemplateDeci(JdbcTemplate jdbcTemplateDeci) {
//		this.jdbcTemplateDeci = jdbcTemplateDeci;
//	}

	/**
	 * 获取电站状态 主要是告警信息
	 *
	 * @param stationId
	 * @param dateParam
	 * @return
	 */
	public List<PmWarnRecord> getStationStatus(Integer stationId, Date dateParam) {
		//定义日期 如果传的日期参数为null则获取当前时间，不为null则设置传的日期参数
		Calendar date = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		date.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		if (dateParam != null)
			date.setTime(dateParam);

		int date_year = date.get(Calendar.YEAR);
		int date_month = date.get(Calendar.MONTH) + 1;
		int date_day = date.get(Calendar.DAY_OF_MONTH);
		int date_hour = date.get(Calendar.HOUR_OF_DAY) - 1;
		int date_minute = date.get(Calendar.MINUTE);
		String year = String.valueOf(date_year);
		String month = String.valueOf(date_month);
		String day = String.valueOf(date_day);
		date_minute -= date_minute % 5;
		String fen = String.valueOf(date_minute) + ":00";
		if (date_minute < 10) {
			fen = "0" + date_minute + ":00";
		}
		if (date_month < 10)
			month = "0" + month;

		List<PmWarnRecord> result = new ArrayList<PmWarnRecord>();

		try {
			StringBuffer sb = new StringBuffer();
			sb.append(" select warn_record_id, equipment_id, asset_id, real_Warn_Id,warn_name, warn_set_id, warn_level, warn_type, station_id,occur_time,occur_recover, warn_status, " +
					"	station_name,equipment_name, warn_source, provincename,cityname, equipment_code, send_state" +
					" 	from warnrecord where ((OCCUR_TIME < '" + year + "-" + month + "-" + day + " " + date_hour + ":" + fen + "' and OCCUR_RECOVER > '" + year + "-" + month + "-" + day + " 0:" + fen + "') " +
					"						or ( OCCUR_TIME < '" + year + "-" + month + "-" + day + " " + date_hour + ":" + fen + "' and WARN_STATUS = 1 AND WARN_LEVEL <4 )) "); //1、一般告警 2、外网告警 3、电站告警
			if (stationId != null && stationId != 0)  //如果参数电站传递为null或者0则查询全面告警信息
				sb.append(" and station_id= " + stationId);
//		    System.out.println(sb.toString());
			result = jdbcTemplateDeci.query(sb.toString(), new ResultSetExtractor<List<PmWarnRecord>>() {
				public List<PmWarnRecord> extractData(ResultSet rs) throws SQLException, DataAccessException {
					List<PmWarnRecord> result = new ArrayList<PmWarnRecord>();
					while (rs.next()) {
						PmWarnRecord row = new PmWarnRecord();
						row.setWarn_Name(rs.getString("WARN_NAME"));
						row.setStation_Name(rs.getString("STATION_NAME"));
						row.setProvinceName(rs.getString("PROVINCENAME"));
						row.setCityName(rs.getString("CITYNAME"));
						row.setOccur_Time(rs.getDate("OCCUR_TIME"));
						row.setOccur_Recover(rs.getDate("OCCUR_RECOVER"));
						row.setEquipment_Name(rs.getString("EQUIPMENT_NAME"));
						result.add(row);
					}
					return result;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	/**
	 * 决策树电站状态告警信息查询修改
	 *
	 * @param stationId
	 * @param dateParam
	 * @return 2019年7月31日 下午3:58:50
	 * @author Lzq
	 */
	public List<PmWarnRecord> getStationWarnStatus(Integer stationId, Date dateParam) {
		//定义日期 如果传的日期参数为null则获取当前时间，不为null则设置传的日期参数
		Calendar date = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		date.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		if (dateParam != null)
			date.setTime(dateParam);

		int date_year = date.get(Calendar.YEAR);
		int date_month = date.get(Calendar.MONTH) + 1;
		int date_day = date.get(Calendar.DAY_OF_MONTH);
		int date_hour = date.get(Calendar.HOUR_OF_DAY) - 1;
		int date_minute = date.get(Calendar.MINUTE);
		String year = String.valueOf(date_year);
		String month = String.valueOf(date_month);
		String day = String.valueOf(date_day);
		date_minute -= date_minute % 5;
		String fen = String.valueOf(date_minute) + ":00";
		if (date_minute < 10) {
			fen = "0" + date_minute + ":00";
		}
		if (date_month < 10)
			month = "0" + month;

		List<PmWarnRecord> result = new ArrayList<PmWarnRecord>();

		try {
			StringBuffer sb = new StringBuffer();
			sb.append(" select warn_record_id, equipment_id, asset_id, real_Warn_Id,warn_name, warn_set_id, warn_level, warn_type, station_id,occur_time,occur_recover, warn_status, " +
					"	station_name,equipment_name, warn_source, provincename,cityname, equipment_code, send_state" +
					" 	from warnrecord where (");
			sb.append(" (OCCUR_TIME<'" + year + "-" + month + "-" + day + " 00:00:00' and OCCUR_RECOVER>'" + year + "-" + month + "-" + day + " 23:59:00') ");
			sb.append(" or (OCCUR_TIME<'" + year + "-" + month + "-" + day + " 00:00:00' and WARN_STATUS=1) ");//WARN_LEVEL<5： 1、一般告警 2、电站告警 3、外网告警 4、通讯关闭；WARN_STATUS=1告警未恢复
			sb.append(" or ('" + year + "-" + month + "-" + day + " 00:00:00'<OCCUR_TIME and OCCUR_TIME<'" + year + "-" + month + "-" + day + " 23:59:00') ");
			sb.append(" or ('" + year + "-" + month + "-" + day + " 00:00:00'<OCCUR_RECOVER and OCCUR_RECOVER <'" + year + "-" + month + "-" + day + " 23:59:00')) and WARN_SOURCE=1 and WARN_LEVEL<5 ");
			if (stationId != null && stationId != 0) {  //如果参数电站传递为null或者0则查询全面告警信息
				sb.append(" and station_id= " + stationId);
			}
			System.out.println(sb.toString());
			result = jdbcTemplateDeci.query(sb.toString(), new ResultSetExtractor<List<PmWarnRecord>>() {
				public List<PmWarnRecord> extractData(ResultSet rs) throws SQLException, DataAccessException {
					List<PmWarnRecord> result = new ArrayList<PmWarnRecord>();
					while (rs.next()) {
						PmWarnRecord row = new PmWarnRecord();
						row.setWarn_Name(rs.getString("WARN_NAME"));
						row.setStation_Name(rs.getString("STATION_NAME"));
						row.setProvinceName(rs.getString("PROVINCENAME"));
						row.setCityName(rs.getString("CITYNAME"));
						row.setOccur_Time(rs.getDate("OCCUR_TIME"));
						row.setOccur_Recover(rs.getDate("OCCUR_RECOVER"));
						row.setEquipment_Name(rs.getString("EQUIPMENT_NAME"));
						result.add(row);
					}
					return result;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	/**
	 * 决策树电站状态告警信息查询修改
	 *
	 * @param stationId
	 * @param dateParam
	 * @return 2019年7月31日 下午3:58:50
	 * @author Lzq
	 */
	public List<PmWarnRecord> getStationWarn(Integer stationId, Date dateParam) {
		//定义日期 如果传的日期参数为null则获取当前时间，不为null则设置传的日期参数
		Calendar date = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		date.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		if (dateParam != null)
			date.setTime(dateParam);

		int date_year = date.get(Calendar.YEAR);
		int date_month = date.get(Calendar.MONTH) + 1;
		int date_day = date.get(Calendar.DAY_OF_MONTH);
		int date_hour = date.get(Calendar.HOUR_OF_DAY) - 1;
		int date_minute = date.get(Calendar.MINUTE);
		String year = String.valueOf(date_year);
		String month = String.valueOf(date_month);
		String day = String.valueOf(date_day);
		date_minute -= date_minute % 5;
		String fen = String.valueOf(date_minute) + ":00";
		if (date_minute < 10) {
			fen = "0" + date_minute + ":00";
		}
		if (date_month < 10)
			month = "0" + month;

		List<PmWarnRecord> result = new ArrayList<PmWarnRecord>();

		try {
			StringBuffer sb = new StringBuffer();
			sb.append(" select warn_record_id, equipment_id, asset_id, real_Warn_Id,warn_name, warn_set_id, warn_level, warn_type, station_id,occur_time,occur_recover, warn_status, " +
					"	station_name,equipment_name, warn_source, provincename,cityname, equipment_code, send_state" +
					" 	from warnrecord where (");
			sb.append(" (OCCUR_TIME<'" + year + "-" + month + "-" + day + " 00:00:00' and OCCUR_RECOVER>'" + year + "-" + month + "-" + day + " 23:59:00') ");
			sb.append(" or (OCCUR_TIME<'" + year + "-" + month + "-" + day + " 00:00:00' and WARN_STATUS=1) ");//WARN_LEVEL<5： 1、一般告警 2、电站告警 3、外网告警 4、通讯关闭；WARN_STATUS=1告警未恢复
			sb.append(" or ('" + year + "-" + month + "-" + day + " 00:00:00'<OCCUR_TIME and OCCUR_TIME<'" + year + "-" + month + "-" + day + " 23:59:00') ");
			sb.append(" or ('" + year + "-" + month + "-" + day + " 00:00:00'<OCCUR_RECOVER and OCCUR_RECOVER <'" + year + "-" + month + "-" + day + " 23:59:00')) and WARN_LEVEL<5 ");
			if (stationId != null && stationId != 0) {  //如果参数电站传递为null或者0则查询全面告警信息
				sb.append(" and station_id= " + stationId);
			}
			System.out.println(sb.toString());
			result = jdbcTemplateDeci.query(sb.toString(), new ResultSetExtractor<List<PmWarnRecord>>() {
				public List<PmWarnRecord> extractData(ResultSet rs) throws SQLException, DataAccessException {
					List<PmWarnRecord> result = new ArrayList<PmWarnRecord>();
					while (rs.next()) {
						PmWarnRecord row = new PmWarnRecord();
						row.setWarn_Name(rs.getString("WARN_NAME"));
						row.setStation_Name(rs.getString("STATION_NAME"));
						row.setProvinceName(rs.getString("PROVINCENAME"));
						row.setCityName(rs.getString("CITYNAME"));
						row.setOccur_Time(rs.getDate("OCCUR_TIME"));
						row.setOccur_Recover(rs.getDate("OCCUR_RECOVER"));
						row.setEquipment_Name(rs.getString("EQUIPMENT_NAME"));
						result.add(row);
					}
					return result;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	/**
	 * 决策树电站状态告警信息查询修改
	 *
	 * @param stationId
	 * @param dateParam
	 * @return 2019年7月31日 下午3:58:50
	 * @author ZhangSC
	 */
	public List<PmWarnRecord> getStationNetStatus(Integer stationId, Date dateParam) {
		//定义日期 如果传的日期参数为null则获取当前时间，不为null则设置传的日期参数
		Calendar date = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		date.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		if (dateParam != null)
			date.setTime(dateParam);

		int date_year = date.get(Calendar.YEAR);
		int date_month = date.get(Calendar.MONTH) + 1;
		int date_day = date.get(Calendar.DAY_OF_MONTH);
		int date_hour = date.get(Calendar.HOUR_OF_DAY) - 1;
		int date_minute = date.get(Calendar.MINUTE);
		String year = String.valueOf(date_year);
		String month = String.valueOf(date_month);
		String day = String.valueOf(date_day);
		date_minute -= date_minute % 5;
		String fen = String.valueOf(date_minute) + ":00";
		if (date_minute < 10) {
			fen = "0" + date_minute + ":00";
		}
		if (date_month < 10)
			month = "0" + month;

		List<PmWarnRecord> result = new ArrayList<PmWarnRecord>();

		try {
			StringBuffer sb = new StringBuffer();
			sb.append(" select warn_record_id, equipment_id, asset_id, real_Warn_Id,warn_name, warn_set_id, warn_level, warn_type, station_id,occur_time,occur_recover, warn_status, " +
					"	station_name,equipment_name, warn_source, provincename,cityname, equipment_code, send_state" +
					" 	from warnrecord where (");
			sb.append(" (OCCUR_TIME<'" + year + "-" + month + "-" + day + " 00:00:00' and OCCUR_RECOVER>'" + year + "-" + month + "-" + day + " 23:59:00') ");
			sb.append(" or (OCCUR_TIME<'" + year + "-" + month + "-" + day + " 00:00:00' and WARN_STATUS=1) ");//WARN_LEVEL<5： 1、一般告警 2、电站告警 3、外网告警 4、通讯关闭；WARN_STATUS=1告警未恢复
			sb.append(" or ('" + year + "-" + month + "-" + day + " 00:00:00'<OCCUR_TIME and OCCUR_TIME<'" + year + "-" + month + "-" + day + " 23:59:00') ");
			sb.append(" or ('" + year + "-" + month + "-" + day + " 00:00:00'<OCCUR_RECOVER and OCCUR_RECOVER <'" + year + "-" + month + "-" + day + " 23:59:00')) and WARN_SOURCE!=3 and WARN_LEVEL<5 ");
			if (stationId != null && stationId != 0) {  //如果参数电站传递为null或者0则查询全面告警信息
				sb.append(" and station_id= " + stationId);
			}
			System.out.println(sb.toString());
			result = jdbcTemplateDeci.query(sb.toString(), new ResultSetExtractor<List<PmWarnRecord>>() {
				public List<PmWarnRecord> extractData(ResultSet rs) throws SQLException, DataAccessException {
					List<PmWarnRecord> result = new ArrayList<PmWarnRecord>();
					while (rs.next()) {
						PmWarnRecord row = new PmWarnRecord();
						row.setWarn_Name(rs.getString("WARN_NAME"));
						row.setStation_Name(rs.getString("STATION_NAME"));
						row.setProvinceName(rs.getString("PROVINCENAME"));
						row.setCityName(rs.getString("CITYNAME"));
						row.setOccur_Time(rs.getDate("OCCUR_TIME"));
						row.setOccur_Recover(rs.getDate("OCCUR_RECOVER"));
						row.setEquipment_Name(rs.getString("EQUIPMENT_NAME"));
						result.add(row);
					}
					return result;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	/**
	 * 获取电站 逆变器异常信息
	 *
	 * @param stationId
	 * @param bujianType
	 * @return
	 */
	public List<DeviceInfo> getStationDeviceInfo(Integer stationId, Integer bujianType) {
		List<DeviceInfo> result = new ArrayList<DeviceInfo>();
		try {
			StringBuffer sb = new StringBuffer();
			sb.append(" SELECT A.ID AS deviceId,A.MINGZI AS deviceName,B.ID AS changZhanID,B.MingZi AS StationName,C.ID AS cityId,C.MINGZI AS cityName,D.ID AS provinceId,D.MINGZI AS provinceName" +
					"	FROM COMMDEV A " +
					"	LEFT JOIN CHANGZHAN B ON A.ChangZhanID = B.ID " +
					"	LEFT JOIN subnet_ems C ON B.SUBNETID = C.ID " +
					"	LEFT JOIN DIANWANG D ON C.DianWangID = D.ID where 1=1 ");
			if (stationId != null && stationId != 0)  //如果参数电站传递为null或者0则查询全面告警信息
				sb.append(" and B.ID = " + stationId);
			if (bujianType != null && bujianType != 0) {
				sb.append(" and A.bjlxid = " + bujianType);
			}
//		    System.out.println(sb.toString());
			result = jdbcTemplateDeci.query(sb.toString(), new ResultSetExtractor<List<DeviceInfo>>() {
				public List<DeviceInfo> extractData(ResultSet rs) throws SQLException, DataAccessException {
					List<DeviceInfo> result = new ArrayList<DeviceInfo>();
					while (rs.next()) {
						DeviceInfo row = new DeviceInfo();
						row.setDeviceId(rs.getInt("deviceId"));
						row.setDeviceName(rs.getString("deviceName"));
						row.setChangZhanID(rs.getInt("changZhanID"));
						row.setStationName(rs.getString("StationName"));
						row.setCityId(rs.getInt("cityId"));
						row.setCityName(rs.getString("cityName"));
						row.setProvinceId(rs.getInt("provinceId"));
						row.setProvinceName(rs.getString("provinceName"));

						result.add(row);
					}
					return result;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	/**
	 * 查询commdev表设备，根据厂站id或者设备id获取。
	 *
	 * @param deviceType 6设备层 / 5厂站层
	 *                   deviceType若为5，则deviceId为厂站id
	 * @param bujianType
	 * @return
	 */
	public List<DeviceInfo> getDeviceInfo(Integer deviceId, Integer deviceType, Integer bujianType) {
		List<DeviceInfo> result = new ArrayList<DeviceInfo>();
		try {
			StringBuffer sb = new StringBuffer();
			sb.append(" SELECT A.ID AS deviceId,A.MINGZI AS deviceName,A.ShuoMing as ShuoMing, B.ID AS changZhanID,B.MingZi AS StationName,C.ID AS cityId,C.MINGZI AS cityName,D.ID AS provinceId,D.MINGZI AS provinceName" +
					"	FROM COMMDEV A " +
					"	LEFT JOIN CHANGZHAN B ON A.ChangZhanID = B.ID " +
					"	LEFT JOIN subnet_ems C ON B.SUBNETID = C.ID " +
					"	LEFT JOIN DIANWANG D ON C.DianWangID = D.ID where 1=1 ");
			if (deviceId != null && deviceId != 0 && deviceType == 5) {
				sb.append(" and B.ID = " + deviceId);
			}
			if (deviceId != null && deviceId != 0 && deviceType == 6) {
				sb.append(" and A.ID = " + deviceId);
			}
			if (bujianType != null && bujianType != 0) {
				sb.append(" and A.bjlxid = " + bujianType);
			}
			result = jdbcTemplateDeci.query(sb.toString(), new ResultSetExtractor<List<DeviceInfo>>() {
				public List<DeviceInfo> extractData(ResultSet rs) throws SQLException, DataAccessException {
					List<DeviceInfo> result = new ArrayList<DeviceInfo>();
					while (rs.next()) {
						DeviceInfo row = new DeviceInfo();
						row.setDeviceId(rs.getInt("deviceId"));
						row.setDeviceName(rs.getString("deviceName"));
						row.setChangZhanID(rs.getInt("changZhanID"));
						row.setStationName(rs.getString("StationName"));
						row.setCityId(rs.getInt("cityId"));
						row.setCityName(rs.getString("cityName"));
						row.setProvinceId(rs.getInt("provinceId"));
						row.setProvinceName(rs.getString("provinceName"));
						row.setShuoMing(rs.getString("ShuoMing"));

						result.add(row);
					}
					return result;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	/**
	 * 获取直流侧电流和电压的数据
	 *
	 * @param buJianId
	 * @param buJianType
	 * @param buJianCanshu
	 * @param type
	 * @param dateParam
	 * @return
	 */
	public List<YCInfoData> getPVData(int buJianId, int buJianType, String buJianCanshu, int type, Date dateParam) {
		//定义日期 如果传的日期参数为null则获取当前时间，不为null则设置传的日期参数
		Calendar date = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		date.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		if (dateParam != null)
			date.setTime(dateParam);

		int date_year = date.get(Calendar.YEAR);
		int date_month = date.get(Calendar.MONTH) + 1;
//		int date_day = date.get(Calendar.DAY_OF_MONTH);
		int date_day = 4;
		int date_hour = date.get(Calendar.HOUR_OF_DAY);
		int date_minute = date.get(Calendar.MINUTE);

		String year = String.valueOf(date_year);
		String month = String.valueOf(date_month);
		String day = String.valueOf(date_day);
		date_minute -= date_minute % 5;
		String fen = String.valueOf(date_minute) + ":00";
		if (date_minute < 10) {
			fen = "0" + date_minute + ":00";
		}
//	    if(date_hour>15)
//	    	date_hour = 15;
		if (date_month < 10)
			month = "0" + month;
		String tableName = "ycdata" + year + month;

		List<YCInfoData> result = new ArrayList<YCInfoData>();

		try {
			StringBuffer sb = new StringBuffer();
			sb.append(" select a.riqi,a.H" + date_hour + " AS powers,c.MingZi,b.DYBJID,b.DYBJType,b.DYBJParam " +
					"	from ls." + tableName + " a inner join xunidmnliang b on a.BuJianID = b.id " +
					"	inner join bujiancanshu c on b.DYBJParam=c.id and b.DYBJType=c.bjlxID " +
					"	where a.riqi = '" + year + "-" + month + "-" + day + " 0:" + fen + "' and a.BuJianCanShuID = 2 AND a.BuJianLeiXingID = 20 and b.DYBJID =" + buJianId);
			if (type == 1)  //1代表17逆变器电压
			{
				buJianCanshu = buJianCanshu.equals("") ? "0" : buJianCanshu;
				sb.append(" and b.DYBJParam in(" + buJianCanshu + ") and b.DYBJParam in (3,5,7,9,11,13,211,213) ");
			} else if (type == 2) //2代表17逆变器电流
			{
				buJianCanshu = buJianCanshu.equals("") ? "0" : buJianCanshu;
				sb.append(" and b.DYBJParam in(" + buJianCanshu + ") and b.DYBJParam in (4,6,8,10,12,14,212,214) ");
			} else if (type == 3) //代表全部电压
				sb.append(" and  b.DYBJParam in (3,5,7,9,11,13,211,213) ");
			else if (type == 4) //代表全部电流
				sb.append(" and b.DYBJParam in (4,6,8,10,12,14,212,214) ");
//		    System.out.println(sb.toString()+";");
			result = jdbcTemplateDeci.query(sb.toString(), new ResultSetExtractor<List<YCInfoData>>() {
				public List<YCInfoData> extractData(ResultSet rs) throws SQLException, DataAccessException {
					List<YCInfoData> result = new ArrayList<YCInfoData>();
					while (rs.next()) {
						YCInfoData row = new YCInfoData();
						row.setValue(rs.getDouble("powers"));
						row.setBujiancanshuname(rs.getString("MingZi"));
						row.setBuJianCanShu(rs.getInt("DYBJParam"));
						row.setBuJianId(rs.getInt("DYBJID"));
						row.setBuJianLeiXing(rs.getInt("DYBJType"));

						result.add(row);
					}
					return result;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	/**
	 * 根据传递的电站获取电站电量
	 *
	 * @param stationIds
	 * @param dateParam
	 * @return
	 */
	public List<PowerDayStat> getGroupStationList(String stationIds, Date dateParam, int orderType) {
		//定义日期 如果传的日期参数为null则获取当前时间，不为null则设置传的日期参数
		Calendar date = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		date.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		if (dateParam != null)
			date.setTime(dateParam);


		int date_year = date.get(Calendar.YEAR);
		int date_month = date.get(Calendar.MONTH) + 1;
		int date_day = date.get(Calendar.DAY_OF_MONTH);
		int date_hour = date.get(Calendar.HOUR_OF_DAY) - 1;
		int date_minute = date.get(Calendar.MINUTE);
		final double theoryParam = getTheoryParam(date_year, date_month);


		String year = String.valueOf(date_year);
		String month = String.valueOf(date_month);
		String day = String.valueOf(date_day);
		date_minute -= date_minute % 5;
		String fen = String.valueOf(date_minute) + ":00";
		if (date_minute < 10) {
			fen = "0" + date_minute + ":00";
		}
		if (date_month < 10)
			month = "0" + month;
		String tableName = "kwhdata" + year + month;

		List<PowerDayStat> result = new ArrayList<PowerDayStat>();

		try {
			StringBuffer sb = new StringBuffer();
			sb.append(" select * from " +
					"	( 	select a.id,a.mingzi,b.id as townId,b.mingzi as townName,c.id as countyId,c.mingzi as countyName,d.InstallCapacity as capacity  " +
					"		from changzhan a left join subnet_ems b on a.SUBNETID = b.id left join dianwang c on b.DianWangID = c.id left join stationinfo d on a.ID =d.ChangZhanID " +
					"	) a left join " +
					"	(	SELECT a.riqi,a.H" + date_hour + " AS dayPower,a.bujianId as changzhanid from ls." + tableName + " a	" +
					"		where a.bujianleixingid=" + BJLX.changzhan.getValue() + " and a.bujiancanshuid= " + ChangZhanParam.daypowers.getValue() + " " +
					"		and a.riqi = '" + year + "-" + month + "-" + day + " 0:" + fen + "' " +
					"	) B ON A.id = B.CHANGZHANID " +
					"	left join ( select STATION_ID,GROUP_CONCAT(warnrecord.EQUIPMENT_NAME,warnrecord.WARN_NAME,'发生时间',warnrecord.OCCUR_TIME,'恢复时间',ifnull(warnrecord.OCCUR_RECOVER,'')) as warninfos from warnrecord " +
					"	where ((OCCUR_TIME < '" + year + "-" + month + "-" + day + " " + date_hour + ":" + fen + "' and OCCUR_RECOVER > '" + year + "-" + month + "-" + day + " 0:" + fen + "') or ( OCCUR_TIME < '" + year + "-" + month + "-" + day + " " + date_hour + ":" + fen + "' and WARN_STATUS = 1 )) and WARN_LEVEL < 5 " +
					"	and station_id in (" + stationIds + ") group by STATION_ID " +
					"	) C ON A.ID = C.STATION_ID where a.id in(" + stationIds + ") ");
			if (1 == orderType) {//判断是否进行正序排序
				sb.append(" order by " + "daypower");
			} else if (2 == orderType) {//倒序排列
				sb.append(" order by " + "daypower" + " desc");
			}
//		    System.out.println(sb.toString());
			result = jdbcTemplateDeci.query(sb.toString(), new ResultSetExtractor<List<PowerDayStat>>() {
				public List<PowerDayStat> extractData(ResultSet rs) throws SQLException, DataAccessException {
					List<PowerDayStat> result = new ArrayList<PowerDayStat>();
					while (rs.next()) {
						PowerDayStat row = new PowerDayStat();
						row.setStationId(rs.getInt("id"));
						row.setStationName(rs.getString("mingzi"));
						row.setTownId(rs.getInt("townId"));
						row.setTownName(rs.getString("townName"));
						row.setCountyId(rs.getInt("countyId"));
						row.setCountyName(rs.getString("countyName"));
						row.setDayPower(rs.getDouble("dayPower"));
						row.setMemo(rs.getString("warninfos"));
						row.setCapacity(Double.parseDouble(MathUtil.formatDoubleNonExt(rs.getDouble("capacity"), 2)));//装机容量
						row.setMonthPower(Double.parseDouble(MathUtil.formatDoubleNonExt(rs.getDouble("capacity") * theoryParam, 2)));//理论日发电量
						result.add(row);
					}
					return result;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	/**
	 * 根据传递的电站获取电站电量
	 *
	 * @param stationIds
	 * @param dateParam
	 * @return
	 */
	public List<PowerStatData> getGroupStationPagingList(String stationIds, Date dateParam, int orderType, int pageSize, int PageIndex) {
		//定义日期 如果传的日期参数为null则获取当前时间，不为null则设置传的日期参数
		Calendar date = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		date.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		if (dateParam != null)
			date.setTime(dateParam);


		int date_year = date.get(Calendar.YEAR);
		int date_month = date.get(Calendar.MONTH) + 1;
		int date_day = date.get(Calendar.DAY_OF_MONTH);
		int date_hour = date.get(Calendar.HOUR_OF_DAY) - 1;
		int date_minute = date.get(Calendar.MINUTE);
		final double theoryParam = getTheoryParam(date_year, date_month);


		String year = String.valueOf(date_year);
		String month = String.valueOf(date_month);
		String day = String.valueOf(date_day);
		date_minute -= date_minute % 5;
		String fen = String.valueOf(date_minute) + ":00";
		if (date_minute < 10) {
			fen = "0" + date_minute + ":00";
		}
		if (date_month < 10)
			month = "0" + month;
		String tableName = "kwhdata" + year + month;

		List<PowerStatData> result = new ArrayList<PowerStatData>();

		try {
			StringBuffer sb = new StringBuffer();
			sb.append(" select * from " +
					"	( 	select a.id,a.mingzi,b.id as townId,b.mingzi as townName,c.id as countyId,c.mingzi as countyName,d.InstallCapacity as capacity  " +
					"		from changzhan a left join subnet_ems b on a.SUBNETID = b.id left join dianwang c on b.DianWangID = c.id left join stationinfo d on a.ID =d.ChangZhanID " +
					"	) a left join " +
					"	(	SELECT a.riqi,a.H" + date_hour + " AS dayPower,a.bujianId as changzhanid from ls." + tableName + " a	" +
					"		where a.bujianleixingid=" + BJLX.changzhan.getValue() + " and a.bujiancanshuid= " + ChangZhanParam.daypowers.getValue() + " " +
					"		and a.riqi = '" + year + "-" + month + "-" + day + " 0:" + fen + "' " +
					"	) B ON A.id = B.CHANGZHANID where a.id in(" + stationIds + ") ");
			if (1 == orderType) {//判断是否进行正序排序
				sb.append(" order by " + "daypower");
			} else if (2 == orderType) {//倒序排列
				sb.append(" order by " + "daypower" + " desc");
			}
//		    System.out.println(sb.toString());
			String strSql = CommonUtil.getPageConvert(pageSize, PageIndex, sb.toString());
			result = jdbcTemplateDeci.query(strSql, new ResultSetExtractor<List<PowerStatData>>() {
				public List<PowerStatData> extractData(ResultSet rs) throws SQLException, DataAccessException {
					List<PowerStatData> result = new ArrayList<PowerStatData>();
					while (rs.next()) {
						PowerStatData row = new PowerStatData();
						row.setId(rs.getInt("id"));
						row.setName(rs.getString("mingzi"));
						// row.setTownId(rs.getInt("townId"));
						row.setTownName(rs.getString("townName"));
						//row.setCountyId(rs.getInt("countyId"));
						row.setCountyName(rs.getString("countyName"));
						row.setDayPower(rs.getDouble("dayPower"));
						row.setCapacity(Double.parseDouble(MathUtil.formatDoubleNonExt(rs.getDouble("capacity"), 2)));//装机容量
						row.setMonthPower(Double.parseDouble(MathUtil.formatDoubleNonExt(rs.getDouble("capacity") * theoryParam, 2)));//理论日发电量
						result.add(row);
					}
					return result;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	/**
	 * 获取分组的平均值
	 *
	 * @param stationIds
	 * @param dateParam
	 * @return
	 */
	public HisDataTimeAndValue getGroupAVG(String stationIds, Date dateParam) {
		//定义日期 如果传的日期参数为null则获取当前时间，不为null则设置传的日期参数
		Calendar date = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		date.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		if (dateParam != null)
			date.setTime(dateParam);

		int date_year = date.get(Calendar.YEAR);
		int date_month = date.get(Calendar.MONTH) + 1;
		int date_day = date.get(Calendar.DAY_OF_MONTH);
		int date_hour = date.get(Calendar.HOUR_OF_DAY) - 1;
		int date_minute = date.get(Calendar.MINUTE);

		String year = String.valueOf(date_year);
		String month = String.valueOf(date_month);
		String day = String.valueOf(date_day);
		date_minute -= date_minute % 5;
		String fen = String.valueOf(date_minute) + ":00";
		if (date_minute < 10) {
			fen = "0" + date_minute + ":00";
		}
		if (date_month < 10)
			month = "0" + month;
		String tableName = "kwhdata" + year + month;
		List<HisDataTimeAndValue> result = new ArrayList<HisDataTimeAndValue>();
		try {
			StringBuffer sb = new StringBuffer();
			sb.append(" SELECT a.riqi,sum(a.H" + date_hour + ")/sum(b.InstallCapacity) AS dayPower from ls." + tableName + " a left join stationinfo b on a.BuJianID = b.changzhanid	" +
					"	where a.bujianleixingid=" + BJLX.changzhan.getValue() + " and a.bujiancanshuid= " + ChangZhanParam.daypowers.getValue() + " " +
					"	and a.riqi = '" + year + "-" + month + "-" + day + " 0:" + fen + "' " +
					"	and a.BuJianID in(" + stationIds + ") and a.h" + date_hour + " !=0 " +
					"	and a.bujianid not in ( " +
					"	select STATION_ID as bujianid  from warnrecord " +
					"	where ((OCCUR_TIME < '" + year + "-" + month + "-" + day + " " + date_hour + ":" + fen + "' and OCCUR_RECOVER > '" + year + "-" + month + "-" + day + " 0:" + fen + "') or ( OCCUR_TIME < '" + year + "-" + month + "-" + day + " " + date_hour + ":" + fen + "' and WARN_STATUS = 1 )) and WARN_LEVEL < 5 " +
					"	and station_id in (" + stationIds + ") group by STATION_ID ) ");
//		    System.out.println(sb.toString());
			result = jdbcTemplateDeci.query(sb.toString(), new ResultSetExtractor<List<HisDataTimeAndValue>>() {
				public List<HisDataTimeAndValue> extractData(ResultSet rs) throws SQLException, DataAccessException {
					List<HisDataTimeAndValue> result = new ArrayList<HisDataTimeAndValue>();
					while (rs.next()) {
						HisDataTimeAndValue row = new HisDataTimeAndValue();
						float value = rs.getFloat(2);
						row.setValue(Float.parseFloat(MathUtil.formatDoubleNon3(value))); //修改小数点保留三位
						result.add(row);
					}
					return result;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if (result.size() > 0)
			return result.get(0);
		else
			return null;
	}

	/**
	 * 根据年月获取理论发电量计算参数
	 *
	 * @param year
	 * @param month
	 * @return 每kW的日发电量
	 */
	private double getTheoryParam(int year, int month) {
		//15MW每年的发电量(万kWh）
		double[] yearAmount = {
				1942.77, 1928.82, 1914.87, 1900.93, 1886.98,
				1873.03, 1859.08, 1845.13, 1831.19, 1817.24,
				1803.29, 1789.34, 1775.39, 1761.45, 1747.5,
				1733.55, 1719.6, 1705.65, 1691.71, 1677.76,
				1663.81, 1649.86, 1635.91, 1621.96, 1608.02
		};
		//每月的发电比例
		/*每个月的日照时间
		 *  3.39 3.38 3.89 4.51
			4.8 4.2 3.39 3.55
			3.28 2.79 2.5 2.48
		*/
		double[] monthRatio = {
				0.081952384, 0.073803155, 0.094039756,
				0.105511062, 0.116038773, 0.098258639,
				0.081952384, 0.085820343, 0.076735318,
				0.067447537, 0.058487285, 0.059953366
		};
		double[] monthDays = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
		int beginYear = 2017;
		int index = year - beginYear;
		double theoryParam = yearAmount[index] * 10000 / 15000 * monthRatio[month - 1] / monthDays[month - 1];
		return theoryParam;
	}

	/*
	 * 获取电站详细信息
	 *
	 * author liudelli
	 *date 2016/06/23
	 * */
	public List<DeviceInfo> GetDeviceInfo(int stationID) {
		String sqlString = "";
		List<DeviceInfo> result = new ArrayList<DeviceInfo>();
		try {
			sqlString = "select A.ID AS deviceId,A.MINGZI AS deviceName,c.ID AS changZhanID,c.MingZi AS StationName,d.ID AS cityId,d.MINGZI AS cityName,e.ID AS provinceId," +
					"e.MINGZI AS provinceName,a.shuoming,F.shuoming AS pvconfig ";
			sqlString += " from commdev  a ";    //where changzhanID=
			sqlString += " left join stationinfo b on a.ChangZhanID=b.ChangZhanID ";
			sqlString += " LEFT JOIN CHANGZHAN C ON B.ChangZhanID = C.ID ";

			sqlString += " left join subnet_ems D on D.id = C.SUBNETID "; //乡镇
			//"left join dianwang on dianwang.id = subnet_ems.DianWangID " +
			sqlString += " left join subnet_ems E on E.id=D.parentId ";//	区县

//		    sqlString+=" LEFT JOIN subnet_ems D ON C.SUBNETID = D.ID  "; 
//		    sqlString+=" LEFT JOIN DIANWANG E ON D.DianWangID = E.ID  "; 
			sqlString += " LEFT JOIN devConfig F ON A.ID = F.ID AND A.BJLXID = F.BJLXID where 1=1  ";
			sqlString += " and b.changzhanID= " + stationID;
			//System.out.println(sqlString);
			result = jdbcTemplateDeci.query(sqlString, new ResultSetExtractor<List<DeviceInfo>>() {
				public List<DeviceInfo> extractData(ResultSet rs) throws SQLException, DataAccessException {
					List<DeviceInfo> result = new ArrayList<DeviceInfo>();
					while (rs.next()) {
						DeviceInfo row = new DeviceInfo();
						row.setDeviceId(rs.getInt("deviceId"));
						row.setDeviceName(rs.getString("deviceName"));
						row.setChangZhanID(rs.getInt("changZhanID"));
						row.setStationName(rs.getString("StationName"));
						row.setCityId(rs.getInt("cityId"));
						row.setCityName(rs.getString("cityName"));
						row.setProvinceId(rs.getInt("provinceId"));
						row.setProvinceName(rs.getString("provinceName"));
						row.setShuoMing(rs.getString("shuoming"));
						row.setMemo(rs.getString("pvconfig"));
						result.add(row);
					}
					return result;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	public List<StationInfo> getStationData(String StationName, String StationCode, String StationStat, Integer provinceId, Integer cityId, Integer changZhanId) {
		List<StationInfo> lists = new ArrayList<StationInfo>();
		String sqlString;
		sqlString = "select stationinfo.fullName,stationinfo.Memo,stationinfo.StationStat,stationinfo.ChangZhanID,StationCode,StationName," +
				"StationLat,StationLong,stationinfo.StationType,a.id cityId,a.mingzi cityName," +
				"b.id provinceid,b.mingzi provinceName,VillageInfo.VillageCode,VillageInfo.PoorPopulation," +
				"VillageInfo.PvPoorPopulation,stationinfo.InstallCapacity,stationinfo.PoorNum,stationinfo.BuildAdd," +
				"stationinfo.LandType,stationinfo.UseTax,stationinfo.TotalInvestment,stationinfo.InvestmentSubject," +
				"stationinfo.BuildSubject,stationinfo.FundSource,stationinfo.OperationFoudSource,stationinfo.ExpectPoorIncome," +
				"stationinfo.PoorFamilyIncome,stationinfo.PoorWay,stationinfo.progress,stationinfo.InstallAngle," +
				"stationinfo.ComponentWay,stationinfo.floorArea,stationinfo.meanAtitude,stationinfo.operationTime,stationinfo.operationPeriod,LinkInfo.LinkMan,LinkInfo.LinkPhone from stationinfo " +
				" left join changzhan on changzhan.ID = stationinfo.ChangZhanID " +

				" left join subnet_ems a on a.id = changzhan.SUBNETID " +//乡镇
				//"left join dianwang on dianwang.id = subnet_ems.DianWangID " +
				" left join subnet_ems b on b.id=a.parentId " +//	区县
				" left join subnet_ems c on c.id=b.parentId and c.parentId=0 " +//市
				" left join VillageInfo on VillageInfo.ChangZhanID = stationinfo.ChangZhanID left join LinkInfo on LinkInfo.ChangZhanID = stationinfo.ChangZhanID where 1=1 ";
		if (StationName != null && !StationName.equals(""))
			sqlString += " and StationName like '%" + StationName + "%' ";
		if (StationCode != null && !StationCode.equals(""))
			sqlString += " and StationCode like '%" + StationCode + "%' ";
		if (StationStat != null && !StationStat.equals("") && !StationStat.equals("0"))
			sqlString += " and StationStat like '%" + StationStat + "%' ";
		if (provinceId != null && provinceId != 0)
			sqlString += " and c.id = " + provinceId + " ";
		if (cityId != null && cityId != 0)
			sqlString += " and a.id = " + cityId + " ";
		if (changZhanId != null && changZhanId != 0)
			sqlString += " and stationinfo.ChangZhanID = " + changZhanId + " ";
		try {
			lists = jdbcTemplateDeci.query(sqlString + " order by StationCode",
					new RowMapper<StationInfo>() {
						public StationInfo mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							StationInfo bujian = new StationInfo(rs.getInt("StationStat"), rs.getInt("changZhanID"), rs.getString("StationCode"), rs.getString("StationName"), rs.getString("StationLat"), rs.getString("StationLong"), 1, rs.getInt("StationType"), rs.getInt("cityId"), rs.getInt("provinceId"), rs.getString("cityName"), rs.getString("villageCode"), rs.getString("provinceName"), rs.getFloat("installCapacity"), rs.getInt("poorNum"), rs.getString("buildAdd"), rs.getString("landType"), rs.getFloat("useTax"), rs.getFloat("totalInvestment"), rs.getString("investmentSubject"), rs.getString("buildSubject"), rs.getString("fundSource"), rs.getString("operationFoudSource"), rs.getFloat("expectPoorIncome"), rs.getFloat("poorFamilyIncome"), rs.getInt("poorWay"), rs.getString("progress"), rs.getString("LinkMan"), rs.getString("LinkPhone"), rs.getString("installAngle"), rs.getInt("componentWay"), rs.getFloat("floorArea"), rs.getFloat("meanAtitude"), rs.getDate("operationTime"), rs.getFloat("operationPeriod"), rs.getInt("poorPopulation"), rs.getInt("pvPoorPopulation"), rs.getString("fullName"), rs.getString("Memo"));
							return bujian;
						}
					});
		} catch (Exception e) {
			System.out.println("GetFirstArea()异常:" + e.toString());
		}
		return lists;
	}

	/* 获取容器 ,对应原来的厂站表
	 *  2016.06.08
	 * @Author:by liuxf
	 * @para int dianwangid 一级区域ID
	 * @para int subnetid   二级区域ID
	 * @para int parentID   上一级容器ID
	 *
	 *
	 * */
	public List<Changzhan> getContainer(int dianwangID, int subnetID, int parentID) {

		String sqlStr = "select id,mingzi,subnetid from changzhan left join stationinfo on changzhan.id = stationinfo.changzhanid  where 1=1 ";
		//增加电站过滤条件
		if (!stationStat.equals(""))
			sqlStr += " and stationinfo.StationStat in(" + stationStat + ") ";
		if (dianwangID != 0)
			sqlStr = sqlStr + " and netid=" + dianwangID;
		if (subnetID != 0)
			sqlStr = sqlStr + " and subnetid=" + subnetID;
		if (parentID != 0)
			sqlStr = sqlStr + " and parentID=" + parentID;

		return jdbcTemplateDeci.query(sqlStr,
				new RowMapper<Changzhan>() {
					public Changzhan mapRow(ResultSet rs, int index) throws SQLException {
						return new Changzhan(rs.getInt("id"), rs.getString("mingzi"), rs.getInt("subnetid"));
					}
				});
	}

	/**
	 * 源深系统测验
	 * 2020年12月9日18:55:30
	 *
	 * @Author 史善力
	 */
	public List<DeviceInfo> getYuanshenTest() {
		String sqlStr = "select A.ChangZhanID AS deviceId,A.StationName AS deviceName from stationinfo A where StationLat<=31.8795 ";
		List<DeviceInfo> result = new ArrayList<DeviceInfo>();
		result = jdbcTemplateDeci.query(sqlStr.toString(), new ResultSetExtractor<List<DeviceInfo>>() {
			public List<DeviceInfo> extractData(ResultSet rs) throws SQLException, DataAccessException {
				List<DeviceInfo> result = new ArrayList<DeviceInfo>();
				while (rs.next()) {
					DeviceInfo row = new DeviceInfo();
					row.setDeviceId(rs.getInt("deviceId"));
					row.setDeviceName(rs.getString("deviceName"));
					result.add(row);
				}
				return result;
			}
		});
		return result;
	}

}
