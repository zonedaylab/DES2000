/********************************************************************
 *	版权所有 (C) 2016-2020 积成电子股份有限公司
 *	保留所有版权
 *	
 *	作者：	liuxf
 *	日期：	2016-06-05
 *	摘要：	历史数据查询类
 *  功能：      获取历史数据
 *          
 *
 *********************************************************************/
package cn.zup.iot.timerdecision.dao;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;

import cn.zup.iot.timerdecision.model.*;
import cn.zup.iot.timerdecision.util.DataSourceUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import cn.zup.iot.timerdecision.util.iESDate;
import cn.zup.iot.timerdecision.util.farmat.MathUtil;
import cn.zup.iot.timerdecision.service.StrategyWarnEngineService.s_warn_strategy;
import cn.zup.iot.timerdecision.service.settings.BJLX;
import cn.zup.iot.timerdecision.service.settings.ChangZhanParam;
import cn.zup.iot.timerdecision.service.settings.InfoType;
import cn.zup.iot.timerdecision.service.settings.RegionType;
import org.springframework.stereotype.Component;


/*
 * 类的功能：历史数据查询
 * */


@Component
public class HisDataDao implements Serializable {
	private JdbcTemplate jdbcTemplateHis;
	private String iesbase = "";
	
	public JdbcTemplate getJdbcTemplateHis() {
		return jdbcTemplateHis;
	}

	public void setJdbcTemplateHis(JdbcTemplate jdbcTemplateHis) {
		this.jdbcTemplateHis = jdbcTemplateHis;
	}
	
	private static final String hourStr = "21"; //小时参数默认一天的电量查询为21点数据
	private static final int minuteParam = 300000; //小时参数默认一天的电量查询为21点数据
	
	private static List<String> minuteList = new ArrayList<String>();
	
	static{
		minuteList = genMinuteArr();
	}
	
	public HisDataDao()
	{

		Map map = System.getenv();
		Iterator it = map.entrySet().iterator();
		while(it.hasNext())
		{
			Entry entry = (Entry)it.next();
			if(entry.getKey().equals("IESBASE"))
			{
				System.out.print(entry.getKey()+"=");
				System.out.println(entry.getValue());
				iesbase = entry.getValue().toString();
			}
		}
	}

	//从二进制文件中获得int型变量函数
	private int getInt(InputStream inStream) {
		try {
			return inStream.read() + ((inStream.read()) << 8)
					+ ((inStream.read()) << 16) + ((inStream.read()) << 24);
		} catch (Exception e) {
			return -1;
		}
	}

	/***
	 * 从缓存文件中获取告警信息 
	 * @param BuJianType
	 * @param BuJianCanshu
	 * @param BuJianId
	 * @return
	 */
	public List<YXData> WarnPushData(String BuJianType,String BuJianCanshu,String BuJianId){
		List<YXData> listYX= new ArrayList<YXData>();
		boolean befound = false; // 找到要找的数据
		int buJianType = 0; // 部件类型
		int cSType = 0; // 参数类型
		int buJianID = 0; // 部件ID
		int changZhanID = 0; // 厂站ID
		String buJianTypeStr = "";
		String cSTypeStr = "";
		String buJianIDStr = "";
		String time = "";
		String timeTmp="";

		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		int today_year = today.get(Calendar.YEAR);
		int today_month = today.get(Calendar.MONTH) + 1;
		int today_day = today.get(Calendar.DAY_OF_MONTH);
		int today_hour = today.get(Calendar.HOUR_OF_DAY);
		int today_minute = today.get(Calendar.MINUTE);
		int index = today_minute / 5 ; // 当前要采集数据点的索引
		
		time = Integer.toString(today_year);
		time = time + "-";
		    if (today_month < 10)
		    {
		      time = time + "0";
		      time = time + Integer.toString(today_month);
		    }
		    else {
		      time = time + Integer.toString(today_month);
		    }time = time + "-";
		    if (today_day < 10)
		    {
		      time = time + "0";
		      time = time + Integer.toString(today_day);
		    }
		    else {
		      time = time + Integer.toString(today_day);
		    }time = time + " ";
		    if (today_hour < 10) {
		      time = time + "0";
		      time = time + Integer.toString(today_hour);
		    } else {
		      time = time + Integer.toString(today_hour);
		    }
		    timeTmp = time + ":";
		    time = timeTmp + "00:00";
		    
		System.out.println("获取历史临时文件，当前时间是：" + String.valueOf(today_year) + "-"
				+ String.valueOf(today_month) + "-" + String.valueOf(today_day)
				+ " " + String.valueOf(today_hour));
		String strYear = Integer.toString(today_year);
		String strMonth = today_month < 10 ? ("0" + String.valueOf(today_month))
				: ("" + String.valueOf(today_month));
		String strDay = today_day < 10 ? ("0" + String.valueOf(today_day))
				: ("" + String.valueOf(today_day));
		String strHour = today_hour < 10 ? ("0" + String.valueOf(today_hour))
				: ("" + String.valueOf(today_hour));

		if(iesbase.equals("")) //没有配置环境变量
			return null;
			String strCacheFilePath = iesbase+"/tmp/data_"
//			String strCacheFilePath = "D:/temp/data_"
			+ strYear + "_" + strMonth + strDay + File.separator + "yx5m_"
			//+ strYear + "_" + strMonth + strDay + "_15.dat";
			+ strYear + "_" + strMonth + strDay + "_" + strHour + ".dat";
		
		try {
			FileInputStream fis = new FileInputStream(strCacheFilePath);
			System.out.println(strCacheFilePath);
			DataInputStream dis = new DataInputStream(fis);

			while (dis.available() != 0) { 
				// 部件类型
				buJianType = dis.readByte();
				buJianTypeStr = Integer.toString(buJianType);

				// 部件参数
				cSType = dis.readByte();
				cSTypeStr = Integer.toString(cSType);

				// 部件ID
				buJianID = getInt(dis);
				buJianIDStr = Integer.toString(buJianID);
				changZhanID = getInt(dis);
				if (buJianTypeStr.equals(BuJianType) && cSTypeStr.equals(BuJianCanshu))
					befound = true; // 是要找的数据

				for (int i = 1; i <= 12; i++) {
					// 1个小时内的12个点
					byte bytes = dis.readByte();

					int minTmp = 0;
//					if(befound && i==index)
					if(befound && i==index && bytes !=0)
					{
						minTmp += 5*index;
			            if (minTmp == 5)
				              time = timeTmp + "0" + minTmp + ":00";
				        else if (minTmp <= 55) {
				             if (minTmp == 0)
				                time = timeTmp + "00" + ":00";
				              else
				                time = timeTmp + minTmp + ":00";
				        }
				        else 
				        	time = timeTmp + "00" + ":00";
						YXData yx = new YXData();
			        	try{
			        		yx.setXNbuJianLeiXing(Integer.parseInt(buJianTypeStr));
			        		yx.setXNbuJianId(Integer.parseInt(buJianIDStr));
			        		yx.setXNbuJianCanShu(Integer.parseInt(cSTypeStr));
			        		yx.setChangZhanId(changZhanID);
			        		yx.setTime(time);
			        		yx.setValue(bytes);
			        	}catch(Exception e){
			        		e.printStackTrace();
			        	}
		        		listYX.add(yx);
					}

				}
			}
			fis.close();
			dis.close();
		} catch (IOException e) {
			System.out.println(e.toString());
			listYX = null;
			return null;
			
		}
		return listYX;
	}
	

	/***
	 * 读取三段式的数据 
	 * @param BuJianType
	 * @param BuJianCanshu
	 * @param BuJianId
	 * @param BuJianId
	 * @return
	 */
	public List<YCInfoData> YCInfoPushData(String BuJianType,String BuJianCanshu,String BuJianId ){
		List<YCInfoData> rslist= new ArrayList<YCInfoData>();
		String tmpString = "";

		int ch = 0;
		int s = 0;
		int buJianType = 0; // 部件类型
		int cSType = 0; // 参数类型
		int buJianID = 0; // 部件ID
		int changZhanID = 0; // 厂站ID
		int tmp = 0;
		String buJianTypeStr = "";
		String cSTypeStr = "";
		String buJianIDStr = "";
		String time = "";
		String timeTmp="";

		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		int today_year = today.get(Calendar.YEAR);
		int today_month = today.get(Calendar.MONTH) + 1;
		int today_day = today.get(Calendar.DAY_OF_MONTH);
		int today_hour = today.get(Calendar.HOUR_OF_DAY);
		int today_minute = today.get(Calendar.MINUTE);
		int dataNum = today_minute / 5 + 1; // 当前要采集数据点的个数

		int index = today_minute / 5 ; // 当前要采集数据点的索引
		
		 time = Integer.toString(today_year);
		    time = time + "-";
		    if (today_month < 10)
		    {
		      time = time + "0";
		      time = time + Integer.toString(today_month);
		    }
		    else {
		      time = time + Integer.toString(today_month);
		    }time = time + "-";
		    if (today_day < 10)
		    {
		      time = time + "0";
		      time = time + Integer.toString(today_day);
		    }
		    else {
		      time = time + Integer.toString(today_day);
		    }time = time + " ";
		    if (today_hour < 10) {
		      time = time + "0";
		      time = time + Integer.toString(today_hour);
		    } else {
		      time = time + Integer.toString(today_hour);
		    }
		    timeTmp = time + ":";
		    time = timeTmp + "00:00";
		    
		float f = 0;
		System.out.println("获取历史临时文件，当前时间是：" + String.valueOf(today_year) + "-"
				+ String.valueOf(today_month) + "-" + String.valueOf(today_day)
				+ " " + String.valueOf(today_hour));
		String strYear = Integer.toString(today_year);
		String strMonth = today_month < 10 ? ("0" + String.valueOf(today_month))
				: ("" + String.valueOf(today_month));
		String strDay = today_day < 10 ? ("0" + String.valueOf(today_day))
				: ("" + String.valueOf(today_day));
		String strHour = today_hour < 10 ? ("0" + String.valueOf(today_hour))
				: ("" + String.valueOf(today_hour));

		if(iesbase.equals("")) //没有配置环境变量
			return null;
		String strCacheFilePath = iesbase+"/tmp/data_"
			//String strCacheFilePath = "D:/temp/data_"
			+ strYear + "_" + strMonth + strDay + File.separator + "yc5m_"
			//+ strYear + "_" + strMonth + strDay + "_15.dat";
			+ strYear + "_" + strMonth + strDay + "_" + strHour + ".dat";
		
		
		try {
			FileInputStream fis = new FileInputStream(strCacheFilePath);
			DataInputStream dis = new DataInputStream(fis);
			//将电站通讯信息放到map中--光伏
			//List<YCInfoData> listYc = getYCXNData(BJLX.changzhan.getValue(),ChangZhanParam.stationrunstate.getValue(),0);
			//源深电表--54 9
			List<YCInfoData> listYc = getYCXNData(BJLX.yuanshendianbiao.getValue(),9,0);
			final Map<Integer,Integer> map=new HashMap<Integer,Integer>();
			for(int i=0;i<listYc.size();i++)
			{
	    		map.put(listYc.get(i).getId(),listYc.get(i).getBuJianId());
			}

			while (dis.available() != 0) {
				// 部件类型
				buJianType = dis.readByte();
				buJianTypeStr = Integer.toString(buJianType);

				// 部件参数
				cSType = dis.readByte();
				cSTypeStr = Integer.toString(cSType);

				// 部件ID
				buJianID = getInt(dis);
				buJianIDStr = Integer.toString(buJianID);
				changZhanID = getInt(dis);

				boolean befound = false; // 找到要找的数据
				if (buJianTypeStr.equals(BuJianType) && cSTypeStr.equals(BuJianCanshu) && map.containsKey(buJianID))
					befound = true; // 是要找的数据

				for (int i = 0; i < 12; i++) {
					int minTmp = 0;
					// 1个小时内的12个点
					byte[] bytes = new byte[4];
					bytes[0] = dis.readByte();
					bytes[1] = dis.readByte();
					bytes[2] = dis.readByte();
					bytes[3] = dis.readByte();

					tmp = (((bytes[3] << 24) & 0xff000000)
							| ((bytes[2] << 16) & 0xff0000)
							| ((bytes[1] << 8) & 0xff00) | (bytes[0] & 0xff));
					f = Float.intBitsToFloat(tmp);

					if (befound && i==index) {
						if (i < dataNum) {
							minTmp += 5*index;
				            if (minTmp == 5)
					              time = timeTmp + "0" + minTmp + ":00";
					        else if (minTmp <= 55) {
					             if (minTmp == 0)
					                time = timeTmp + "00" + ":00";
					              else
					                time = timeTmp + minTmp + ":00";
					        }
					        else 
					        	time = timeTmp + "00" + ":00";
							tmpString = Float.toString(f);
							YCInfoData ycData = new YCInfoData();
				        	SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm");
				        	try{
				        	Date d= df.parse(time);
			        		ycData.setBuJianId(map.get(buJianID));
			        		ycData.setXNbuJianLeiXing(Integer.parseInt(buJianTypeStr));
			        		ycData.setXNbuJianId(Integer.parseInt(buJianIDStr));
			        		ycData.setXNbuJianCanShu(Integer.parseInt(cSTypeStr));
			        		ycData.setChangZhanId(changZhanID);
			        		ycData.setTime(time);
			        		ycData.setValue(Double.parseDouble(tmpString));
//			        		hd.setTime(time1);
//			        		hd.setTmptime(time);
//			        		hd.setValue(Float.parseFloat(MathUtil.formatDoubleNon3(Float.valueOf(tmpString))));   //2014.05.07zxm修改小数点保留三位
			        		//hd.setValue(Float.valueOf(tmpString));
				        	}catch(Exception e){
				        		e.printStackTrace();
				        	}
			        		rslist.add(ycData);
						} else if (i == dataNum || i == 12) {
							return rslist;
						}
					}
				}
				f = dis.readFloat();
				s = dis.readByte();
				s = dis.readByte();
				s = dis.readByte();
				f = dis.readFloat();
				s = dis.readByte();
				s = dis.readByte();
				s = dis.readByte();
			}
			fis.close();
			dis.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		return rslist;
	}

	/**
     * 从单精度值中获取遥测设备三段式信息  
     * @param BuJianType
	 * @param BuJianCanshu
	 * @param BuJianId
     * @return
     */
    public List<YCInfoData> getYCXNData(int BuJianType,int BuJianCanshu,int BuJianId ) 
	{
		List<YCInfoData> lists = new ArrayList<YCInfoData>(); 
	    StringBuffer sb= new StringBuffer();
    	sb.append(" select a.*,b.StationName,b.StationCode,d.MingZi as cityName,e.MingZi as proviceName from xunidmnliang a  " +
    				" left join stationinfo b on a.ChangZhanID=b.ChangZhanID  " +
    				" left join changzhan c on c.id = b.ChangZhanID  " +
    				" left join subnet_ems d on d.id = c.SUBNETID  " +
    				" left join  dianwang e on e.id = d.dianwangId " +
    				" where 1=1 ");
    	if(BuJianType !=0)
    		sb.append(" AND DYBJType ="+BuJianType);
    	if(BuJianCanshu !=0)
    		sb.append(" AND DYBJParam ="+BuJianCanshu);
    	if(BuJianId !=0)
    		sb.append(" AND DYBJID ="+BuJianId);
//    	System.out.println(sb.toString());
		try {
			lists = jdbcTemplateHis.query(sb.toString(),
					new RowMapper<YCInfoData>() {
						public YCInfoData mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							YCInfoData ycInfoData = new YCInfoData();
							ycInfoData.setId(rs.getInt("ID"));
							ycInfoData.setBuJianId(rs.getInt("DYBJID"));
							ycInfoData.setBuJianCanShu(rs.getInt("DYBJParam"));
							ycInfoData.setChangZhanId(rs.getInt("ChangZhanId"));
							ycInfoData.setBuJianLeiXing(rs.getInt("DYBJType"));
							ycInfoData.setStationName(rs.getString("StationName"));
							ycInfoData.setStationCode(rs.getString("StationCode"));
							ycInfoData.setCityName(rs.getString("cityName"));
							ycInfoData.setProviceName(rs.getString("proviceName")); 
							return ycInfoData;
						}
					});
		} catch (Exception e) {
			System.out.println("GetArea()异常:" + e.toString());
		} 
		return lists;
	} 
	
