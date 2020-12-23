package cn.zup.iot.timercalc.service;

import cn.zup.iot.common.model.DataEvent;
import cn.zup.iot.timercalc.dao.CalcDao;
import cn.zup.iot.timercalc.dao.DataDao;
import cn.zup.iot.timercalc.data_struct.cal_param;
import cn.zup.iot.timercalc.model.CalcConfig;
import cn.zup.iot.timercalc.model.CalcParamConfig;
import cn.zup.iot.timercalc.model.DataStructure;
import cn.zup.iot.timercalc.model.EnergyData;
import cn.zup.iot.timercalc.util.JdbcTemplateUtils;
import cn.zup.iot.timercalc.util.JedisUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import redis.clients.jedis.Jedis;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*************************************************************************

* 项目名称
* 文件名称： 定计算任务线程
* 描    述：
*

*
*
************************************************************************/
public class CCalThread{

//	//创建计算中flag;
//	private boolean calcingFlag = false;
	private CalcDao calcDao = new CalcDao();
	private DataDao dataDao = new DataDao();
	private Jedis jedis = JedisUtil.jedisPool.getResource();
	private JdbcTemplate jdbcTemplateLs = JdbcTemplateUtils.jdbcTemplateLs;
	private Connection conn;
	private Statement stmt;

	/**
	 * @author ZhangSC
	 * 针对源深项目重写计算服务
	 * 0点作为起始点，0点之后为今天的数据
	 *
	 */
	public void timingCalc(LocalDateTime localDateTime) throws SQLException {
		System.out.println(localDateTime);
		//int minuteParam = 300000;
		Calendar today = GregorianCalendar.from(ZonedDateTime.of(localDateTime, ZoneId.systemDefault()));
		int today_minute = today.get(Calendar.MINUTE);
		int today_sec = today.get(Calendar.SECOND);
		int today_hour = today.get(Calendar.HOUR_OF_DAY);
		// ------------------ 开始执行 ---------------------------
		System.out.println("定时计算服务开始"+ today_hour+":"+today_minute+":"+today_sec);

		today_minute -= today_minute%5;
		today.set(Calendar.MINUTE,today_minute);
		timingCalcFun(today);
	}