    /***
	 * 获取遥测信息
	 * @param yc
	 * @return
	 */
	public List<YCInfoData> getYcDataInfo(YCInfoData yc)
	{
		List<YCInfoData> lists = new ArrayList<YCInfoData>();
	    StringBuffer sb= new StringBuffer();
    	sb.append(" select * from YcDataInfo " +
    				" where 1=1 and bujianleixingId="+yc.getBuJianLeiXing()+" and bujianId="+yc.getBuJianId()+" and bujiancanshuId="+yc.getBuJianCanShu());
//    	System.out.println(sb.toString());
		try {
			lists = jdbcTemplateHis.query(sb.toString(),
					new RowMapper<YCInfoData>() {
						public YCInfoData mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							YCInfoData ycInfoData = new YCInfoData();
							ycInfoData.setBuJianId(rs.getInt("bujianId"));
							ycInfoData.setBuJianLeiXing(rs.getInt("bujianleixingId"));
							ycInfoData.setBuJianCanShu(rs.getInt("bujiancanshuId"));
							ycInfoData.setTime(rs.getString("Time"));
							ycInfoData.setValue(rs.getDouble("ycValue"));
							ycInfoData.setLastValue(rs.getDouble("lastValue"));
							ycInfoData.setLasttimes(rs.getByte("lastTimes"));
							ycInfoData.setCalsStat(rs.getInt("calstat"));
							return ycInfoData;
						}
					});
		} catch (Exception e) {
			System.out.println("GetArea()异常:" + e.toString());
		}
		return lists;
	}
	/***
	 * 插入数据库通讯状态
	 * @param yc
	 * @return 
	 */
	public void InsertYcDataInfo(YCInfoData yc)
	{
		String sql="";
		try {
			sql ="INSERT INTO YcDataInfo VALUES(";
			sql += yc.getBuJianLeiXing()+",";//设备ID
			sql += yc.getBuJianId()+",";//设备ID
			sql += yc.getBuJianCanShu()+",";//设备ID
			sql +=yc.getValue()==null?0+",": yc.getValue()+",";//  设置值
			sql +=yc.getTime()==null?"'2000-1-1 00:00:00',": "'"+yc.getTime()+"',";//  设置时间 
			sql += yc.getLasttimes()+",";//  设置值
			sql += yc.getCalsStat()+",";//  设置值
			sql += yc.getLastValue();//  设置值
			sql +=")";
//			System.out.println(sql);
			jdbcTemplateHis.update(sql); 
		} catch (Exception e) {
			System.out.println("InsertYcDataInfo()异常:" + e.toString());
		}
	}
	/***
	 * 更新数据库通讯状态
	 * @param yc
	 * @return 
	 */
	public void UpdateYcDataInfo(YCInfoData yc)
	{
		String sql="";
		try {
			sql = "update YcDataInfo set ycValue = '"+yc.getValue()+"' ,lastValue='"+yc.getLastValue()+"', lastTimes='"+yc.getLasttimes();
			if(yc.getTime()!=null && !yc.getTime().equals(""))// 判断如果为null或者为空则表示不需要更新
				sql += "', Time='"+yc.getTime();
			sql += "', calstat="+yc.getCalsStat()+" where bujianleixingId="+BJLX.changzhan.getValue()+" and bujiancanshuId=" +ChangZhanParam.stationrunstate.getValue() +" and bujianId = '"+yc.getBuJianId()+"'";
			final String sqlStr = sql;
//			System.out.println(sqlStr);
			jdbcTemplateHis.update(sqlStr); 
		} catch (Exception e) {
			System.out.println("UpdateYcDataInfo()异常:" + e.toString());
		}
	}
	
	public List<StationInfo> getStationData(String StationName,String StationCode,String StationStat,Integer provinceId,Integer cityId,Integer changZhanId)
	{
		List<StationInfo> lists = new ArrayList<StationInfo>();		
		String sqlString ; 
		sqlString="select stationinfo.fullName,stationinfo.Memo,stationinfo.StationStat,stationinfo.ChangZhanID,StationCode,StationName," +
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
				" left join subnet_ems b on b.id=a.parentId "+//	区县
				" left join subnet_ems c on c.id=b.parentId and c.parentId=0 "+//市
				" left join VillageInfo on VillageInfo.ChangZhanID = stationinfo.ChangZhanID left join LinkInfo on LinkInfo.ChangZhanID = stationinfo.ChangZhanID where 1=1 ";
		if(StationName != null && !StationName.equals(""))
			sqlString += " and StationName like '%"+StationName+"%' ";
		if(StationCode != null && !StationCode.equals(""))
			sqlString += " and StationCode like '%"+StationCode+"%' ";
		if(StationStat != null && !StationStat.equals("") && !StationStat.equals("0"))
			sqlString += " and StationStat like '%"+StationStat+"%' ";
		if(provinceId != null && provinceId !=0 )
			sqlString += " and b.id = "+provinceId+" ";
		if(cityId != null && cityId !=0)
			sqlString += " and a.id = "+cityId+" ";
		if(changZhanId != null && changZhanId !=0)
			sqlString += " and stationinfo.ChangZhanID = "+changZhanId+" ";
		try {
			lists = jdbcTemplateHis.query(sqlString + " order by StationCode",
					new RowMapper<StationInfo>() {
						public StationInfo mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							StationInfo bujian = new StationInfo(rs.getInt("StationStat"),rs.getInt("changZhanID"),rs.getString("StationCode"),rs.getString("StationName"),rs.getString("StationLat"),rs.getString("StationLong"),1,rs.getInt("StationType"),rs.getInt("cityId"),rs.getInt("provinceId"),rs.getString("cityName"),rs.getString("villageCode"),rs.getString("provinceName"),rs.getFloat("installCapacity"),rs.getInt("poorNum"),rs.getString("buildAdd"),rs.getString("landType"),rs.getFloat("useTax"),rs.getFloat("totalInvestment"),rs.getString("investmentSubject"),rs.getString("buildSubject"),rs.getString("fundSource"),rs.getString("operationFoudSource"),rs.getFloat("expectPoorIncome"),rs.getFloat("poorFamilyIncome"),rs.getInt("poorWay"),rs.getString("progress"),rs.getString("LinkMan"),rs.getString("LinkPhone"),rs.getString("installAngle"),rs.getInt("componentWay"),rs.getFloat("floorArea"),rs.getFloat("meanAtitude"),rs.getDate("operationTime"),rs.getFloat("operationPeriod"),rs.getInt("poorPopulation"),rs.getInt("pvPoorPopulation"),rs.getString("fullName"),rs.getString("Memo"));
							return bujian;
						}
					});
		} catch (Exception e) {
			System.out.println("GetFirstArea()异常:" + e.toString());
		}
		return lists;	
	}  
	
	/**
	 * 获取区域的电站或逆变器小时电量
	 * @param regionType 区域类型
	 * @param regionId 区域ID
	 * @param BuJianType 部件类型
	 * @param BuJianCanshu 部件参数
	 * @param stDate 开始时间
	 * @param edDate 结束时间
	 * @return
	 */
	public List<HisDataTimeAndValue> getRegionCumulativeHours(int regionType,int regionId, int BuJianType,int BuJianCanshu,Date stDate,Date edDate) {

		 iESDate startDate=new iESDate(stDate);  
		 iESDate endDate =new iESDate(edDate); 
		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());		
	    String todayMonth = startDate.getMonth()<10?"0"+String.valueOf(startDate.getMonth()):String.valueOf(startDate.getMonth());
	    String tableName = "kwhdata" + startDate.getYear() + todayMonth;
	    List<HisDataTimeAndValue> result  = new ArrayList<HisDataTimeAndValue>();	    
	    try{
	    	StringBuffer sb= new StringBuffer();
	    	sb.append(
	    	  
	    	" SELECT * FROM " +
	    	
	    		//1<表名为c ，用来进行排序
	    		"( " + //begin first select 1 
	    		
	    		//2<表名为 b 	获取所有历史数据， 		
	    		"SELECT * FROM  " +  
	    			" (" +  //begin second select 2
	    			
	    			//2-1<.获取历史日期的电度数据	  a为历史数据表  			
	    			"SELECT a.riqi,sum(h0),sum(h1),sum(h2),sum(h3),sum(h4),sum(h5),sum(h6),sum(h7),sum(h8),sum(h9),sum(h10),sum(h11),sum(h12) ,sum(h13) ,sum(h14),sum(h15) ,sum(h16),sum(h17) ,sum(h18),sum(h19),sum(h20),sum(h21),sum(h22),sum(h23) from ls." +tableName+ " a  " +
	    				"where " +
	    				"a.bujianleixingid= "+ BuJianType);
	    	
			    	//如果区域ID为县、区、乡镇，则设置具体的过滤条件
			    	if(regionType!=0 && regionType != RegionType.province.getValue() && regionType != RegionType.city.getValue())
			    	{
			    		if(regionType==RegionType.commdev.getValue())
			    		{
			    			sb.append(" and a.bujianid = "+regionId);
			    		}
			    		else
			    		{
					    	sb.append(//选择所有的厂站ID
			    			" and a.bujianid in " +		    			
			    					"(select changzhan.ID as changzhanid  from changzhan  " +
			    						"inner join subnet_ems  on changzhan.SUBNETID = subnet_ems.id " +
			    						"inner join  dianwang  on subnet_ems.dianwangid = dianwang.id where 1=1 ");
			    	
					    	if(regionType==RegionType.village.getValue())
						    	sb.append(//区域ID为村的所有电站
						    					" and changzhan.id="+regionId+" ) ");		    	
					    	else if(regionType==RegionType.town.getValue())
						    	sb.append(//区域ID为乡镇的所有电站
						    					" and  subnet_ems.id="+regionId +" ) ");		    	
					    	else if(regionType==RegionType.county.getValue())
						    	sb.append(//区域ID为区县级的所有电站
						    					"  and dianwang.id="+regionId +" ) ");
					    	else 
					    		throw new Exception("请选择具体的区域类型,当前区域类型为" +  regionType);		  
			    		}
			    	}
			    	sb.append(
	    				" and a.bujiancanshuid="+ BuJianCanshu+" "+
	    				
	    				" and a.RIQI LIKE '%00:55:00%' "+	    				
	    				//" and a.RIQI != '" + year + "-"+month+"-"+day+" 0:55:00' " +	    				
	    				" and a.RIQI >= '" + startDate.getYear() + "-"+startDate.getMonth()+"-"+startDate.getDayOfMonth()+" 0:55:00' " +
	    				" and a.RIQI <= '" + endDate.getYear() + "-"+endDate.getMonth()+"-"+endDate.getDayOfMonth()+" 0:55:00' " +

	    			"GROUP BY a.RIQI,a.BuJianLeiXingID,a.BuJianCanShuID  ");
			    	
			    	
			    	//开始拼接其他月份的
				    // 其他月份的数据 
			    	if(!(endDate.getYear()==startDate.getYear() && endDate.getMonth()==startDate.getMonth()))
			    	{
			    		while(true)
			    	    {
			    	    	startDate.AddMonth(1);
			    	    	startDate.setDay(1);
			    	    	if(endDate.getMonths()< startDate.getMonths())
			    	    		break;
			    		    todayMonth = startDate.getMonth()<10?"0"+String.valueOf(startDate.getMonth()):String.valueOf(startDate.getMonth());
			    		    tableName = "kwhdata" + startDate.getYear() + todayMonth;
			    			//2-1<.获取历史日期的电度数据	  a为历史数据表  			
			    			sb.append(" UNION ALL SELECT a.riqi,sum(h0),sum(h1),sum(h2),sum(h3),sum(h4),sum(h5),sum(h6),sum(h7),sum(h8),sum(h9),sum(h10),sum(h11),sum(h12) ,sum(h13) ,sum(h14),sum(h15) ,sum(h16),sum(h17) ,sum(h18),sum(h19),sum(h20),sum(h21),sum(h22),sum(h23) from ls." +tableName+ " a  " +
			    				"where " +
			    				"a.bujianleixingid= "+ BuJianType);
			    	
					    	//如果区域ID为县、区、乡镇，则设置具体的过滤条件
					    	if(regionType!=0 && regionType != RegionType.province.getValue() && regionType != RegionType.city.getValue())
					    	{
					    		if(regionType==RegionType.commdev.getValue())
					    		{
					    			sb.append(" and a.bujianid = "+regionId);
					    		}
					    		else
					    		{
							    	sb.append(//选择所有的厂站ID
					    			" and a.bujianid in " +		    			
					    					"(select changzhan.ID as changzhanid  from changzhan  " +
					    						"inner join subnet_ems  on changzhan.SUBNETID = subnet_ems.id " +
					    						"inner join  dianwang  on subnet_ems.dianwangid = dianwang.id where 1=1 ");
					    	
							    	if(regionType==RegionType.village.getValue())
								    	sb.append(//区域ID为村的所有电站
								    					" and changzhan.id="+regionId+" ) ");		    	
							    	else if(regionType==RegionType.town.getValue())
								    	sb.append(//区域ID为乡镇的所有电站
								    					" and  subnet_ems.id="+regionId +" ) ");		    	
							    	else if(regionType==RegionType.county.getValue())
								    	sb.append(//区域ID为区县级的所有电站
								    					"  and dianwang.id="+regionId +" ) ");
							    	else 
							    		throw new Exception("请选择具体的区域类型,当前区域类型为" +  regionType);		  
					    		}
					    	}
					    	sb.append(
			    				" and a.bujiancanshuid="+ BuJianCanshu+" "+
			    				
			    				" and a.RIQI LIKE '%00:55:00%' "+	    				
			    				//" and a.RIQI != '" + year + "-"+month+"-"+day+" 0:55:00' " +	    				
			    				" and a.RIQI >= '" + startDate.getYear() + "-"+startDate.getMonth()+"-"+startDate.getDayOfMonth()+" 0:55:00' " +
			    				" and a.RIQI <= '" + endDate.getYear() + "-"+endDate.getMonth()+"-"+endDate.getDayOfMonth()+" 0:55:00' " +
	
			    			"GROUP BY a.RIQI,a.BuJianLeiXingID,a.BuJianCanShuID  ");
			    			
			    		}
			    	}
			    	
		   sb.append(				
		    	" ) " +	//>end first select 1		    			
		    	"b ORDER BY b.riqi DESC  " + 
		    	
		    " ) " +//>end second select 2
		    "c ORDER BY c.riqi"); 
	    	
	    	
//	    	System.out.println(sb.toString());
	    	
		    result = jdbcTemplateHis.query(sb.toString(), new ResultSetExtractor<List<HisDataTimeAndValue>>() { 
		    public List<HisDataTimeAndValue> extractData(ResultSet rs)  throws SQLException, DataAccessException {  
				 List<HisDataTimeAndValue> result = new ArrayList<HisDataTimeAndValue>();  
				 while(rs.next()) {  
					    //将小时列转换到实体HisDataTimeAndValue中
	                    for(int i = 0;i<=23;i++)
	                    {
						    HisDataTimeAndValue row = new HisDataTimeAndValue(); 
						    Date date=rs.getTimestamp(1);
						    date.setHours(i);
		                    row.setTmptime(date.toString()); 
		                    row.setValue(Float.parseFloat(MathUtil.formatDoubleNon3(rs.getFloat(i+2))));
		                    result.add(row); 
	                    }
				 }  
				 return result;  
		     }});
	    }catch(Exception e){
    		e.printStackTrace();
    	}
	    return result;
	}


	/**
	 * 获取区域的电站或逆变器小时电量(此处均为00:05:00的数据，即整点5分的数据)
	 * @param regionType 区域类型
	 * @param regionId 区域ID
	 * @param BuJianType 部件类型
	 * @param BuJianCanshu 部件参数
	 * @param stDate 开始时间
	 * @param edDate 结束时间
	 * @return
	 */
	public List<HisDataTimeAndValue> getRegionHoursData(int regionType,int regionId, int BuJianType,int BuJianCanshu,Date stDate,Date edDate) {

		iESDate startDate=new iESDate(stDate);
		iESDate endDate =new iESDate(edDate);
		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		String todayMonth = startDate.getMonth()<10?"0"+String.valueOf(startDate.getMonth()):String.valueOf(startDate.getMonth());
		String tableName = "kwhdata" + startDate.getYear() + todayMonth;
		List<HisDataTimeAndValue> result  = new ArrayList<HisDataTimeAndValue>();
		try{
			StringBuffer sb= new StringBuffer();
			sb.append(

					" SELECT * FROM " +

							//1<表名为c ，用来进行排序
							"( " + //begin first select 1

							//2<表名为 b 	获取所有历史数据，
							"SELECT * FROM  " +
							" (" +  //begin second select 2

							//2-1<.获取历史日期的电度数据	  a为历史数据表
							"SELECT a.riqi,sum(h0),sum(h1),sum(h2),sum(h3),sum(h4),sum(h5),sum(h6),sum(h7),sum(h8),sum(h9),sum(h10),sum(h11),sum(h12) ,sum(h13) ,sum(h14),sum(h15) ,sum(h16),sum(h17) ,sum(h18),sum(h19),sum(h20),sum(h21),sum(h22),sum(h23) from ls." +tableName+ " a  " +
							"where " +
							"a.bujianleixingid= "+ BuJianType);

			//如果区域ID为县、区、乡镇，则设置具体的过滤条件
			if(regionType!=0 && regionType != RegionType.province.getValue() && regionType != RegionType.city.getValue())
			{
				if(regionType==RegionType.commdev.getValue())
				{
					sb.append(" and a.bujianid = "+regionId);
				}
				else
				{
					sb.append(//选择所有的厂站ID
							" and a.bujianid in " +
									"(select changzhan.ID as changzhanid  from changzhan  " +
									"inner join subnet_ems  on changzhan.SUBNETID = subnet_ems.id " +
									"inner join  dianwang  on subnet_ems.dianwangid = dianwang.id where 1=1 ");

					if(regionType==RegionType.village.getValue())
						sb.append(//区域ID为村的所有电站
								" and changzhan.id="+regionId+" ) ");
					else if(regionType==RegionType.town.getValue())
						sb.append(//区域ID为乡镇的所有电站
								" and  subnet_ems.id="+regionId +" ) ");
					else if(regionType==RegionType.county.getValue())
						sb.append(//区域ID为区县级的所有电站
								"  and dianwang.id="+regionId +" ) ");
					else
						throw new Exception("请选择具体的区域类型,当前区域类型为" +  regionType);
				}
			}
			sb.append(
					" and a.bujiancanshuid="+ BuJianCanshu+" "+

							" and a.RIQI = '" + startDate.getYear() + "-"+startDate.getMonth()+"-"+startDate.getDayOfMonth()+" 00:05:00' " +
							"GROUP BY a.RIQI,a.BuJianLeiXingID,a.BuJianCanShuID  ");

			sb.append(
					" ) " +	//>end first select 1
							"b ORDER BY b.riqi DESC  " +

							" ) " +//>end second select 2
							"c ORDER BY c.riqi");


				    	System.out.println(sb.toString());

			result = jdbcTemplateHis.query(sb.toString(), new ResultSetExtractor<List<HisDataTimeAndValue>>() {
				public List<HisDataTimeAndValue> extractData(ResultSet rs)  throws SQLException, DataAccessException {
					List<HisDataTimeAndValue> result = new ArrayList<HisDataTimeAndValue>();
					while(rs.next()) {
						//将小时列转换到实体HisDataTimeAndValue中
						for(int i = 0;i<=23;i++)
						{
							HisDataTimeAndValue row = new HisDataTimeAndValue();
							Date date=rs.getTimestamp(1);
							date.setHours(i);
							row.setTmptime(date.toString());
							row.setValue(Float.parseFloat(MathUtil.formatDoubleNon3(rs.getFloat(i+2))));
							result.add(row);
						}
					}
					return result;
				}});
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 获取区域的电站或逆变器小时电量(此处均为00:minute:00的数据，minute为时间传过来的参数)
	 * @param regionType 区域类型
	 * @param regionId 区域ID
	 * @param BuJianType 部件类型
	 * @param BuJianCanshu 部件参数
	 * @param stDate 开始时间
	 * @param edDate 结束时间
	 * @return
	 */
	public List<HisDataTimeAndValue> getRegionHoursMinuteData(int regionType,int regionId, int BuJianType,int BuJianCanshu,Date stDate,Date edDate) {

		iESDate startDate=new iESDate(stDate);
		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		String todayMonth = startDate.getMonth()<10?"0"+String.valueOf(startDate.getMonth()):String.valueOf(startDate.getMonth());
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(stDate.getTime()-300000);//往前移5分钟的数据
		int ist_minute =  calendar.get(Calendar.MINUTE);
		ist_minute -= ist_minute%5;
		String sst_minute = "";
		if(ist_minute<10)
			sst_minute = "0"+ist_minute;
		else
			sst_minute = ""+ist_minute;
		String tableName = "kwhdata" + startDate.getYear() + todayMonth;
		List<HisDataTimeAndValue> result  = new ArrayList<HisDataTimeAndValue>();
		try{
			StringBuffer sb= new StringBuffer();
			sb.append(

					" SELECT * FROM " +

							//1<表名为c ，用来进行排序
							"( " + //begin first select 1

							//2<表名为 b 	获取所有历史数据，
							"SELECT * FROM  " +
							" (" +  //begin second select 2

							//2-1<.获取历史日期的电度数据	  a为历史数据表
							"SELECT a.riqi,sum(h0),sum(h1),sum(h2),sum(h3),sum(h4),sum(h5),sum(h6),sum(h7),sum(h8),sum(h9),sum(h10),sum(h11),sum(h12) ,sum(h13) ,sum(h14),sum(h15) ,sum(h16),sum(h17) ,sum(h18),sum(h19),sum(h20),sum(h21),sum(h22),sum(h23) from ls." +tableName+ " a  " +
							"where " +
							"a.bujianleixingid= "+ BuJianType);

			//如果区域ID为县、区、乡镇，则设置具体的过滤条件
			if(regionType!=0 && regionType != RegionType.province.getValue() && regionType != RegionType.city.getValue())
			{
				if(regionType==RegionType.commdev.getValue())
				{
					sb.append(" and a.bujianid = "+regionId);
				}
				else
				{
					sb.append(//选择所有的厂站ID
							" and a.bujianid in " +
									"(select changzhan.ID as changzhanid  from changzhan  " +
									"inner join subnet_ems  on changzhan.SUBNETID = subnet_ems.id " +
									"inner join  dianwang  on subnet_ems.dianwangid = dianwang.id where 1=1 ");

					if(regionType==RegionType.village.getValue())
						sb.append(//区域ID为村的所有电站
								" and changzhan.id="+regionId+" ) ");
					else if(regionType==RegionType.town.getValue())
						sb.append(//区域ID为乡镇的所有电站
								" and  subnet_ems.id="+regionId +" ) ");
					else if(regionType==RegionType.county.getValue())
						sb.append(//区域ID为区县级的所有电站
								"  and dianwang.id="+regionId +" ) ");
					else
						throw new Exception("请选择具体的区域类型,当前区域类型为" +  regionType);
				}
			}
			sb.append(
					" and a.bujiancanshuid="+ BuJianCanshu+" "+

							" and a.RIQI = '" + startDate.getYear() + "-"+startDate.getMonth()+"-"+startDate.getDayOfMonth()+" 00:"+sst_minute+":00' " +
							"GROUP BY a.RIQI,a.BuJianLeiXingID,a.BuJianCanShuID  ");

			sb.append(
					" ) " +	//>end first select 1
							"b ORDER BY b.riqi DESC  " +

							" ) " +//>end second select 2
							"c ORDER BY c.riqi");


			System.out.println(sb.toString());

			result = jdbcTemplateHis.query(sb.toString(), new ResultSetExtractor<List<HisDataTimeAndValue>>() {
				public List<HisDataTimeAndValue> extractData(ResultSet rs)  throws SQLException, DataAccessException {
					List<HisDataTimeAndValue> result = new ArrayList<HisDataTimeAndValue>();
					while(rs.next()) {
						//将小时列转换到实体HisDataTimeAndValue中
						for(int i = 0;i<=23;i++)
						{
							HisDataTimeAndValue row = new HisDataTimeAndValue();
							Date date=rs.getTimestamp(1);
							date.setHours(i);
							row.setTmptime(date.toString());
							row.setValue(Float.parseFloat(MathUtil.formatDoubleNon3(rs.getFloat(i+2))));
							result.add(row);
						}
					}
					return result;
				}});
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 获取区域的电站或逆变器小时电量
	 * @param regionType 区域类型
	 * @param regionId 区域ID
	 * @param BuJianType 部件类型
	 * @param BuJianCanshu 部件参数
	 * @param stDate 开始时间
	 * @param edDate 结束时间
	 * @return
	 */
	public List<HisDataTimeAndValue> getHoursYcData(int regionType,int regionId, int BuJianType,int BuJianCanshu,Date stDate,Date edDate) {

		iESDate startDate=new iESDate(stDate);
		iESDate endDate =new iESDate(edDate);
		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		String todayMonth = startDate.getMonth()<10?"0"+String.valueOf(startDate.getMonth()):String.valueOf(startDate.getMonth());
		String tableName = "ycdata" + startDate.getYear() + todayMonth;
		List<HisDataTimeAndValue> result  = new ArrayList<HisDataTimeAndValue>();
		try{
			StringBuffer sb= new StringBuffer();
			sb.append(

					" SELECT * FROM " +

							//1<表名为c ，用来进行排序
							"( " + //begin first select 1

							//2<表名为 b 	获取所有历史数据，
							"SELECT * FROM  " +
							" (" +  //begin second select 2

							//2-1<.获取历史日期的电度数据	  a为历史数据表
							"SELECT a.riqi,sum(h0),sum(h1),sum(h2),sum(h3),sum(h4),sum(h5),sum(h6),sum(h7),sum(h8),sum(h9),sum(h10),sum(h11),sum(h12) ,sum(h13) ,sum(h14),sum(h15) ,sum(h16),sum(h17) ,sum(h18),sum(h19),sum(h20),sum(h21),sum(h22),sum(h23) from ls." +tableName+ " a  " +
							"where " +
							"a.bujianleixingid= "+ 20);

			//如果区域ID为县、区、乡镇，则设置具体的过滤条件
			if(regionType!=0 && regionType != RegionType.province.getValue() && regionType != RegionType.city.getValue())
			{
				if(regionType==RegionType.commdev.getValue())
				{
					sb.append(" and a.bujianid = (select id from xunidmnliang where DYBJID =  " + regionId +
							" and DYBJParam = "+BuJianCanshu+" and DYBJType = "+BuJianType+")");
				}
				else
				{
					sb.append(//选择所有的厂站ID
							" and a.bujianid in " +
									"(select changzhan.ID as changzhanid  from changzhan  " +
									"inner join subnet_ems  on changzhan.SUBNETID = subnet_ems.id " +
									"inner join  dianwang  on subnet_ems.dianwangid = dianwang.id where 1=1 ");

					if(regionType==RegionType.village.getValue())
						sb.append(//区域ID为村的所有电站
								" and changzhan.id="+regionId+" ) ");
					else if(regionType==RegionType.town.getValue())
						sb.append(//区域ID为乡镇的所有电站
								" and  subnet_ems.id="+regionId +" ) ");
					else if(regionType==RegionType.county.getValue())
						sb.append(//区域ID为区县级的所有电站
								"  and dianwang.id="+regionId +" ) ");
					else
						throw new Exception("请选择具体的区域类型,当前区域类型为" +  regionType);
				}
			}
			sb.append(
					" and a.bujiancanshuid= 2  and a.RIQI = '" + startDate.getYear() + "-"+startDate.getMonth()+"-"+startDate.getDayOfMonth()+" 00:05:00' " +
							"GROUP BY a.RIQI,a.BuJianLeiXingID,a.BuJianCanShuID  ");

			sb.append(
					" ) " +	//>end first select 1
							"b ORDER BY b.riqi DESC  " +

							" ) " +//>end second select 2
							"c ORDER BY c.riqi");


			System.out.println(sb.toString());

			result = jdbcTemplateHis.query(sb.toString(), new ResultSetExtractor<List<HisDataTimeAndValue>>() {
				public List<HisDataTimeAndValue> extractData(ResultSet rs)  throws SQLException, DataAccessException {
					List<HisDataTimeAndValue> result = new ArrayList<HisDataTimeAndValue>();
					while(rs.next()) {
						//将小时列转换到实体HisDataTimeAndValue中
						for(int i = 0;i<=23;i++)
						{
							HisDataTimeAndValue row = new HisDataTimeAndValue();
							Date date=rs.getTimestamp(1);
							date.setHours(i);
							row.setTmptime(date.toString());
							row.setValue(Float.parseFloat(MathUtil.formatDoubleNon3(rs.getFloat(i+2))));
							result.add(row);
						}
					}
					return result;
				}});
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 获取区域的电站或逆变器小时电量
	 * @param regionType 区域类型
	 * @param regionId 区域ID
	 * @param BuJianType 部件类型
	 * @param BuJianCanshu 部件参数
	 * @param stDate 开始时间
	 * @param edDate 结束时间
	 * @return
	 */
	public List<HisDataTimeAndValue> getRegionHoursTime(int regionType,int regionId, int BuJianType,int BuJianCanshu,Date stDate,Date edDate) {

		iESDate startDate=new iESDate(stDate);
		iESDate endDate =new iESDate(edDate);
		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		String todayMonth = startDate.getMonth()<10?"0"+String.valueOf(startDate.getMonth()):String.valueOf(startDate.getMonth());
		String tableName = "kwhdata" + startDate.getYear() + todayMonth;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(stDate.getTime()-300000);//往前移5分钟的数据
		int ist_minute =  calendar.get(Calendar.MINUTE);
		ist_minute -= ist_minute%5;
		String sst_minute = "";
		if(ist_minute<10)
			sst_minute = "0"+ist_minute;
		else
			sst_minute = ""+ist_minute;
		List<HisDataTimeAndValue> result  = new ArrayList<HisDataTimeAndValue>();
		try{
			StringBuffer sb= new StringBuffer();
			sb.append(

					" SELECT * FROM " +

							//1<表名为c ，用来进行排序
							"( " + //begin first select 1

							//2<表名为 b 	获取所有历史数据，
							"SELECT * FROM  " +
							" (" +  //begin second select 2

							//2-1<.获取历史日期的电度数据	  a为历史数据表
							"SELECT a.riqi,sum(h0),sum(h1),sum(h2),sum(h3),sum(h4),sum(h5),sum(h6),sum(h7),sum(h8),sum(h9),sum(h10),sum(h11),sum(h12) ,sum(h13) ,sum(h14),sum(h15) ,sum(h16),sum(h17) ,sum(h18),sum(h19),sum(h20),sum(h21),sum(h22),sum(h23) from ls." +tableName+ " a  " +
							"where " +
							"a.bujianleixingid= "+ BuJianType);

			//如果区域ID为县、区、乡镇，则设置具体的过滤条件
			if(regionType!=0 && regionType != RegionType.province.getValue() && regionType != RegionType.city.getValue())
			{
				if(regionType==RegionType.commdev.getValue())
				{
					sb.append(" and a.bujianid = "+regionId);
				}
				else
				{
					sb.append(//选择所有的厂站ID
							" and a.bujianid in " +
									"(select changzhan.ID as changzhanid  from changzhan  " +
									"inner join subnet_ems  on changzhan.SUBNETID = subnet_ems.id " +
									"inner join  dianwang  on subnet_ems.dianwangid = dianwang.id where 1=1 ");

					if(regionType==RegionType.village.getValue())
						sb.append(//区域ID为村的所有电站
								" and changzhan.id="+regionId+" ) ");
					else if(regionType==RegionType.town.getValue())
						sb.append(//区域ID为乡镇的所有电站
								" and  subnet_ems.id="+regionId +" ) ");
					else if(regionType==RegionType.county.getValue())
						sb.append(//区域ID为区县级的所有电站
								"  and dianwang.id="+regionId +" ) ");
					else
						throw new Exception("请选择具体的区域类型,当前区域类型为" +  regionType);
				}
			}
			sb.append(
					" and a.bujiancanshuid="+ BuJianCanshu+" "+

							" and a.RIQI = '" + startDate.getYear() + "-"+startDate.getMonth()+"-"+startDate.getDayOfMonth()+" 00:"+sst_minute+":00' " +
							"GROUP BY a.RIQI,a.BuJianLeiXingID,a.BuJianCanShuID  ");

			sb.append(
					" ) " +	//>end first select 1
							"b ORDER BY b.riqi DESC  " +

							" ) " +//>end second select 2
							"c ORDER BY c.riqi");


			System.out.println(sb.toString());

			result = jdbcTemplateHis.query(sb.toString(), new ResultSetExtractor<List<HisDataTimeAndValue>>() {
				public List<HisDataTimeAndValue> extractData(ResultSet rs)  throws SQLException, DataAccessException {
					List<HisDataTimeAndValue> result = new ArrayList<HisDataTimeAndValue>();
					while(rs.next()) {
						//将小时列转换到实体HisDataTimeAndValue中
						for(int i = 0;i<=23;i++)
						{
							HisDataTimeAndValue row = new HisDataTimeAndValue();
							Date date=rs.getTimestamp(1);
							date.setHours(i);
							row.setTmptime(date.toString());
							row.setTime(rs.getLong(i+2));
							result.add(row);
						}
					}
					return result;
				}});
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 标杆电量存库
	 * mxf
	 * 
	 * 2017年10月6日11:30:58
	 * @param list
	 */
	public void insertBenchmarkElectricity(List<cn.zup.iot.timerdecision.model.ForecastPower> list, Date date) {
//		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
//		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis()-minuteParam); //获取前minuteParam数据 
//		
//		int today_year = today.get(Calendar.YEAR);
//		int today_month = today.get(Calendar.MONTH) + 1;
//		
//		String year = String.valueOf(today_year);
//	    String month = String.valueOf(today_month);

		Calendar time = Calendar.getInstance();
		time.setTime(date);

		int today_year = time.get(Calendar.YEAR);
		int today_month = time.get(Calendar.MONTH) + 1;
		
		String year = String.valueOf(today_year);
	    String month = String.valueOf(today_month);
	    
	    if(today_month<10)
	    	month = "0"+month;
	    String tableName = "EnergyData" + year + month;
		 
        SimpleDateFormat  SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        
		try{
		if(list != null) {
			for(cn.zup.iot.timerdecision.model.ForecastPower forecastPower : list) {
				String sql = "insert into ls." +tableName + " values(" + forecastPower.getHourPower() + ",'" +
				SimpleDateFormat.format(forecastPower.getRiqi().getTime()) + "'," + forecastPower.getBuJianLeiXingID() + "," + 
				forecastPower.getBuJianID() + "," + forecastPower.getBuJianCanShuID() + "," + 
				forecastPower.getChangZhanID() + "," + forecastPower.getValue() + "," + forecastPower.getValueFlag() + ")";
				
				jdbcTemplateHis.update(sql);
			}
		}
		}catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * 获取区域下历史电量数据
	 * by liuxf
	 * 2016.12.21. 11:16
	 * 修改日期：2017年7月20日11:32:03     mxf
	 * @param regionType  区域类型
	 * @param regionId	  区域ID
	 * @param BuJianType 部件类型
	 * @param BuJianCanshu 部件参数
	 * @param stDate 开始日期
	 * @param edDate  结束日期
	 * @param batchType 批次 如果传0 则全部
	 * @return
	 * @throws ParseException 
	 */
	public List<HisDataTimeAndValue> getRegionHistPowerData(int regionType,
			int regionId, int BuJianType,int BuJianCanshu,
			Date stDate,Date edDate,Integer batchType) {
	  
		 iESDate startDate=new iESDate(stDate);  
		 iESDate endDate =new iESDate(edDate);
		 Date date = new Date();
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		 
	    
		 List<HisDataTimeAndValue> histDataList = new LinkedList<HisDataTimeAndValue>();
		 try{
			 if(startDate.getYear() == endDate.getYear()){//同一年
				 if(startDate.getMonth() == endDate.getMonth()){//同一个月
					 for(int i=startDate.getDayOfMonth();i<=endDate.getDayOfMonth();i++){
						 date = sdf.parse(startDate.getYear() + "-" + startDate.getMonth() + "-" + i);
						 HisDataTimeAndValue hisDataTimeAndValue =  getRegionMonthHistPowerData(regionType,regionId,BuJianType,
									BuJianCanshu,batchType,date);//查询每一天的发电量
						 histDataList.add(hisDataTimeAndValue);
					 }
				 }else{//跨月
					 for(int i = startDate.getMonth();i<=endDate.getMonth();i++){
						 //该月有多少天
			    		 Calendar a = Calendar.getInstance();  
		    	         a.set(Calendar.YEAR, startDate.getYear());  
		    	         a.set(Calendar.MONTH, i - 1);  
		    	         a.set(Calendar.DATE, 1);  
		    	         a.roll(Calendar.DATE, -1);  
			    	       
		    	         int days =  a.getActualMaximum(Calendar.DAY_OF_MONTH);//该月的天数
		    	         
						 if(i == startDate.getMonth()){//开始月份
							 for(int m = startDate.getDayOfMonth();m<=days;m++){
								 date = sdf.parse(startDate.getYear() + "-" + i + "-" + m);
								 HisDataTimeAndValue hisDataTimeAndValue =  getRegionMonthHistPowerData(regionType,regionId,BuJianType,
											BuJianCanshu,batchType,date);//查询每一天的发电量
								 histDataList.add(hisDataTimeAndValue);
							 }
						 }else if(i == endDate.getMonth()){//最后月份
							 for(int m = 1;m<=endDate.getDayOfMonth();m++){
								 date = sdf.parse(startDate.getYear() + "-" + i + "-" + m);
								 HisDataTimeAndValue hisDataTimeAndValue =  getRegionMonthHistPowerData(regionType,regionId,BuJianType,
											BuJianCanshu,batchType,date);//查询每一天的发电量
								 histDataList.add(hisDataTimeAndValue);
							 }
						 }else{//中间月份
							 for(int m = 1;m<=days;m++){
								 date = sdf.parse(startDate.getYear() + "-" + i + "-" + m);
								 HisDataTimeAndValue hisDataTimeAndValue =  getRegionMonthHistPowerData(regionType,regionId,BuJianType,
											BuJianCanshu,batchType,date);//查询每一天的发电量
								 histDataList.add(hisDataTimeAndValue);
							 }
						 }
					 }
				 }
			 }else{//跨年
				 for(int i=startDate.getYear();i<=endDate.getYear();i++){
					 if(startDate.getYear() == i){//首年
						 for(int m = startDate.getMonth();m<=12;m++){//循环该年的月份
							 //该月有多少天
				    		 Calendar a = Calendar.getInstance();  
			    	         a.set(Calendar.YEAR, i);  
			    	         a.set(Calendar.MONTH, m - 1);  
			    	         a.set(Calendar.DATE, 1);  
			    	         a.roll(Calendar.DATE, -1);
			    	         int days =  a.getActualMaximum(Calendar.DAY_OF_MONTH);//该月的天数
			    	         
			    	         if(startDate.getMonth() == m){//开始月份
			    	        	 for(int n = startDate.getDayOfMonth();n<=days;n++){
			    	        		 date = sdf.parse(i + "-" + m + "-" + n);
			    	        		 HisDataTimeAndValue hisDataTimeAndValue =  getRegionMonthHistPowerData(regionType,regionId,BuJianType,
												BuJianCanshu,batchType,date);//查询每一天的发电量
			    	        		 histDataList.add(hisDataTimeAndValue);
			    	        	 }
			    	         }else{//首年最后的几个月份
			    	        	 for(int n = 1;n<=days;n++){
			    	        		 date = sdf.parse(i + "-" + m + "-" + n);
			    	        		 HisDataTimeAndValue hisDataTimeAndValue =  getRegionMonthHistPowerData(regionType,regionId,BuJianType,
												BuJianCanshu,batchType,date);//查询每一天的发电量
			    	        		 histDataList.add(hisDataTimeAndValue);
			    	        	 }
			    	         }
						 }
					 }else if(i == endDate.getYear()){//最后一年
						 for(int m = 1;m<=endDate.getMonth();m++){
							 //该月有多少天
				    		 Calendar a = Calendar.getInstance();  
			    	         a.set(Calendar.YEAR, i);  
			    	         a.set(Calendar.MONTH, m - 1);  
			    	         a.set(Calendar.DATE, 1);  
			    	         a.roll(Calendar.DATE, -1);
			    	         int days =  a.getActualMaximum(Calendar.DAY_OF_MONTH);//该月的天数
			    	         
			    	         if(m == endDate.getMonth()){//最后一年的最后一个月
			    	        	 for(int n = 1;n<=endDate.getDayOfMonth();n++){
			    	        		 date = sdf.parse(i + "-" + m + "-" + n);
			    	        		 HisDataTimeAndValue hisDataTimeAndValue =  getRegionMonthHistPowerData(regionType,regionId,BuJianType,
												BuJianCanshu,batchType,date);//查询每一天的发电量
			    	        		 histDataList.add(hisDataTimeAndValue);
			    	        	 }
			    	         }else{//最后一年前面的月份
			    	        	 for(int n = 1;n<=days;n++){
			    	        		 date = sdf.parse(i + "-" + m + "-" + n);
			    	        		 HisDataTimeAndValue hisDataTimeAndValue =  getRegionMonthHistPowerData(regionType,regionId,BuJianType,
												BuJianCanshu,batchType,date);//查询每一天的发电量
			    	        		 histDataList.add(hisDataTimeAndValue);
			    	        	 }
			    	         }
						 }
					 }else{//中间年份
						 for(int m = 1;m<=12;m++){
							 //该月有多少天
				    		 Calendar a = Calendar.getInstance();  
			    	         a.set(Calendar.YEAR, i);  
			    	         a.set(Calendar.MONTH, m - 1);  
			    	         a.set(Calendar.DATE, 1);  
			    	         a.roll(Calendar.DATE, -1);
			    	         int days =  a.getActualMaximum(Calendar.DAY_OF_MONTH);//该月的天数
			    	         
			    	         for(int n = 1;n<= days;n++){
			    	        	 date = sdf.parse(i + "-" + m + "-" + n);
			    	        	 HisDataTimeAndValue hisDataTimeAndValue =  getRegionMonthHistPowerData(regionType,regionId,BuJianType,
											BuJianCanshu,batchType,date);//查询每一天的发电量
			    	        	 histDataList.add(hisDataTimeAndValue);
			    	         }
						 }
					 }
				 }
			 }
		 }catch(Exception e){
			 e.printStackTrace();
		 }
	    return histDataList;    
	}
	
	/**
	 * 获取区域下历史某个月电量数据
	 * by liuxf
	 * 2016.12.21. 11:16
	 * @param regionType  区域类型
	 * @param regionId	  区域ID
	 * @param BuJianType 部件类型
	 * @param BuJianCanshu 部件参数
	 * @param date 日期
	 * @param batchType 批次 如果传0 则全部
	 * @return
	 */
	public HisDataTimeAndValue getRegionMonthHistPowerData(int regionType,
			int regionId, int BuJianType,int BuJianCanshu,
			Integer batchType,Date date) {
		
		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis()-minuteParam); //获取前minuteParam数据 
		
		int today_year = today.get(Calendar.YEAR);
		int today_month = today.get(Calendar.MONTH) + 1;
		int today_day = today.get(Calendar.DAY_OF_MONTH);
		int today_hour = today.get(Calendar.HOUR_OF_DAY);	
		if(today.get(Calendar.HOUR_OF_DAY)==0)
		{
			today_day-=1;
			today_hour = 23;
		}		
		int today_minute = today.get(Calendar.MINUTE); 	
		String year = String.valueOf(today_year);
	    String month = String.valueOf(today_month);
	    String day = String.valueOf(today_day);	    
		today_minute -= today_minute%5;
		if(today_hour>Integer.parseInt(hourStr))
			today_hour=Integer.parseInt(hourStr);
	    String fen = String.valueOf(today_minute)+":00";
	    if(today_minute<10){
	    	fen="0"+today_minute+":00";
	    }
	    iESDate variableDate=new iESDate(date);  
	    String todayMonth = variableDate.getMonth()<10?"0"+String.valueOf(variableDate.getMonth()):String.valueOf(variableDate.getMonth());
	    String tableName = "kwhdata" + variableDate.getYear() + todayMonth;
	    HisDataTimeAndValue result = new HisDataTimeAndValue();
	    try{
	    	StringBuffer sb= new StringBuffer();
	 	     if( (variableDate.getYear() + "-" + variableDate.getMonth() + "-" + variableDate.getDayOfMonth()).equals(
	 	    		today_year + "-" + today_month + "-" + today_day) ){//若为当天时间
	 	    	sb.append(
	 		    		
	 		    			//<2-2.获取当前日期的电度数据	
	 		    			" select a.riqi,SUM(a.H"+today_hour+") AS powers from ls."+tableName+" a ");

	 					//增加电站批次过滤
	 					if(batchType!=null && batchType!=0&&BuJianType==BJLX.changzhan.getValue())
	 						sb.append( " inner JOIN stationinfo b on a.BuJianID = b.ChangZhanID and b.batch =" + batchType);    
	 					
	 		    			sb.append( " where " +
	 		    				  " a.bujianleixingid="+BuJianType+" ");	  	
	 		    	
	 		    	if(regionType!=0 && regionType != RegionType.province.getValue())
	 		    	{
	 		    		if(regionType==RegionType.commdev.getValue())
	 		    		{
	 		    			sb.append(" and a.bujianid = "+regionId);
	 		    		}
	 		    		else
	 		    		{
	 				    	sb.append(" and a.changzhanid in " + // 选择所有的厂站ID
	 				    			" (SELECT D.ID  FROM ( SELECT ID,MINGZI,parentId FROM subnet_ems WHERE parentId = 0 ) A  " +
	 				    			" INNER JOIN ( SELECT ID,MINGZI,parentId FROM subnet_ems  ) B ON A.ID = B.parentId  " +
	 				    			" INNER JOIN ( SELECT ID,MINGZI,parentId FROM subnet_ems  ) c ON b.ID = c.parentId " +
	 				    			" INNER JOIN CHANGZHAN D ON D.SUBNETID = C.ID  where 1=1 ");
	 				    	if(regionType==RegionType.village.getValue())
	 					    	sb.append(" and D.id="+regionId);//区域ID为村的所有电站
	 				    	else if(regionType==RegionType.town.getValue())
	 					    	sb.append(" and  C.id="+regionId);//区域ID为乡镇的所有电站
	 				    	else if(regionType==RegionType.county.getValue())
	 					    	sb.append("  and B.id="+regionId);//区域ID为区县的所有电站
	 				    	else if(regionType==RegionType.city.getValue())
	 					    	sb.append("  and A.id="+regionId);//区域ID为市的所有电站
	 				    	else 
	 				    		throw new Exception("请选择具体的区域类型,当前区域类型为" +  regionType);	
	 				    	sb.append(" ) "); 	 	  
	 		    		}
	 		    	}
	 		    	sb.append(	    			
	 		    			" and a.bujiancanshuid="+BuJianCanshu+" "+
	 		    			" and a.RIQI = '" + year + "-"+month+"-"+day+" 0:"+fen+"' " +
	 			    		" GROUP BY a.RIQI,a.BuJianLeiXingID,a.BuJianCanShuID  "
	 			    		//end >2-2
	 			    		);
	 		    	
	 	     }else{
	 	    	sb.append(
		    			
	 			    	" SELECT * FROM " +
	 			    	
	 			    		//1<表名为c ，用来进行排序
	 			    		"( " + //begin first select 1 
	 			    		
	 			    		//2<表名为 b 	获取所有历史数据， 		
	 			    		"SELECT * FROM  " +  
	 			    			" (" +  //begin second select 2
	 			    			
	 			    			//2-1<.获取历史日期（不包括当前日期）的电度数据	  a为历史数据表  			
	 			    			"SELECT a.riqi,SUM(a.H"+hourStr+") AS powers from ls." +tableName+ " a  ");  
	 		
	 					//增加电站批次过滤
	 					if(batchType!=null && batchType!=0&&BuJianType==BJLX.changzhan.getValue())
	 						sb.append( " inner JOIN stationinfo b on a.BuJianID = b.ChangZhanID and b.batch =" + batchType);  
	 					
	 			    	sb.append(" where " +
	 			    				 " a.bujianleixingid= "+ BuJianType);
	 			    	
	 			    	//如果区域ID为县、区、乡镇，则设置具体的过滤条件
	 			    	if(regionType!=0 && regionType != RegionType.province.getValue())
	 			    	{
	 			    		if(regionType==RegionType.commdev.getValue())
	 			    		{
	 			    			sb.append(" and a.bujianid = "+regionId);
	 			    		}
	 			    		else
	 			    		{
	 					    	sb.append(" and a.BuJianID in " + // 选择所有的厂站ID
	 					    			" (SELECT D.ID  FROM ( SELECT ID,MINGZI,parentId FROM subnet_ems WHERE parentId = 0 ) A  " +
	 					    			" INNER JOIN ( SELECT ID,MINGZI,parentId FROM subnet_ems  ) B ON A.ID = B.parentId  " +
	 					    			" INNER JOIN ( SELECT ID,MINGZI,parentId FROM subnet_ems  ) c ON b.ID = c.parentId " +
	 					    			" INNER JOIN CHANGZHAN D ON D.SUBNETID = C.ID  where 1=1 ");
	 					    	if(regionType==RegionType.village.getValue())
	 						    	sb.append(" and D.id="+regionId);//区域ID为村的所有电站
	 					    	else if(regionType==RegionType.town.getValue())
	 						    	sb.append(" and  C.id="+regionId);//区域ID为乡镇的所有电站
	 					    	else if(regionType==RegionType.county.getValue())
	 						    	sb.append("  and B.id="+regionId);//区域ID为区县的所有电站
	 					    	else if(regionType==RegionType.city.getValue())
	 						    	sb.append("  and A.id="+regionId);//区域ID为市的所有电站
	 					    	else 
	 					    		throw new Exception("请选择具体的区域类型,当前区域类型为" +  regionType);		
	 					    	sb.append(" ) "); 
	 			    		}
	 			    	}
	 		
	 			    	
	 			    	sb.append(
	 			    			
	 			    				" and a.bujiancanshuid="+ BuJianCanshu+" "+
	 			    				
	 			    				//" and a.RIQI LIKE '%00:55:00%' "+	    				
	 			    				//" and a.RIQI != '" + year + "-"+month+"-"+day+" 0:55:00' " +
	 			    				
	 			    				" and a.RIQI = '" +variableDate.getYear() + "-"+variableDate.getMonth()+"-"+variableDate.getDayOfMonth()+" 0:55:00' " +
	 			    				//" and a.RIQI <= '" + endDate.getYear() + "-"+endDate.getMonth()+"-"+endDate.getDayOfMonth()+" 0:55:00' " +
	 		
	 			    			"GROUP BY a.RIQI,a.BuJianLeiXingID,a.BuJianCanShuID  ");
	 			    			
	 			    			//end >2-1
	 			    	 sb.append(				
	 	 		 		    	" ) " +	//>end first select 1		    			
	 	 		 		    	" b ORDER BY b.riqi DESC  " + 
	 	 		 		    	
	 	 		 		    " ) " +//>end second select 2
	 	 		 		    " c ORDER BY c.riqi"); 
	 	     }
	 	     
	 	   
//	    	 sb.append(				
//	 		    	" ) " +	//>end first select 1		    			
//	 		    	" b ORDER BY b.riqi DESC  " + 
//	 		    	
//	 		    " ) " +//>end second select 2
//	 		    " c ORDER BY c.riqi"); 
	    	
		  
	    	System.out.println(sb.toString());
	    	try{//没数据直接返回null
		    	result =  jdbcTemplateHis.queryForObject(sb.toString(), new RowMapper<HisDataTimeAndValue>(){
	
					@Override
					public HisDataTimeAndValue mapRow(ResultSet rs, int index)
							throws SQLException {
						 HisDataTimeAndValue row = new HisDataTimeAndValue(); 
					    Date date=rs.getTimestamp(1);
	                    row.setTmptime(date.toString());
	                    float value=rs.getFloat(2);
	                    row.setValue(Float.parseFloat(MathUtil.formatDoubleNonExt(value, 0))); //修改小数点保留三位
						return row;
					}
		    		
		    	});
	    	}catch(EmptyResultDataAccessException e){
	    		return null;
	    	}
		   
	    }catch(Exception e){
    		e.printStackTrace();
    	}
	    return result;
	}
	
	/**
	 * 获取电站多个部件参数的历史曲线  遥测量 	 *
	 * @Date 2017.01.19 add by liuxf 
	 * @param listParam 部件参数列表
	 * @param reportDate 报告时间
	 * @return
	 */
	public List<HisDataTimeAndValue> GetStationHisDataMulti(
			List<BuJianParam> listParam,Date reportDate) {		
		  
		List<HisDataTimeAndValue> result  = new ArrayList<HisDataTimeAndValue>();
	    try{
	    	for(int i=0;i<listParam.size();i++)
	    	{	    		BuJianParam bjParam=listParam.get(i);
	    		result.addAll(GetStationHisData(Integer.parseInt(bjParam.getBjlx()),
	    				Integer.parseInt(bjParam.getBjid()),Integer.parseInt(bjParam.getBjcs()), reportDate) );
	    	}	
	    }catch(Exception e){
    		e.printStackTrace();
    	}	    
	    return result;
	}
	
	/**
	 * 获取电站历史曲线  遥测量 
	 * @param buJianType
	 * @param buJianId
	 * @param buJianCanshu
	 * @param reportDate
	 * @return
	 */
	public List<HisDataTimeAndValue> GetStationHisData(int buJianType,int buJianId,int buJianCanshu,Date reportDate) {

		Calendar dateParam = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		dateParam.setTime(reportDate);
		int today_year = dateParam.get(Calendar.YEAR);
		int today_month = dateParam.get(Calendar.MONTH) + 1;
		int today_day = dateParam.get(Calendar.DAY_OF_MONTH);
		int today_minute = dateParam.get(Calendar.MINUTE); 
		String year = String.valueOf(today_year);
	    String month = String.valueOf(today_month);
	    String day = String.valueOf(today_day); 
	    
		today_minute -= today_minute%5; 
	    String fen = String.valueOf(today_minute)+":00";
	    if(today_minute<10){
	    	fen="0"+today_minute+":00";
	    }
	    			
	    String tableName="";
	    if(today_month<10)
	    	month = "0"+month;
    	tableName= "YCDATA" + year + month;
	    List<HisDataTimeAndValue> result  = new ArrayList<HisDataTimeAndValue>();
	    try{
	    	StringBuffer sb= new StringBuffer();
	    	sb.append(" select riqi,h0,h1,h2,h3,h4,h5,h6,h7,h8,h9,h10,h11,h12,h13,h14,h15,h16,h17,h18,h19,h20,h21,h22,h23 ");
	    	sb.append(" from ls."+tableName+" a ");
	    	sb.append(" where a.bujianleixingid = 20");
		    sb.append(" and riqi = '" + year + "-"+month+"-"+day+" 0:55:00' and BuJianID in (select ID as BuJianID from ms.xunidmnliang where DYBJType = "+buJianType);
	    	if(buJianId!=0)
	    		sb.append(" and DYBJID = "+buJianId+" ");
	    	sb.append(" and DYBJParam ="+buJianCanshu+" ) "); 
		    System.out.println(sb.toString());
		    result = jdbcTemplateHis.query(sb.toString(), new ResultSetExtractor<List<HisDataTimeAndValue>>() { 
		    public List<HisDataTimeAndValue> extractData(ResultSet rs)  throws SQLException, DataAccessException {  
				 List<HisDataTimeAndValue> result = new ArrayList<HisDataTimeAndValue>();  
				 while(rs.next()) {  
				    HisDataTimeAndValue row = new HisDataTimeAndValue(); 
				    Date date=rs.getTimestamp(1);
                    row.setTmptime(date.toString()); 
                    List<Float> list = new ArrayList<Float>();
                    for(int i = 0;i<=23;i++)
                    {
                    	list.add(Float.parseFloat(MathUtil.formatDoubleNonExt(rs.getFloat(i+2),2)));
                    }
                    row.setListValue(list);
                    result.add(row);  
				 }  
				 return result;  
		     }});
		    
	    }catch(Exception e){
    		e.printStackTrace();
    	}
	    return result;
	}
	
	/***
	 * 获取不同区域类型三段式的数据
	 * @param regionType  区域类型
	 * @param regionId	  区域ID
	 * @param buJianType 部件类型
	 * @param buJianCanshu 部件参数 
	 * @param infoType 信息类型
	 * @param batchType 批次 如果传0 则全部
	 * @return
	 */
	public String getRegionPowerData(int regionType,int regionId,int buJianType,int buJianCanshu,int infoType,Integer batchType, Date dayTime) {

		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis()-minuteParam); //获取前minuteParam数据 
		if(dayTime != null){
			today.setTimeInMillis(dayTime.getTime());
		}
		int today_year = today.get(Calendar.YEAR);
		int today_month = today.get(Calendar.MONTH) + 1;
		int today_day = today.get(Calendar.DAY_OF_MONTH);
		int today_hour = today.get(Calendar.HOUR_OF_DAY);
	    if(infoType==InfoType.yaoce.getValue()) 
	    	today_hour--;
		if(today.get(Calendar.HOUR_OF_DAY)==0 && buJianCanshu!=ChangZhanParam.daypowers.getValue())
		{
			today_day-=1;
			today_hour = 23;
		}
		else if(today.get(Calendar.HOUR_OF_DAY)==0 && buJianCanshu==ChangZhanParam.daypowers.getValue())
		{
			today_hour = 0;
		}
		int today_minute = today.get(Calendar.MINUTE);
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
	    List<HisDataTimeAndValue> result  = new ArrayList<HisDataTimeAndValue>();
	    String tableName = "";
	  
	    if(infoType==InfoType.yaoce.getValue())
	    {
	    	tableName = "ycdata" + year + month;
		    try{
		    	StringBuffer sb= new StringBuffer();
		    	sb.append("select riqi,SUM(H"+xiaoshi+") AS H"+xiaoshi);
		    	sb.append(" from ls."+tableName+" a ");
		    	sb.append("where a.bujianleixingid=20 ");
			    sb.append("and riqi = '" + year + "-"+month+"-"+day+" 0:"+fen+"' AND BuJianID in (select ID as BuJianID from ms.xunidmnliang where DYBJType = "+buJianType);

		    	if(regionType!=0 && regionType != RegionType.province.getValue() && regionType != RegionType.city.getValue())
		    	{
			    	sb.append(" and a.changzhanid in (select a.ID as changzhanid  from changzhan a inner join subnet_ems b on a.SUBNETID = b.id inner join  dianwang c on b.dianwangid = c.id where 1=1 ");
			    	if(regionType==RegionType.village.getValue())
				    	sb.append(" and a.id="+regionId);
			    	else if(regionType==RegionType.town.getValue())
				    	sb.append(" and  b.id="+regionId);
			    	else if(regionType==RegionType.county.getValue())
				    	sb.append("  and c.id="+regionId);
			    	sb.append(" )");
		    	}
		    	sb.append(" and DYBJParam ="+buJianCanshu+" ) GROUP BY RiQi "); 
			    System.out.println(sb.toString());
			    result = jdbcTemplateHis.query(sb.toString(), new ResultSetExtractor<List<HisDataTimeAndValue>>() { 
			    public List<HisDataTimeAndValue> extractData(ResultSet rs)  throws SQLException, DataAccessException {  
					 List<HisDataTimeAndValue> result = new ArrayList<HisDataTimeAndValue>();  
					 while(rs.next()) {  
					    HisDataTimeAndValue row = new HisDataTimeAndValue(); 
//					    Date date=rs.getTimestamp(1);
//	                    row.setTmptime(date.toString());
	                    float value=rs.getFloat(2);
	                    row.setValue(Float.parseFloat(MathUtil.formatDoubleNonExt(value, 2))); //修改小数点保留三位
	                    result.add(row);  
					 }  
					 return result;  
			     }});
		    }catch(Exception e){
	    		e.printStackTrace();
	    	}
	    }
	    else if(infoType==InfoType.diandu.getValue())
	    {
	    	tableName = "kwhdata" + year + month;
		    try{
			    StringBuffer sb= new StringBuffer();
		    	if(buJianCanshu==ChangZhanParam.yearpowers.getValue())
		    	{
		    		//通过汇总月 得出 年电量
			    	sb.append("select SUM(H"+xiaoshi+") AS H"+xiaoshi+",'"+year + "-"+month+"-"+day+" 0:"+fen+"' as riqi from (");
		    		//获取本月份 
			    	sb.append("select riqi,SUM(H"+xiaoshi+") AS H"+xiaoshi);
			    	sb.append(" from ls."+tableName+" a ");
					//增加电站批次过滤
					if(batchType!=null && batchType!=0&&buJianType==BJLX.changzhan.getValue())
						sb.append( " inner JOIN stationinfo b on a.BuJianID = b.ChangZhanID and b.batch =" + batchType);  
					
			    	sb.append(" where a.bujianleixingid="+buJianType+" ");
			    	if(regionType!=0 && regionType != RegionType.province.getValue())
			    	{
				    	sb.append(" and a.changzhanid in " +
				    			" (SELECT D.ID  FROM ( SELECT ID,MINGZI,parentId FROM subnet_ems WHERE parentId = 0 ) A  " +
				    			" INNER JOIN ( SELECT ID,MINGZI,parentId FROM subnet_ems  ) B ON A.ID = B.parentId  " +
				    			" INNER JOIN ( SELECT ID,MINGZI,parentId FROM subnet_ems  ) c ON b.ID = c.parentId " +
				    			" INNER JOIN CHANGZHAN D ON D.SUBNETID = C.ID  where 1=1 ");
				    	if(regionType==RegionType.village.getValue())
					    	sb.append(" and D.id="+regionId);
				    	else if(regionType==RegionType.town.getValue())
					    	sb.append(" and  C.id="+regionId);
				    	else if(regionType==RegionType.county.getValue())
					    	sb.append("  and B.id="+regionId);
				    	else if(regionType==RegionType.city.getValue())
					    	sb.append("  and A.id="+regionId);
				    	sb.append(" )");
			    	}
			    	if(regionType == RegionType.commdev.getValue()){
			    		sb.append(" and a.bujianid = "+regionId);
			    	}
			    	sb.append("and a.bujiancanshuid="+ChangZhanParam.monthpowers.getValue()+" ");
				    sb.append("and riqi = '" + year + "-"+month+"-"+day+" 0:"+fen+"' GROUP BY RiQi");
				    
				    // 其他月份的数据 
		    		for(int i=1;i<today_month;i++) 
		    		{
		    			//不能小于系统上线时间
		    			if(today_year <= 2016 && i<=9)
		    				continue;
		    			//获取日
		    			String DatStr = String.valueOf(getMonthDays(today_year,i));
		    			//获取月份
		    			String monthStr = String.valueOf(i);
		    		    if(i<10)
		    		    	monthStr = "0"+i;
				    	sb.append(" union all select riqi,SUM(H"+hourStr+") AS H"+xiaoshi);
				    	sb.append(" from ls.kwhdata" + year+monthStr+" a ");
				    	
						//增加电站批次过滤
						if(batchType!=null && batchType!=0&&buJianType==BJLX.changzhan.getValue())
							sb.append( " inner JOIN stationinfo b on a.BuJianID = b.ChangZhanID and b.batch =" + batchType);  
						
				    	sb.append(" where a.bujianleixingid="+buJianType+" ");
				    	if(regionType!=0 && regionType != RegionType.province.getValue())
				    	{

					    	sb.append(" and a.changzhanid in " +
					    			" (SELECT D.ID  FROM ( SELECT ID,MINGZI,parentId FROM subnet_ems WHERE parentId = 0 ) A  " +
					    			" INNER JOIN ( SELECT ID,MINGZI,parentId FROM subnet_ems  ) B ON A.ID = B.parentId  " +
					    			" INNER JOIN ( SELECT ID,MINGZI,parentId FROM subnet_ems  ) c ON b.ID = c.parentId " +
					    			" INNER JOIN CHANGZHAN D ON D.SUBNETID = C.ID  where 1=1 ");
					    	if(regionType==RegionType.village.getValue())
						    	sb.append(" and D.id="+regionId);
					    	else if(regionType==RegionType.town.getValue())
						    	sb.append(" and  C.id="+regionId);
					    	else if(regionType==RegionType.county.getValue())
						    	sb.append("  and B.id="+regionId);
					    	else if(regionType==RegionType.city.getValue())
						    	sb.append("  and A.id="+regionId);
					    	sb.append(" )");
				    	}
				    	if(regionType == RegionType.commdev.getValue()){
				    		sb.append(" and a.bujianid = "+regionId);
				    	}
				    	sb.append("and a.bujiancanshuid="+ChangZhanParam.monthpowers.getValue()+" ");
					    sb.append("and riqi = '" + year + "-"+monthStr+"-"+DatStr+" 0:55:00' GROUP BY RiQi");
		    		}
			    	sb.append(" ) a ");
		    	}else if(buJianCanshu==ChangZhanParam.yearOnlinePower.getValue()){//年上网电量

		    		//通过汇总月 得出 年电量
			    	sb.append("select SUM(H"+xiaoshi+") AS H"+xiaoshi+",'"+year + "-"+month+"-"+day+" 0:"+fen+"' as riqi from (");
		    		//获取本月份 
			    	sb.append("select riqi,SUM(H"+xiaoshi+") AS H"+xiaoshi);
			    	sb.append(" from ls."+tableName+" a ");
					//增加电站批次过滤
					if(batchType!=null && batchType!=0&&buJianType==BJLX.changzhan.getValue())
						sb.append( " inner JOIN stationinfo b on a.BuJianID = b.ChangZhanID and b.batch =" + batchType);  
					
			    	sb.append(" where a.bujianleixingid="+buJianType+" ");
			    	if(regionType!=0 && regionType != RegionType.province.getValue())
			    	{
				    	sb.append(" and a.changzhanid in " +
				    			" (SELECT D.ID  FROM ( SELECT ID,MINGZI,parentId FROM subnet_ems WHERE parentId = 0 ) A  " +
				    			" INNER JOIN ( SELECT ID,MINGZI,parentId FROM subnet_ems  ) B ON A.ID = B.parentId  " +
				    			" INNER JOIN ( SELECT ID,MINGZI,parentId FROM subnet_ems  ) c ON b.ID = c.parentId " +
				    			" INNER JOIN CHANGZHAN D ON D.SUBNETID = C.ID  where 1=1 ");
				    	if(regionType==RegionType.village.getValue())
					    	sb.append(" and D.id="+regionId);
				    	else if(regionType==RegionType.town.getValue())
					    	sb.append(" and  C.id="+regionId);
				    	else if(regionType==RegionType.county.getValue())
					    	sb.append("  and B.id="+regionId);
				    	else if(regionType==RegionType.city.getValue())
					    	sb.append("  and A.id="+regionId);
				    	sb.append(" )");
			    	}
			    	if(regionType == RegionType.commdev.getValue()){
			    		sb.append(" and a.bujianid = "+regionId);
			    	}
			    	sb.append("and a.bujiancanshuid="+ChangZhanParam.monthOnlinePower.getValue()+" ");
				    sb.append("and riqi = '" + year + "-"+month+"-"+day+" 0:"+fen+"' GROUP BY RiQi");
				    
				    // 其他月份的数据 
		    		for(int i=1;i<today_month;i++) 
		    		{
		    			//不能小于系统上线时间
		    			if(today_year <= 2016 && i<=9)
		    				continue;
		    			//获取日
		    			String DatStr = String.valueOf(getMonthDays(today_year,i));
		    			//获取月份
		    			String monthStr = String.valueOf(i);
		    		    if(i<10)
		    		    	monthStr = "0"+i;
				    	sb.append(" union all select riqi,SUM(H"+hourStr+") AS H"+xiaoshi);
				    	sb.append(" from ls.kwhdata" + year+monthStr+" a ");
				    	
						//增加电站批次过滤
						if(batchType!=null && batchType!=0&&buJianType==BJLX.changzhan.getValue())
							sb.append( " inner JOIN stationinfo b on a.BuJianID = b.ChangZhanID and b.batch =" + batchType);  
						
				    	sb.append(" where a.bujianleixingid="+buJianType+" ");
				    	if(regionType!=0 && regionType != RegionType.province.getValue())
				    	{

					    	sb.append(" and a.changzhanid in " +
					    			" (SELECT D.ID  FROM ( SELECT ID,MINGZI,parentId FROM subnet_ems WHERE parentId = 0 ) A  " +
					    			" INNER JOIN ( SELECT ID,MINGZI,parentId FROM subnet_ems  ) B ON A.ID = B.parentId  " +
					    			" INNER JOIN ( SELECT ID,MINGZI,parentId FROM subnet_ems  ) c ON b.ID = c.parentId " +
					    			" INNER JOIN CHANGZHAN D ON D.SUBNETID = C.ID  where 1=1 ");
					    	if(regionType==RegionType.village.getValue())
						    	sb.append(" and D.id="+regionId);
					    	else if(regionType==RegionType.town.getValue())
						    	sb.append(" and  C.id="+regionId);
					    	else if(regionType==RegionType.county.getValue())
						    	sb.append("  and B.id="+regionId);
					    	else if(regionType==RegionType.city.getValue())
						    	sb.append("  and A.id="+regionId);
					    	sb.append(" )");
				    	}
				    	if(regionType == RegionType.commdev.getValue()){
				    		sb.append(" and a.bujianid = "+regionId);
				    	}
				    	sb.append("and a.bujiancanshuid="+ChangZhanParam.monthOnlinePower.getValue()+" ");
					    sb.append("and riqi = '" + year + "-"+monthStr+"-"+DatStr+" 0:55:00' GROUP BY RiQi");
		    		}
			    	sb.append(" ) a ");
		    	
		    	}else 
		    	{
			    	sb.append("select SUM(H"+xiaoshi+") AS H"+xiaoshi+",riqi");
			    	sb.append(" from ls."+tableName+" a ");
					//增加电站批次过滤
					if(batchType!=null && batchType!=0&&buJianType==BJLX.changzhan.getValue())
						sb.append( " inner JOIN stationinfo b on a.BuJianID = b.ChangZhanID and b.batch =" + batchType);  
					
			    	sb.append(" where a.bujianleixingid="+buJianType+" ");
			    	if(regionType!=0 && regionType != RegionType.province.getValue())
			    	{
				    	sb.append(" and a.changzhanid in " +
				    			" (SELECT D.ID  FROM ( SELECT ID,MINGZI,parentId FROM subnet_ems WHERE parentId = 0 ) A  " +
				    			" INNER JOIN ( SELECT ID,MINGZI,parentId FROM subnet_ems  ) B ON A.ID = B.parentId  " +
				    			" INNER JOIN ( SELECT ID,MINGZI,parentId FROM subnet_ems  ) c ON b.ID = c.parentId " +
				    			" INNER JOIN CHANGZHAN D ON D.SUBNETID = C.ID  where 1=1 ");
				    	if(regionType==RegionType.village.getValue())
					    	sb.append(" and D.id="+regionId);
				    	else if(regionType==RegionType.town.getValue())
					    	sb.append(" and  C.id="+regionId);
				    	else if(regionType==RegionType.county.getValue())
					    	sb.append("  and B.id="+regionId);
				    	else if(regionType==RegionType.city.getValue())
					    	sb.append("  and A.id="+regionId);
				    	sb.append(" )");
			    	}
			    	
			    	if(regionType == RegionType.commdev.getValue()){
			    		sb.append(" and a.bujianid = "+regionId);
			    	}
			    	
			    	sb.append(" and a.bujiancanshuid="+buJianCanshu+" ");
				    sb.append(" and riqi = '" + year + "-"+month+"-"+day+" 0:"+fen+"' GROUP BY RiQi");
		    	}
			    System.out.println(sb.toString());
			    result = jdbcTemplateHis.query(sb.toString(), new ResultSetExtractor<List<HisDataTimeAndValue>>() { 
			    public List<HisDataTimeAndValue> extractData(ResultSet rs)  throws SQLException, DataAccessException {  
					 List<HisDataTimeAndValue> result = new ArrayList<HisDataTimeAndValue>();  
					 while(rs.next()) {  
					    HisDataTimeAndValue row = new HisDataTimeAndValue(); 
	                    double value=rs.getDouble(1);
//					    Date date=rs.getTimestamp(2);
//	                    row.setTmptime(date.toString());
	                    row.setValue(Float.parseFloat(MathUtil.formatDoubleNonExt(value, 0))); //修改小数点保留三位
	                    result.add(row);  
					 }
					 return result;  
			     }});
		    }catch(Exception e){
	    		e.printStackTrace();
	    	}
	    }
	    else if(infoType==InfoType.yaoxin.getValue())
	    	tableName = "yxhour" + year + month;
	    
	    if(result.size()==0){
	    	return "0";
	    }else{
		    return String.valueOf(result.get(0).getValue());	    
	    }
	}
	
	/**
	 * 根据年月获取天数
	 * @param year
	 * @param month
	 * @return
	 */
	public int getMonthDays(int year,int month)
	{

		int days = 0;
		switch (month) {
        case 1:
        	days = 31;
            break;
        case 3:
        	days = 31;
            break;
        case 5:
        	days = 31;
            break;
        case 7:
        	days = 31;
            break;
        case 8:
        	days = 31;
            break;
        case 10:
        	days = 31;
            break;
        case 12:
        	days = 31; 
            break;
        case 2:
            if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
            	days = 29; 
            } 
            else 
            {
            	days = 28;
            }
            break;
        default:
        	days = 30;
            break;
        }
		return days;
	}

	/**
	 * 根据获取计算统计数据
	 * 
	 * @param bujianleixingid  
	 * @param bujianid 
	 * @param bujiancanshu 
	 * @param datatime
	 */
	public List<HisDataTimeAndValue> getHisDataTimeAndValue(int bujianleixingid,int bujianid, int bujiancanshu, Date datatime){
		Calendar date = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		date.setTimeInMillis(Calendar.getInstance().getTimeInMillis()-minuteParam); //鑾峰彇鍓峬inuteParam鏁版嵁 
		if(datatime != null){
			date.setTimeInMillis(datatime.getTime());
		}
		int today_year = date.get(Calendar.YEAR);
		int today_month = date.get(Calendar.MONTH) + 1;
		int today_day = date.get(Calendar.DAY_OF_MONTH);
		String year = String.valueOf(today_year);
		String month = String.valueOf(today_month);
	    if(today_month<10)
	    	month = "0"+month;
	    String day = String.valueOf(today_day);
		
		StringBuffer sb = new StringBuffer();
		sb.append(" select bujianid,hourpower,datatime,value,bujiancanshuid from ls.energydata" + year + month +" where bujianleixingid = "+bujianleixingid 
					+" and bujianid="+ bujianid);
		sb.append(" and bujiancanshuid = " + bujiancanshu);
		sb.append(" and datatime = '" + year +"-"+month+"-" +day +" 21:00:00' ");
	
		System.out.println(sb);
		
	    List<HisDataTimeAndValue> result = jdbcTemplateHis.query(sb.toString(), new ResultSetExtractor<List<HisDataTimeAndValue>>() { 
	    public List<HisDataTimeAndValue> extractData(ResultSet rs)  throws SQLException, DataAccessException {  
			 List<HisDataTimeAndValue> list = new ArrayList<HisDataTimeAndValue>();  
			 while(rs.next()) {  
			    HisDataTimeAndValue row = new HisDataTimeAndValue(); 
	//		    Date date=rs.getTimestamp(1);
	//         row.setTmptime(date.toString());
	         float value=rs.getFloat(4);
	         row.setValue(Float.parseFloat(MathUtil.formatDoubleNonExt(value, 3))); //修改小数点保留三位
	         list.add(row);  
			 }  
			 return list;  
	     }});
	    return result;
	}
	
	/**
	 * 获取逆变器组串的电流电压值
	 * @param buJianType
	 * @param bujiancanshu
	 * @param bujianid
	 * @param reportDate
	 * @return
	 */
	public List<HisDataTimeAndValue> getGroupStringData(int buJianType,int bujiancanshu,int bujianid, Date reportDate) {

		Calendar dateParam = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		dateParam.setTime(reportDate);
		int today_year = dateParam.get(Calendar.YEAR);
		int today_month = dateParam.get(Calendar.MONTH) + 1;
		int today_day = dateParam.get(Calendar.DAY_OF_MONTH);
		
		String year = String.valueOf(today_year);
	    String month = String.valueOf(today_month);
	    
	    String day = String.valueOf(today_day); 
	    if(today_day < 10) {
	    	day = "0"+today_day;
	    }
	    if(today_month<10)
	    	month = "0"+month;
	    String riqi = "";
	    for(String item : minuteList) {
	    	riqi += "riqi = '" + year + "-" + month + "-" + day + item + "' or ";
	    }
	    riqi = riqi.substring(0, riqi.length() - 3);
	    
	    String tableName="";
	   
    	tableName= "YCDATA" + year + month;
	    List<HisDataTimeAndValue> result  = new ArrayList<HisDataTimeAndValue>();
	    try{
	    	StringBuffer sb= new StringBuffer();
	    	sb.append(" select riqi,h0,h1,h2,h3,h4,h5,h6,h7,h8,h9,h10,h11,h12,h13,h14,h15,h16,h17,h18,h19,h20,h21,h22,h23 ");
	    	sb.append(" from ls."+tableName+" a ");
	    	sb.append(" where a.bujianleixingid = 20");
		    sb.append(" and ("+riqi+") and BuJianID in (select ID as BuJianID from ms.xunidmnliang where DYBJType = "+buJianType);
	    	
	    	sb.append(" and DYBJID = "+bujianid+" ");
	    	sb.append(" and DYBJParam ="+bujiancanshu+" ) "); 
	    	System.out.println(sb);
		    result = jdbcTemplateHis.query(sb.toString(), new ResultSetExtractor<List<HisDataTimeAndValue>>() { 
		    public List<HisDataTimeAndValue> extractData(ResultSet rs)  throws SQLException, DataAccessException {  
				 List<HisDataTimeAndValue> result = new ArrayList<HisDataTimeAndValue>();  
				 while(rs.next()) {  
				    HisDataTimeAndValue row = new HisDataTimeAndValue(); 
				    List<Float> valueList = new ArrayList<Float>();
				    Date date=rs.getTimestamp(1);
                    row.setTmptime(date.toString()); 
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
	    return result;
	}
	
	/**
	 * 生成每五分钟间隔的list
	 * mxf
	 * @return
	 */
	private static List<String> genMinuteArr() {
		List<String> list = new ArrayList<String>();
		for(int i=0;i<60;i=i+5) {
			
			if(i < 10) {
				list.add(" 00:0"+ i + ":00");
			} else {
				list.add(" 00:" + i + ":00");
			}
		}
		return list;
	}
	
	
//	public static void main(String[] args) {
//		String arg = "salfagj or ";
//		arg = arg.substring(0, arg.length() -3);
//		System.out.println(arg);
//	}

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

		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis()-minuteParam); //获取前minuteParam数据 
		if(date != null){
			today.setTimeInMillis(date.getTimeInMillis());
		}
		int today_year = today.get(Calendar.YEAR);
		int today_month = today.get(Calendar.MONTH) + 1;
		int today_day = today.get(Calendar.DAY_OF_MONTH);
		int today_hour = today.get(Calendar.HOUR_OF_DAY);
		int today_minute = today.get(Calendar.MINUTE);
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
	    List<YCData> result  = new ArrayList<YCData>();
	    String tableName = "";
    	StringBuffer sb= new StringBuffer();
	    if(infoType==1)
	    {
	    	tableName = "ycdata" + year + month;
	    	sb.append("select a.riqi,a.H"+xiaoshi+" as datavalue ");
	    	sb.append(" from ls."+tableName+" a  where "
	    			+ " a.BuJianLeiXingID = "+buJianType + " "
	    	    	+ " and a.BuJianID = "+buJianID + " "
	    	    	+ " and a.BuJianCanShuID = "+buJianCanshu + " ");
		    sb.append(" and a.riqi = '" + year + "-"+month+"-"+day+" 00:"+fen+"'  and 1=1 ");
	    }else if(infoType==2)
  	    {
  	    	tableName = "kwhdata" + year + month;
  	    	sb.append("select a.riqi,a.H"+xiaoshi+" as datavalue ");
  	    	sb.append(" from ls."+tableName+" a where "
  	    			+ " a.BuJianLeiXingID = "+buJianType + " "
  	    	    	+ " and a.BuJianID = "+buJianID + " "
  	    	    	+ " and a.BuJianCanShuID = "+buJianCanshu + " ");
  		    sb.append(" and a.riqi = '" + year + "-"+month+"-"+day+" 00:"+fen+"'  and 1=1  ");
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
	    System.out.println(sb.toString());
 
	    try{
		    result = jdbcTemplateHis.query(sb.toString(), new ResultSetExtractor<List<YCData>>() { 
		    public List<YCData> extractData(ResultSet rs)  throws SQLException, DataAccessException {  
				 List<YCData> result = new ArrayList<YCData>();  
				 while(rs.next()) {  
					 YCData row = new YCData(); 
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
	
	/***
	 * 获取最新缓存文件中的遥信数据
	 * 构建两个三段式Map，由这两个三段式Map返回三段式对应Map(三段式：value)
	 * 1.虚拟状态量Id：三段式
	 * 2.虚拟状态量Id：value
	 * 组合成三段式:value 返回
	 * timeLineType 1 当前时间点 2上一个时间点
	 * @param 
	 * @param
	 * @param
	 */
	public Map<String,Double> getYxMapData4Strategy(int dyBujianType, int dyBujianID, int dyBujianParam, int timeLineType)
	{
		//构建第一个三段式Map
		StringBuilder sql = new StringBuilder();
		Map<String,String> configMapOfYx = new HashMap<String,String>();
		sql.append("select id as xuniId, DYBJType as buJianType, DYBJID as buJianId, DYBJParam as buJianCanShu from xunizhtliang where 1=1");
		if(dyBujianType!=0)
			sql.append(" and DYBJType = " + dyBujianType);
		if(dyBujianID!=0)
			sql.append(" and DYBJID = " + dyBujianID);
		if(dyBujianParam!=0)
			sql.append(" and DYBJType = " + dyBujianParam);
		configMapOfYx = jdbcTemplateHis.query(sql.toString(), new ResultSetExtractor<Map<String,String>>() { 
		    public Map<String,String> extractData(ResultSet rs)  throws SQLException, DataAccessException {
		    	Map<String,String> configMapOfYx = new HashMap<String,String>();
				 while(rs.next()) {
					 String xuniId = rs.getString("xuniId");
					 String buJianType = rs.getString("buJianType");
					 String buJianId = rs.getString("buJianId");
					 String buJianCanShu = rs.getString("buJianCanShu");
					 String configRealParam =buJianType + "-" +buJianId + "-" + buJianCanShu;
					 configMapOfYx.put(xuniId,configRealParam);
				 }   
				 return configMapOfYx;  
		     }});
		//构建第二个三段式Map
		Map<String,Double> fileMapOfYx = new HashMap<String,Double>();
		
		int buJianType = 0; // 部件类型
		int cSType = 0; // 参数类型
		int buJianID = 0; // 部件ID
		int changZhanID = 0;
		String buJianTypeStr = "";
		String cSTypeStr = "";
		String buJianIDStr = "";
		String time = "";
		String timeTmp="";
		

		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		if(timeLineType == 1) {
			today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		}else {//如果不为1.取前五分钟，即上一个点
			today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		}
		
		int today_year = today.get(Calendar.YEAR);
		int today_month = today.get(Calendar.MONTH) + 1;
		int today_day = today.get(Calendar.DAY_OF_MONTH);
		int today_hour = today.get(Calendar.HOUR_OF_DAY);
		int today_minute = today.get(Calendar.MINUTE);
		int index = today_minute / 5 ; // 当前要采集数据点的索引
		
		time = Integer.toString(today_year);
		time = time + "-";
		    if (today_month < 10)
		    {
		      time = time + "0";
		      time = time + Integer.toString(today_month);
		    }
		    else {
		      time = time + Integer.toString(today_month);
		    }time = time + "-";
		    if (today_day < 10)
		    {
		      time = time + "0";
		      time = time + Integer.toString(today_day);
		    }
		    else {
		      time = time + Integer.toString(today_day);
		    }time = time + " ";
		    if (today_hour < 10) {
		      time = time + "0";
		      time = time + Integer.toString(today_hour);
		    } else {
		      time = time + Integer.toString(today_hour);
		    }
		    timeTmp = time + ":";
		    time = timeTmp + "00:00";
		    
		System.out.println("获取历史临时文件，当前时间是：" + String.valueOf(today_year) + "-"
				+ String.valueOf(today_month) + "-" + String.valueOf(today_day)
				+ " " + String.valueOf(today_hour));
		String strYear = Integer.toString(today_year);
		String strMonth = today_month < 10 ? ("0" + String.valueOf(today_month))
				: ("" + String.valueOf(today_month));
		String strDay = today_day < 10 ? ("0" + String.valueOf(today_day))
				: ("" + String.valueOf(today_day));
		String strHour = today_hour < 10 ? ("0" + String.valueOf(today_hour))
				: ("" + String.valueOf(today_hour));

		if(iesbase.equals("")) //没有配置环境变量
			iesbase = "";
		String strCacheFilePath = iesbase+"/tmp/data_"
		+ strYear + "_" + strMonth + strDay + File.separator + "yx5m_"
		+ strYear + "_" + strMonth + strDay + "_" + strHour + ".dat";
		
		try {
			FileInputStream fis = new FileInputStream(strCacheFilePath);
			System.out.println(strCacheFilePath);
			DataInputStream dis = new DataInputStream(fis);
			
			while (dis.available() != 0) {
				Double valueOfYx = 0.00;
				// 部件类型
				buJianType = dis.readByte();
				buJianTypeStr = Integer.toString(buJianType);

				// 部件参数
				cSType = dis.readByte();
				cSTypeStr = Integer.toString(cSType);

				// 部件ID
				buJianID = getInt(dis);
				buJianIDStr = Integer.toString(buJianID);
				changZhanID = getInt(dis);
				
				for (int i = 0; i <= 11; i++) {
					// 1个小时内的12个点
					byte bytes = dis.readByte();
					if(i==index && bytes !=0)
					{
						valueOfYx = 1.0;
					}

				}
				fileMapOfYx.put(buJianIDStr, valueOfYx);
				
			}
			fis.close();
			dis.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		
		//由上面获取到的两个Map进行hmi数据更新
		Map<String,Double> measureDataMapOfYx = new HashMap<String,Double>();
		Iterator it = configMapOfYx.entrySet().iterator();
		while (it.hasNext()) { 
			Map.Entry entry = (Map.Entry) it.next(); 
			String paramKey = (String) entry.getKey();
			measureDataMapOfYx.put(configMapOfYx.get(paramKey),fileMapOfYx.get(paramKey));			
		}
		return measureDataMapOfYx;
	}
	
	/***
	 * 获取最新缓存文件中的遥测数据
	 * 构建两个三段式Map，由这两个三段式Map返回三段式对应Map(三段式：value)
	 * 1.虚拟状态量Id：三段式
	 * 2.虚拟状态量Id：value
	 * 组合成三段式:value 返回
	 * timeLineType 1 当前时间点 2上一个时间点
	 * @param 
	 * @param
	 * @param
	 */
	public Map<String,Double> getYcMapData4Strategy(int dyBujianType, int dyBujianID, int dyBujianParam, int timeLineType)
	{
		//构建第一个三段式Map
		StringBuilder sql = new StringBuilder();
		Map<String,String> configMapOfYc = new HashMap<String,String>();
		sql.append("select id as xuniId, DYBJType as buJianType, DYBJID as buJianId, DYBJParam as buJianCanShu from xunidmnliang where 1=1");
		if(dyBujianType!=0)
			sql.append(" and DYBJType = " + dyBujianType);
		if(dyBujianID!=0)
			sql.append(" and DYBJID = " + dyBujianID);
		if(dyBujianParam!=0)
			sql.append(" and DYBJType = " + dyBujianParam);
		configMapOfYc = jdbcTemplateHis.query(sql.toString(), new ResultSetExtractor<Map<String,String>>() { 
		    public Map<String,String> extractData(ResultSet rs)  throws SQLException, DataAccessException {
		    	Map<String,String> configMapOfYc = new HashMap<String,String>();
				 while(rs.next()) {
					 String xuniId = rs.getString("xuniId");
					 String buJianType = rs.getString("buJianType");
					 String buJianId = rs.getString("buJianId");
					 String buJianCanShu = rs.getString("buJianCanShu");
					 String configRealParam =buJianType + "-" +buJianId + "-" + buJianCanShu;
					 configMapOfYc.put(xuniId,configRealParam);
				 }   
				 return configMapOfYc;  
		     }});
		//构建第二个三段式Map
		Map<String,Double> fileMapOfYc = new HashMap<String,Double>();
		int buJianType = 0; // 部件类型
		int cSType = 0; // 参数类型
		int buJianID = 0; // 部件ID
		int changZhanID = 0;
		String buJianTypeStr = "";
		String cSTypeStr = "";
		String buJianIDStr = "";
		String time = "";
		String timeTmp="";
		int tmp = 0;
		float f = 0;
		int s = 0;
		
		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		if(timeLineType == 1) {
			today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		}else {//如果不为1.取前五分钟，即上一个点
			today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		}
		int today_year = today.get(Calendar.YEAR);
		int today_month = today.get(Calendar.MONTH) + 1;
		int today_day = today.get(Calendar.DAY_OF_MONTH);
		int today_hour = today.get(Calendar.HOUR_OF_DAY);
		int today_minute = today.get(Calendar.MINUTE);
		int index = today_minute / 5 ; // 当前要采集数据点的索引
		
		time = Integer.toString(today_year);
		time = time + "-";
		    if (today_month < 10)
		    {
		      time = time + "0";
		      time = time + Integer.toString(today_month);
		    }
		    else {
		      time = time + Integer.toString(today_month);
		    }time = time + "-";
		    if (today_day < 10)
		    {
		      time = time + "0";
		      time = time + Integer.toString(today_day);
		    }
		    else {
		      time = time + Integer.toString(today_day);
		    }time = time + " ";
		    if (today_hour < 10) {
		      time = time + "0";
		      time = time + Integer.toString(today_hour);
		    } else {
		      time = time + Integer.toString(today_hour);
		    }
		    timeTmp = time + ":";
		    time = timeTmp + "00:00";
		    
		System.out.println("获取历史临时文件，当前时间是：" + String.valueOf(today_year) + "-"
				+ String.valueOf(today_month) + "-" + String.valueOf(today_day)
				+ " " + String.valueOf(today_hour));
		String strYear = Integer.toString(today_year);
		String strMonth = today_month < 10 ? ("0" + String.valueOf(today_month))
				: ("" + String.valueOf(today_month));
		String strDay = today_day < 10 ? ("0" + String.valueOf(today_day))
				: ("" + String.valueOf(today_day));
		String strHour = today_hour < 10 ? ("0" + String.valueOf(today_hour))
				: ("" + String.valueOf(today_hour));

		if(iesbase.equals("")) //没有配置环境变量
			iesbase = "";
		String strCacheFilePath = iesbase+"/tmp/data_"
		+ strYear + "_" + strMonth + strDay + File.separator + "yc5m_"
		+ strYear + "_" + strMonth + strDay + "_" + strHour + ".dat";
		
		try {
			FileInputStream fis = new FileInputStream(strCacheFilePath);
			System.out.println(strCacheFilePath);
			DataInputStream dis = new DataInputStream(fis);
			
			while (dis.available() != 0) {
				Double valueOfYc = 0.00;
				// 部件类型
				buJianType = dis.readByte();
				buJianTypeStr = Integer.toString(buJianType);

				// 部件参数
				cSType = dis.readByte();
				cSTypeStr = Integer.toString(cSType);

				// 部件ID
				buJianID = getInt(dis);
				buJianIDStr = Integer.toString(buJianID);
				changZhanID = getInt(dis);

				for (int i = 0; i <= 11; i++) {
					// 1个小时内的12个点
					byte[] bytes = new byte[4];
					bytes[0] = dis.readByte();
					bytes[1] = dis.readByte();
					bytes[2] = dis.readByte();
					bytes[3] = dis.readByte();

					tmp = (((bytes[3] << 24) & 0xff000000)
							| ((bytes[2] << 16) & 0xff0000)
							| ((bytes[1] << 8) & 0xff00) | (bytes[0] & 0xff));
					f = Float.intBitsToFloat(tmp);
					if(i==index)
					{
						valueOfYc = (double) f;
					}

				}
				fileMapOfYc.put(buJianIDStr, valueOfYc);
				f = dis.readFloat();
				s = dis.readByte();
				s = dis.readByte();
				s = dis.readByte();
				f = dis.readFloat();
				s = dis.readByte();
				s = dis.readByte();
				s = dis.readByte();
				
			}
			fis.close();
			dis.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		
		//由上面获取到的两个Map进行hmi数据更新
		Map<String, Double> measureDataMapOfYc = new HashMap<String, Double>();
		Iterator it = configMapOfYc.entrySet().iterator();
		while (it.hasNext()) { 
			Map.Entry entry = (Map.Entry) it.next(); 
			String paramKey = (String) entry.getKey();
			measureDataMapOfYc.put(configMapOfYc.get(paramKey),fileMapOfYc.get(paramKey));			
		}	
		return measureDataMapOfYc;
	}
	
	/***
	 *由bujianType获取系统中该部件类型的所有部件
	 * @param dyBujianType
	 * @return Map<bujianType,bujianID> configMap
	 * @param
	 */
	public List<Commdev> getBujian(int dyBujianType)
	{
		//构建第一个三段式Map
		StringBuilder sql = new StringBuilder();
		List<Commdev> bujianList = new ArrayList<Commdev>();
		if(dyBujianType != 6) {
			sql.append("SELECT a.BJLXID AS bujianType, a.ID AS bujianID, a.ChangZhanID AS changzhanID, b.MingZi AS changzhanName "
					+ "FROM commdev a LEFT JOIN changzhan b ON a.ChangZhanID = b.id WHERE 1=1 ");
			if(dyBujianType!=0)
				sql.append(" and BJLXID = " + dyBujianType);
			bujianList = jdbcTemplateHis.query(sql.toString(), new ResultSetExtractor<List<Commdev>>() { 
			    public List<Commdev> extractData(ResultSet rs)  throws SQLException, DataAccessException {
			    	List<Commdev> bujianListTemp = new ArrayList<Commdev>();
					 while(rs.next()) {
						 Commdev commdev = new Commdev();
						 commdev.setBJLXID(rs.getInt("bujianType"));
						 commdev.setID(rs.getInt("bujianID"));
						 commdev.setChangZhanID(rs.getInt("changzhanID"));
						 commdev.setMingZi(rs.getString("changzhanName"));
						 bujianListTemp.add(commdev);
					 }
					 return bujianListTemp;
			}});
		}else {
			sql.append("SELECT ID AS changzhanID, mingzi AS changzhanName FROM changzhan WHERE 1=1");
			bujianList = jdbcTemplateHis.query(sql.toString(), new ResultSetExtractor<List<Commdev>>() { 
			    public List<Commdev> extractData(ResultSet rs)  throws SQLException, DataAccessException {
			    	List<Commdev> bujianListTemp = new ArrayList<Commdev>();
					 while(rs.next()) {
						 Commdev commdev = new Commdev();
						 commdev.setBJLXID(6);
						 commdev.setID(rs.getInt("changzhanID"));
						 commdev.setChangZhanID(rs.getInt("changzhanID"));
						 commdev.setMingZi(rs.getString("changzhanName"));
						 bujianListTemp.add(commdev);
					 }
					 return bujianListTemp;
			}});
		}
		return bujianList;
	}
	
	
	/***
	 *由bujianType获取系统中该部件类型的所有部件
	 * @return Map<bujianType,bujianID> configMap
	 * @param
	 * @throws SQLException 
	 */
	public List<s_warn_strategy> getlistStrategy() throws SQLException,Exception
	{
		//构建第一个三段式Map
		StringBuilder sql = new StringBuilder();
		List<s_warn_strategy> listStrategy = new ArrayList<s_warn_strategy>();

		sql.append("SELECT config_ID,config_Name,config_Type,device_Type,device_ID,device_Param,"
				+ "chang_ZhanID,push_Strategy,calc_Interval,valid_flag  FROM mscq.warnpushconfig WHERE 1=1");
		listStrategy = jdbcTemplateHis.query(sql.toString(), new ResultSetExtractor<List<s_warn_strategy>>() { 
		    public List<s_warn_strategy> extractData(ResultSet rs)  throws SQLException, DataAccessException {
		    	List<s_warn_strategy> warnListTemp = new ArrayList<s_warn_strategy>();
				 while(rs.next()) {
					 s_warn_strategy warnStrategy = new s_warn_strategy();
					 warnStrategy.deviceType = rs.getByte("device_Type");
					 warnStrategy.strategyID = rs.getByte("config_ID");
					 warnStrategy.expression= rs.getString("push_Strategy");
					 warnStrategy.generateType = rs.getByte("config_Type");
					 warnListTemp.add(warnStrategy);
				 }
				 return warnListTemp;
		}});
		
		for(int i=0;i<  listStrategy.size();i++){
			s_warn_strategy item=listStrategy.get(i);
			
			item.addParamList(getlistStrategyParam(" config_id="+item.strategyID));
			
		}
		return listStrategy;
	}
	
	
	
	/// <summary>
		/// 获得数据列
		/// </summary>
		public List<cn.zup.iot.timerdecision.model.Warnfactorconfig> getlistStrategyParam(String strWhere) throws SQLException{
			StringBuffer strSql=new StringBuffer();
			strSql.append("select config_ID,param_Order,data_Type,deviceType,deviceID,deviceParam,min_Value,max_Value,mainFlag,valid_Flag");	
			strSql.append(" FROM warnfactorconfig ");	
			if(strWhere.trim()!=""){
				strSql.append(" where "+strWhere);
			}
			
			List<cn.zup.iot.timerdecision.model.Warnfactorconfig> list = jdbcTemplateHis.query(strSql.toString(), 
					new ResultSetExtractor<List<cn.zup.iot.timerdecision.model.Warnfactorconfig>>(){

				@Override
				public List<cn.zup.iot.timerdecision.model.Warnfactorconfig> extractData(ResultSet rs) throws SQLException,
						DataAccessException {
					List<cn.zup.iot.timerdecision.model.Warnfactorconfig> list = new ArrayList<cn.zup.iot.timerdecision.model.Warnfactorconfig>();
					while (rs.next()){
						cn.zup.iot.timerdecision.model.Warnfactorconfig model=new cn.zup.iot.timerdecision.model.Warnfactorconfig();	
						
						
						model.setConfig_ID(rs.getInt("config_ID")); 
						model.setParam_Order(rs.getInt("param_Order"));								 	
						model.setData_Type(rs.getInt("data_Type")); 
						model.setDeviceType(rs.getInt("deviceType")); 									 	
						model.setDeviceID(rs.getInt("deviceID")); 									 	
						model.setDeviceParam(rs.getInt("deviceParam")); 									 	
						model.setMinValue(rs.getInt("min_Value")); 									 	
						model.setMaxValue(rs.getInt("max_Value")); 
						model.setMainFlag(rs.getString("mainFlag")); 									 	
						 	 
						model.setValid_Flag(rs.getInt("valid_Flag")); 	
						list.add(model);
					}
					return list;
				}
				
			});
			
			return list;
		}

	/**
	 * By ZhangSC
	 * @param data
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
		String value = jdbcTemplateHis.query(sql.toString(), params, new ResultSetExtractor<String>(){
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
	 * @param data
	 * @return
	 */
	public String updateKwhData(cal_param.calc_data data) {
		Calendar calendar = Calendar.getInstance();
		long value = data.getValue();
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

		StringBuffer sql = new StringBuffer(" ");
		if(data.getTargetTable()==1){
			tbName = "ls.ycdata" + year + months;
			sql.append("update " + tbName +" set h"+ hours +" = ?" +
					" WHERE 1=1 ");
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
			sql.append("update " + tbName +" set h"+ hours +" = ?" +
					" WHERE 1=1 ");
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

		System.out.println("时间：h"+hours+" "+riqi+"更新一条数据："+data.getCalcId()+"-"+ bujianleixingid + "-"+bujianid+"-"+bujiancanshuid);
		String error = null;
		try {
			jdbcTemplateHis.update(sql.toString(), value,
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
			//yc表，需首先取出虚拟三段式的值，然后进行存库操作。
			tbName = "ls.ycdata" + year + months;
			bujianid = getXnbjid(bujianleixingid,bujianid,bujiancanshuid);
			if(bujianid != 0){
				StringBuffer sql = new StringBuffer("INSERT INTO " + tbName +
						" (riqi, BuJianLeiXingID, BuJianCanShuID, BuJianID, ChangZhanID, " +
						"H0,H0Flag,H1,H1Flag,H2,H2Flag,H3,H3Flag,H4,H4Flag,H5,H5Flag,H6,H6Flag," +
						"H7,H7Flag,H8,H8Flag,H9,H9Flag,H10,H10Flag,H11,H11Flag,H12,H12Flag,H13,H13Flag," +
						"H14,H14Flag,H15,H15Flag,H16,H16Flag,H17,H17Flag,H18,H18Flag,H19,H19Flag,H20,H20Flag," +
						"H21,H21Flag,H22,H22Flag,H23,H23Flag) VALUES"
						+ " (?, ?, ?, ?, ?, ?, 0, ?, 0 , ?, 0, ?, 0, ?, 0, ?, 0, ?, 0," +
						" ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0," +
						" ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0);");
				System.out.println("时间：h"+hours+" "+riqi+"插入一条数据："+data.getCalcId()+"-"+ bujianleixingid + "-"+bujianid+"-"+bujiancanshuid);
				try {
					jdbcTemplateHis.update(sql.toString(), riqi,20,2,bujianid, changzhanid, hourArray[0], hourArray[1],
							hourArray[2],hourArray[3], hourArray[4], hourArray[5],hourArray[6],hourArray[7],hourArray[8],hourArray[9],
							hourArray[10],hourArray[11],hourArray[12],hourArray[13],hourArray[14],hourArray[15],hourArray[16],hourArray[17],hourArray[18],
							hourArray[19],hourArray[20],hourArray[21],hourArray[22],hourArray[23]);
				} catch (Exception e) {
					error = "insertOpenUsePower：保存数据出错，"+e.getMessage()+"；设别id为："+bujianid+"，部件参数："+bujiancanshuid;
				}
				return error;
			}else{
				return error;
			}
		} else if(data.getTargetTable()==2){
			tbName = "ls.kwhdata" + year + months;
			StringBuffer sql = new StringBuffer("INSERT INTO " + tbName +
					" (riqi, BuJianLeiXingID, BuJianCanShuID, BuJianID, ChangZhanID, " +
					"H0,H0Flag,H1,H1Flag,H2,H2Flag,H3,H3Flag,H4,H4Flag,H5,H5Flag,H6,H6Flag," +
					"H7,H7Flag,H8,H8Flag,H9,H9Flag,H10,H10Flag,H11,H11Flag,H12,H12Flag,H13,H13Flag," +
					"H14,H14Flag,H15,H15Flag,H16,H16Flag,H17,H17Flag,H18,H18Flag,H19,H19Flag,H20,H20Flag," +
					"H21,H21Flag,H22,H22Flag,H23,H23Flag) VALUES"
					+ " (?, ?, ?, ?, ?, ?, 0, ?, 0 , ?, 0, ?, 0, ?, 0, ?, 0, ?, 0," +
					" ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0," +
					" ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0, ?, 0);");
			System.out.println("时间：h"+hours+" "+riqi+"插入一条数据："+data.getCalcId()+"-"+ bujianleixingid + "-"+bujianid+"-"+bujiancanshuid);

			try {
				jdbcTemplateHis.update(sql.toString(), riqi,bujianleixingid,bujiancanshuid,bujianid, changzhanid, hourArray[0], hourArray[1],
						hourArray[2],hourArray[3], hourArray[4], hourArray[5],hourArray[6],hourArray[7],hourArray[8],hourArray[9],
						hourArray[10],hourArray[11],hourArray[12],hourArray[13],hourArray[14],hourArray[15],hourArray[16],hourArray[17],hourArray[18],
						hourArray[19],hourArray[20],hourArray[21],hourArray[22],hourArray[23]);
			} catch (Exception e) {
				error = "insertOpenUsePower：保存数据出错，"+e.getMessage()+"；设别id为："+bujianid+"，部件参数："+bujiancanshuid;
			}
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
		if(buJianId!=0)
			sb.append(" and dybjid = "+buJianId);
		if(bujiancanshuId!=0)
			sb.append(" and dybjparam = "+bujiancanshuId);
		System.out.println(sb);
		List<Integer> result = new ArrayList<Integer>();
		try{
			result = jdbcTemplateHis.query(sb.toString(), new ResultSetExtractor<List<Integer>>() {
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
}