	/**
	 * author ZhangSC
	 * 计算函数
	 * @param today
	 */
	public void timingCalcFun(Calendar today) throws SQLException {
		int today_year = today.get(Calendar.YEAR);
		int today_month = today.get(Calendar.MONTH) + 1;
		int today_day = today.get(Calendar.DAY_OF_MONTH);
		int today_hour = today.get(Calendar.HOUR_OF_DAY);
		int today_minute = today.get(Calendar.MINUTE);
		today_minute = today_minute%5;
		String datatime = String.valueOf(today_year)+"-"+String.format("%02d",today_month)+"-"+String.format("%02d",today_day)+" "+String.format("%02d",0)+"-"+String.format("%02d",today_minute)+"-"+String.format("%02d",0);
		//获取所有的计算信息集合
		CalcConfig calcConfig = new CalcConfig();
		calcConfig.setStartType(1);
		//获取calcconfig表中的所有StartType=1的信息
		List<CalcConfig> calcConfigList = calcDao.getAllCalcInfo(calcConfig);
		DataStructure dataStructure = new DataStructure();
		try{
			conn= JdbcTemplateUtils.dataSourceLs.getConnection();
			stmt = conn.createStatement();
			conn.setAutoCommit(false);
			//遍历所有的计算信息集合
			for(CalcConfig calc: calcConfigList){
				//calcConfigList.parallelStream().forEach(calc->{
				String tablename = "";
				//获取该计算信息的所有计算参数集合
				CalcParamConfig calcParamConfig = new CalcParamConfig();
				calcParamConfig.setCalcID(calc.getCalcID());
				//获取calcparamconfig表中calcId=calc.getCalcID()的所有信息
				List<CalcParamConfig> calcParamConfigList = calcDao.getAllCalcParamsInfo(calcParamConfig);
				//如果发现该电站下逆变器数量不为空
				if(calcParamConfigList != null)
				{
					//获取有多少个逆变器
					int length = calcParamConfigList.size();
					double[] params = new double[length];
					//遍历该计算信息的所有计算参数集合
					for (int i = 0; i < length; i++) {
						//在这里应该将
						//获取电量值并存放到数组中，参数为当前时间，电站信息，逆变器信息，根据calcparamconfig中的ParamStatisTyp来决定是进行日电量啊，还是月电量啊
						System.out.println("信息集合为："+String.format("%s,%s,%s,%s",calcParamConfigList.get(i).getDeviceType(),calcParamConfigList.get(i).getDeviceID(),calcParamConfigList.get(i).getDeviceParam(),calc.getChangZhanID()));
						String tempString = jedis.get(String.format("%s,%s,%s,%s",calcParamConfigList.get(i).getDeviceType(),calcParamConfigList.get(i).getDeviceID(),calcParamConfigList.get(i).getDeviceParam(),calc.getChangZhanID()));
						System.out.println("从redis取出的数据为："+tempString);
						if(tempString==null||tempString==""){
							System.out.println("redis查不到数据，开始从数据库中查找");
							//对每一个逆变器有可能是获取他当前时间的电量，也有可能是日电量或者月电量
							params[i] = timingCalcArrayValue(today, calc, calcParamConfigList.get(i));
						}else{
							System.out.println("从Redis取出的值为"+params[i]);
							params[i] = Double.parseDouble(tempString);
						}
					}
					//这里面应该就是将所有的逆变器的电量求和或者相乘之类的
					double resultValue = calcByFormula(today,calc,params);
					System.out.println("原计算结果为："+resultValue);
					//判断计算结果如果超限，则将其置为0
					if(resultValue>calc.getMaxPositiveValue()||resultValue<calc.getMinPositiveValue()){
						resultValue=0.0;
					}
					String key = calc.getDeviceType()+","+calc.getDeviceID()+","+calc.getDeviceParam()+","+calc.getChangZhanID();
					System.out.println("推送Redis数据为key："+key+",value为"+resultValue);
					jedis.set(key, String.valueOf(resultValue));
					//首先根据这个电站的信息判断操作哪个表。
					//判断完操作哪个表之后，
					//但是插入和更新的时间是用当前的时间（归为哪个时间段，假设是当前时间的统计，而不是日统计）、flink中传来的时间、还是取计算信息的时间
					//将sql语句放入stmt中，然后批量操作sql
					if(calc.getTargetTable()==1){//ycdata表
						tablename = "ycdata"+String.valueOf(today_year)+String.format("%02d",today_month);
						//查询该电站的信息在表中是否存在,利用拼接sql语句，如果存在就更新，不存在就插入。
						addSqlBatch(tablename,datatime,today_hour,resultValue,calc);
					}else if(calc.getTargetTable()==2){//kwhdata
						tablename = "kwhdata"+String.valueOf(today_year)+String.format("%02d",today_month);
						addSqlBatch(tablename,datatime,today_hour,resultValue,calc);

					}else if(calc.getTargetTable()==4) {//energydata表
						tablename = "energydata"+String.valueOf(today_year)+String.format("%02d",today_month);
						addSqlBatch(tablename,datatime,today_hour,resultValue,calc);
//						addBasedataSqlBatch(tablename,datatime,today_hour,resultValue,calc);
					}
				}
			};
			stmt.executeBatch();
			conn.commit();
			System.out.println("提交成功!");
			conn.setAutoCommit(true);
		}catch (SQLException  e){
			e.printStackTrace();
			try{
				if(!conn.isClosed()){
					conn.rollback();
					System.out.println("提交失败，回滚！");
					conn.setAutoCommit(true);
				}

			}catch (SQLException e1){
				e1.printStackTrace();
			}finally {
				stmt.close();
				conn.close();
			}
		}
		System.out.println("运行结束————————————————————————————————————————————————————————");
	}
	/**
	 *
	 * @author ZhangSC
	 * @des 计算值
	 * @param today
	 * @param calcConfig
	 * @param calcParamConfig
	 * @return
	 */
	private double timingCalcArrayValue(Calendar today,CalcConfig calcConfig,CalcParamConfig calcParamConfig)
	{
		double resultValue = 0;
		if(calcParamConfig.getParamStatisType() == 0) //默认获取当前值，这个应该是立刻按当前时间统计
		{
			//获取当前时间段的电量
			resultValue = dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), today,calcConfig.getCalcIntervalTime());
		}else if(calcParamConfig.getParamStatisType()==1) //日统计
		{
			double dateNowValue,dateStartValue = 0.0;
			Calendar dateStart = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
			dateStart.setTimeInMillis(today.getTimeInMillis());
			// 时
			dateStart.set(Calendar.HOUR_OF_DAY, calcConfig.getCalcIntervalTime());
			// 分
			dateStart.set(Calendar.MINUTE, 0);
			// 秒
			dateStart.set(Calendar.SECOND, 0);
			//获取当前时间段的电量
			dateNowValue = dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), today,calcConfig.getCalcIntervalTime());
			//dateStartValue = dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), dateStart,calcConfig.getCalcIntervalTime());
			//获取一天中所有整点的电量
			List<Float> startDataList =  dataDao.getThreeParamDataList(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), dateStart,calcConfig.getCalcIntervalTime());
			if(startDataList!=null)
				for(float sData:startDataList) {
					if(sData-0.1 > 0) {
						dateStartValue = sData;
						break;
					}
				}

			/*if(dateNowValue -0.1 < 0) {
				resultValue = 0;
			}else if(dateStartValue < 0.0){
				List<Float> startDataList =  dataDao.getThreeParamDataList(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), dateStart,calcConfig.getCalcIntervalTime());
				if(startDataList!=null)
					for(float sData:startDataList) {
						if(sData-0.1 > 0) {
							resultValue = dateNowValue - sData;
							break;
						}
					}
			}else {
			}*/
			resultValue = dateNowValue - dateStartValue;
		}else if(calcParamConfig.getParamStatisType() == 2) //月统计
		{
			double dateNowValue,dateStartValue=0.0;
			Calendar todayNew = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
			todayNew.setTimeInMillis(today.getTimeInMillis());

			Calendar dateStart = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
			dateStart.setTimeInMillis(today.getTimeInMillis());
			// 日
			dateStart.set(Calendar.DAY_OF_MONTH, 1);
			// 时
			dateStart.set(Calendar.HOUR_OF_DAY, calcConfig.getCalcIntervalTime());
			// 分
			dateStart.set(Calendar.MINUTE, 0);
			// 秒
			dateStart.set(Calendar.SECOND, 0);

			// 秒
			todayNew.set(Calendar.SECOND, 0);

			List<Float> startDataList =  dataDao.getThreeParamDataList(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), dateStart,calcConfig.getCalcIntervalTime());
			//dateStartValue = dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), dateStart,calcConfig.getCalcIntervalTime());
			dateNowValue = dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), todayNew,calcConfig.getCalcIntervalTime());
			if(startDataList!=null)
				for(float sData:startDataList) {
					if(sData-0.1 > 0) {
						dateStartValue = sData;
						break;
					}
				}
			/*if(dateNowValue-0.1 < 0) {
				for(int i=0;i<todayNew.get(Calendar.DAY_OF_MONTH);i++) {
					todayNew.add(Calendar.DATE, -1);
					dateNowValue = dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), todayNew,calcConfig.getCalcIntervalTime());
					if(dateNowValue-0.1 > 0)
						break;
				}
			}
			if(dateStartValue < 0.0){
				for(int j=0;j<todayNew.get(Calendar.DAY_OF_MONTH);j++) {
					dateStart.add(Calendar.DATE, 1);
					dateStartValue = dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), dateStart,calcConfig.getCalcIntervalTime());
					if(dateStartValue-0.1 > 0)
						break;
				}
			}
			if(dateNowValue -0.1 < 0)
				resultValue = 0;
			else
			*/
			resultValue = dateNowValue - dateStartValue;

		}else if(calcParamConfig.getParamStatisType() == 3) //年统计
		{
			double dateNowValue,dateStartValue;
			Calendar dateStart = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
			dateStart.setTimeInMillis(today.getTimeInMillis());
			// 月
			dateStart.set(Calendar.MONTH, 0);
			// 日
			dateStart.set(Calendar.DAY_OF_MONTH, 1);
			// 时
			dateStart.set(Calendar.HOUR_OF_DAY, 0);
			// 分
			dateStart.set(Calendar.MINUTE, 0);
			// 秒
			dateStart.set(Calendar.SECOND, 0);
			dateStartValue = dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), dateStart,calcConfig.getCalcIntervalTime());
			dateNowValue = dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), today,calcConfig.getCalcIntervalTime());
			resultValue = dateNowValue - dateStartValue;
		}else if(calcParamConfig.getParamStatisType() == 4) //周统计
		{
			double dateNowValue,dateStartValue;
			Calendar dateStart = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
			dateStart.setTimeInMillis(today.getTimeInMillis());
			// 日
			dateStart.set(Calendar.DAY_OF_MONTH, 1);
			//获取本月第一天属于周几
			int monday = dateStart.get(Calendar.DAY_OF_WEEK);
			//获取今天属于每月的第几天
			int dayOfMonth = today.get(Calendar.DAY_OF_MONTH);
			dateStart.setTimeInMillis(today.getTimeInMillis());
			if(monday+dayOfMonth>8)//属于第二周
			{
				int mondayNow = today.get(Calendar.DAY_OF_WEEK)-1;
				int mondays =  mondayNow * 86400000;
				//				if(mondayNow < 1 && today.get(Calendar.HOUR_OF_DAY) < calcConfig.getCalcIntervalTime())
				//					dateStart.setTimeInMillis(dateStart.getTimeInMillis()-86400000-mondays);//再多减去一天
				//				else
				dateStart.setTimeInMillis(dateStart.getTimeInMillis()-mondays);
			}else
				// 时
				{
				dateStart.set(Calendar.DAY_OF_MONTH, 1);
				//				if(today.get(Calendar.HOUR_OF_DAY) < calcConfig.getCalcIntervalTime())
				//					dateStart.setTimeInMillis(dateStart.getTimeInMillis()-86400000);//再多减去一天
				//				else
				//				dateStart.setTimeInMillis(dateStart.getTimeInMillis());

			}
			// 时
			//dateStart.set(Calendar.HOUR_OF_DAY, calcConfig.getCalcIntervalTime());
			dateStart.set(Calendar.HOUR_OF_DAY, 0);
			// 分
			dateStart.set(Calendar.MINUTE, 0);
			// 秒
			dateStart.set(Calendar.SECOND, 0);
			dateStartValue = dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), dateStart,calcConfig.getCalcIntervalTime());
			dateNowValue= dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), today,calcConfig.getCalcIntervalTime());
			resultValue = dateNowValue - dateStartValue;
		}else if(calcParamConfig.getParamStatisType() == 5) //代表更新各月某一周的统计值
		{
			double dateNowValue,dateStartValue;
			Calendar dateStart = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
			//			dateStart.setTimeInMillis(today.getTimeInMillis());
			Calendar dateEnd = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
			//			dateEnd.setTimeInMillis(dateStart.getTimeInMillis());

			Calendar date = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
			date.setTimeInMillis(today.getTimeInMillis());
			date.set(Calendar.DAY_OF_MONTH, 1);
			date.set(Calendar.HOUR_OF_DAY, calcConfig.getCalcIntervalTime());
			// 分
			date.set(Calendar.MINUTE, 0);
			// 秒
			date.set(Calendar.SECOND, 0);
			int day = date.get(Calendar.DAY_OF_WEEK);

			Calendar dateMonth = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
			dateMonth.setTimeInMillis(date.getTimeInMillis());
			dateMonth.set(Calendar.DAY_OF_MONTH,0);
			int dayOfMonth = dateMonth.get(Calendar.DAY_OF_MONTH);

			if(calcConfig.getWeekFlag()==1)//第一周
			{
				dateStart.setTimeInMillis(date.getTimeInMillis()-86400000);
				dateEnd.setTimeInMillis(date.getTimeInMillis()+86400000*(7-day));
			}
			else if(calcConfig.getWeekFlag()==2)
			{
				dateStart.setTimeInMillis(date.getTimeInMillis()+86400000*(7-day));
				dateEnd.setTimeInMillis(date.getTimeInMillis()+86400000*(14-day));
			}
			else if(calcConfig.getWeekFlag()==3)
			{
				dateStart.setTimeInMillis(date.getTimeInMillis()+86400000*(14-day));
				dateEnd.setTimeInMillis(date.getTimeInMillis()+86400000*(21-day));
			}
			else if(calcConfig.getWeekFlag()==4)
			{
				dateStart.setTimeInMillis(date.getTimeInMillis()+86400000*(21-day));
				dateEnd.setTimeInMillis(date.getTimeInMillis()+86400000*(28-day));
			}
			else if(calcConfig.getWeekFlag()==5)
			{
				dateStart.setTimeInMillis(date.getTimeInMillis()+86400000*(28-day));
				//判断当月有多少天
				if(dayOfMonth+day>35)
					dateEnd.setTimeInMillis(date.getTimeInMillis()+86400000*(35-day));
				else
				{
					date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
					dateEnd.setTimeInMillis(date.getTimeInMillis());
				}
			}
			else if(calcConfig.getWeekFlag()==6)
			{
				if(dayOfMonth+day>35)
					dateStart.setTimeInMillis(date.getTimeInMillis()+86400000*(35-day));
				else
				{
					date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
					dateStart.setTimeInMillis(date.getTimeInMillis());
				}
				date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				dateEnd.setTimeInMillis(date.getTimeInMillis()+86400000);
			}

			dateStartValue = dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), dateStart,calcConfig.getCalcIntervalTime());
			dateNowValue= dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), dateEnd,calcConfig.getCalcIntervalTime());
			resultValue = dateNowValue - dateStartValue;
		}else if(calcParamConfig.getParamStatisType() == 7) //季度 统计
		{
			double dateNowValue,dateStartValue=0.0;
			Calendar todayNew = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
			todayNew.setTimeInMillis(today.getTimeInMillis());

			Calendar dateStart = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
			dateStart.setTimeInMillis(today.getTimeInMillis());
			int imonth = todayNew.get(Calendar.MONTH);
			if(imonth<3)
				dateStart.set(Calendar.MONTH, 0);
			if(imonth>2&&imonth<6)
				dateStart.set(Calendar.MONTH, 3);
			if(imonth>5&&imonth<9)
				dateStart.set(Calendar.MONTH, 6);
			if(imonth>8)
				dateStart.set(Calendar.MONTH, 9);
			// 日
			dateStart.set(Calendar.DAY_OF_MONTH, 1);
			// 时
			dateStart.set(Calendar.HOUR_OF_DAY, calcConfig.getCalcIntervalTime());
			// 分
			dateStart.set(Calendar.MINUTE, 0);
			// 秒
			dateStart.set(Calendar.SECOND, 0);

			// 秒
			todayNew.set(Calendar.SECOND, 0);

			List<Float> startDataList =  dataDao.getThreeParamDataList(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), dateStart,calcConfig.getCalcIntervalTime());
			//dateStartValue = dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), dateStart,calcConfig.getCalcIntervalTime());
			dateNowValue = dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), todayNew,calcConfig.getCalcIntervalTime());
			if(startDataList!=null)
				for(float sData:startDataList) {
					if(sData-0.1 > 0) {
						dateStartValue = sData;
						break;
					}
				}
			/*if(dateNowValue-0.1 < 0) {
				for(int i=0;i<todayNew.get(Calendar.DAY_OF_MONTH);i++) {
					todayNew.add(Calendar.DATE, -1);
					dateNowValue = dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), todayNew,calcConfig.getCalcIntervalTime());
					if(dateNowValue-0.1 > 0)
						break;
				}
			}
			if(dateStartValue < 0.0){
				for(int j=0;j<todayNew.get(Calendar.DAY_OF_MONTH);j++) {
					dateStart.add(Calendar.DATE, 1);
					dateStartValue = dataDao.getThreeParamData(calcParamConfig.getDeviceType(), calcParamConfig.getDeviceID(), calcParamConfig.getDeviceParam(), calcParamConfig.getDataType(), dateStart,calcConfig.getCalcIntervalTime());
					if(dateStartValue-0.1 > 0)
						break;
				}
			}
			if(dateNowValue -0.1 < 0)
				resultValue = 0;
			else
			*/
			resultValue = dateNowValue - dateStartValue;

		}

		//计算系数和偏移量
		resultValue = resultValue * calcParamConfig.getfCoef()+calcParamConfig.getfOffset();
		if(resultValue<0)
			resultValue = 0;
		return resultValue;
	}
	/**
	 * @author samson
	 * @des 计算参数的值
	 * @param calcConfig
	 * @param doubleValue
	 * @return
	 */
	private double calcByFormula(Calendar today,CalcConfig calcConfig,double[] doubleValue){
		double value = 0;
		//判断calcconfig中的formula是为多少，将该电站下的所有逆变器的电量进行运算
		if(calcConfig.getFormula()==1) //1代表 求和
		{
			for (double d : doubleValue) {
				value+=d;
			}
		}else if(calcConfig.getFormula()==2) //2代表相减
		{
			if(doubleValue.length>0)
			{
				value = doubleValue[0];
				for (int i = 1; i < doubleValue.length; i++) {
					value -= doubleValue[i];
				}
			}
		}else if(calcConfig.getFormula()==3) //3代表想乘
		{
			double startValue = 1;
			for (double d : doubleValue) {
				startValue = startValue*d;
			}
			value = startValue;
		}else if(calcConfig.getFormula()==4) //4代表相除
		{
			if(doubleValue.length>0)
			{
				value = doubleValue[0];
				for (int i = 1; i < doubleValue.length; i++) {
					if(doubleValue[i] != 0)
						value = value / doubleValue[i];
					else
						value = 0;
				}
			}
		}else if(calcConfig.getFormula()==5) //5代表总数据累计，累加上次的值
		{
			for (double d : doubleValue) {
				value+=d;
			}

			Calendar dateStart = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
			dateStart.setTimeInMillis(today.getTimeInMillis()-86400000);//减去一天
			//暂时禁用，打开为取昨日24个整点量
			List<Float> startDataList =  dataDao.getThreeParamDataList(calcConfig.getDeviceType(),calcConfig.getDeviceID(), calcConfig.getDeviceParam(), calcConfig.getTargetTable(), dateStart,calcConfig.getCalcIntervalTime());
			if(startDataList!=null)
				for(int i=startDataList.size()-1;i>=0;i--) {
					if(startDataList.get(i)-0.1 > 0) {
						value +=startDataList.get(i);
						break;
					}
				}
			//获取上次的值
			/*dateStart.set(Calendar.HOUR_OF_DAY, 23);
			// 分
			dateStart.set(Calendar.MINUTE, 50);
			// 秒
			dateStart.set(Calendar.SECOND, 0);
			//获取上次的值
			value += dataDao.getThreeParamData(calcConfig.getDeviceType(), calcConfig.getDeviceID(), calcConfig.getDeviceParam(), 2, dateStart,calcConfig.getCalcIntervalTime());
*/
		}else if(calcConfig.getFormula()==6) //6代表特殊公式
		{
			List<Double> data = new ArrayList<>();
			for (double d : doubleValue) {
				data.add(d);
			}
			value = Double.parseDouble(getValueByExpression(calcConfig.getExpression(),data));
		}else if(calcConfig.getFormula()==7){
			int num = 0;
			for (double d : doubleValue) {
				num+=1;
				value+=d;
			}
			if(num!=0){
				value/=num;
			}
		}
		return value;
	}

	/*据自定义公式获取数据
	 row 公式字段行  col 公式字段列
	 expression 遵循 javascript 代码 ，变量定义   #r11c12表示 第11行12列

	Sample 2:

	 var curValue=x1-x2
	 if(curValue<0)
	 	return 0;
	 else
	 	retrun curValue;

	 */

	static String getValueByExpression( String expression,List<Double>data){

		final ScriptEngine engine =new ScriptEngineManager().getEngineByName("js");
		/*正则表达式：[x]+\d+
		\w：表任意，有可能是字母、数字等
		[x]：表示限定字母x，
		+：它前面的元素限定至少一个
		\d:表数字
		\d+：表限定至少一个数字。x1能检测出来，x12也能监测出来。x监测不出来
		*/
		Pattern pattern   = Pattern.compile("[x]+\\d+");
		Matcher matcher   = pattern.matcher(expression);
		String expCalculate=expression;


		while(matcher.find()){
			//每一个符合正则的字符串
			String jsVar = matcher.group();
			int index=Integer.parseInt(jsVar.substring(1));
			String value ="("+data.get(index-1)+")";
			expCalculate=expCalculate.replace(jsVar, value);
			//System.out.println(jsVar);
		}
		if(expCalculate.contains("/(0.0)"))
			return "0.0";
		Object result = null;
		try {
			result = engine.eval(expCalculate);
		} catch (ScriptException e) {
			//System.out.println("计算公式报错，公式："+expression);
			e.printStackTrace();
		}
		//System.out.println("结果类型:" + result.getClass().getName() + ",计算结果:" + result);
		return result.toString();

	}

	public int queryData(String tablename, String datatime, CalcConfig value){
		StringBuffer sql = new StringBuffer();
		sql.append(" select count(*) from "+tablename);
		sql.append(" where RiQi='"+datatime);
		sql.append("' and BuJianLeiXingID="+value.getDeviceType());
		sql.append(" and BuJianID="+value.getDeviceID());
		sql.append(" and BuJianCanShuID="+value.getDeviceParam());
		sql.append(" and ChangZhanID="+value.getChangZhanID());
		int count = jdbcTemplateLs.queryForObject(sql.toString(), Integer.class);
		return count;
	}
	public void addSqlBatch(String tablename,String datatime,int today_hour,Double resultValue,CalcConfig calc) throws SQLException {
		if(queryData(tablename,datatime,calc)>0){
			String sql = "update "+tablename+" set "+" H"+String.valueOf(today_hour)+"="+Double.valueOf(resultValue)+ " where RiQi="+"'"+datatime+"'"+ " and BuJianLeiXingID="+calc.getDeviceType()+" and BuJianID="+calc.getDeviceID()+" and BuJianCanShuID="+calc.getDeviceParam()+" and ChangZhanID="+calc.getChangZhanID();
			stmt.addBatch(sql);
		}else{
			String sql = "insert DELAYED into "+tablename+" (RiQi, BuJianLeiXingID, BuJianID, BuJianCanShuID, ChangZhanID,"+" H"+String.valueOf(today_hour)+") values("+"'"+datatime+"'"+","+calc.getDeviceType()+","+calc.getDeviceID()+","+calc.getDeviceParam()+","+calc.getChangZhanID()+","+Double.valueOf(resultValue)+")";
			stmt.addBatch(sql);
		}
	}
	/**
	 * Author 史善力
	 * date 2020年12月23日08:25:09
	 * description：对基础表完成插入或者更新操作
	 **/
	public void addBasedataSqlBatch(String tablename,String datatime,int today_hour,Double resultValue,CalcConfig calc) throws SQLException {
		if(queryData(tablename,datatime,calc)>0){
			String sql = "update "+tablename+" set "+" H"+String.valueOf(today_hour)+"="+Double.valueOf(resultValue)+ " where RiQi="+"'"+datatime+"'"+ " and BuJianLeiXingID="+calc.getDeviceType()+" and BuJianID="+calc.getDeviceID()+" and BuJianCanShuID="+calc.getDeviceParam()+" and ChangZhanID="+calc.getChangZhanID();
			stmt.addBatch(sql);
		}else{
			String sql = "insert DELAYED into "+tablename+" (RiQi, BuJianLeiXingID, BuJianID, BuJianCanShuID, ChangZhanID,"+" H"+String.valueOf(today_hour)+") values("+"'"+datatime+"'"+","+calc.getDeviceType()+","+calc.getDeviceID()+","+calc.getDeviceParam()+","+calc.getChangZhanID()+","+Double.valueOf(resultValue)+")";
			stmt.addBatch(sql);
		}
	}

}




