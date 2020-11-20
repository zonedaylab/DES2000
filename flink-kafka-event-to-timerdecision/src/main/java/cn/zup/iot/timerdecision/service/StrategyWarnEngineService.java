package cn.zup.iot.timerdecision.service;

import cn.zup.iot.timerdecision.dao.DeviceDao;
import cn.zup.iot.timerdecision.dao.HisDataDao;
import cn.zup.iot.timerdecision.dao.ScadaWarnPushDao;
import cn.zup.iot.timerdecision.model.Commdev;
import cn.zup.iot.timerdecision.model.PmWarnRecord;
import cn.zup.iot.timerdecision.model.YXData;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StrategyWarnEngineService {

	//设备评估策略
	public static class  s_warn_strategy
	{	
		public int 		strategyID;			//策略ID
		public byte		deviceType;			//设备类型 马达 母线 变压器  电容  5.	
		public byte 	generateType;		//生成类型： 1.告警 2.状态
		public byte     generateSubtype;   	//生成子类型  如果告警，如谐波畸变   		
		public String   expression;			//策略公式：  
		
		public List<cn.zup.iot.timerdecision.model.Warnfactorconfig> listStrategyParam;
		public void addParamList(List<cn.zup.iot.timerdecision.model.Warnfactorconfig> paramList) {
			
			listStrategyParam=paramList;
		}
		
	} 	

	//参数的数据类型
	public interface param_data_type{
		public  int yx=1;  			//遥信
		public  int yc_limit=2;  	//遥测越限
		public int yc_change=3;		//遥测变更  例如马达启动次数
		public int yc_expression=4;	//遥测公式，预留		
	}
	
	private ScadaWarnPushDao scadaWarnPushDao;

	public ScadaWarnPushDao getScadaWarnPushDao() {
		return scadaWarnPushDao;
	}

	public void setScadaWarnPushDao(ScadaWarnPushDao scadaWarnPushDao) {
		this.scadaWarnPushDao = scadaWarnPushDao;
	}

	
	private HisDataDao hisDataDao = new HisDataDao();
	
	public HisDataDao getHisDataDao() {
		return hisDataDao;
	}

	public void setHisDataDao(HisDataDao hisDataDao) {
		this.hisDataDao = hisDataDao;
	}
	
	private DeviceDao deviceDao;
	
	public DeviceDao getDeviceDao() {
		return deviceDao;
	}
	
	public void setDeviceDao(DeviceDao deviceDao) {
		this.deviceDao = deviceDao;
	}
	//获取策略设备
	List<s_warn_strategy> listStrategy;	
	

	
	Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));	
	
	
	
	
	/***
	 * 根据策略进行告警推送
	 *  1.变压器 ，局放数据获取状态，并记录状态发生时的故障录波。暂时按照遥信进行处理。珠海一多
		2.母线监测 接地电流/电压。 北京的一个厂家
		3.电容器/SVG 。参数：跳闸、 谐波、畸变率、温度越限   from 南自		
		4.变频器 根据启动次数进行判断，启动次数发生变化就认为是报警。 from 南自		
		分合闸次数（一级）    零序电流、漏电流（二次）（遥测）    		
		5.电动机马达保护：（1）华建马保护：预警告过载、故障状态（遥信）。from 南自
	 * @throws ParseException 
	 * @throws SQLException 
	 */	
	public void  WarnMessageGenerateAPI()  {
		
		try {
			
		listStrategy=hisDataDao.getlistStrategy();
		
		Map<String,Double>  mapMeasureData = new HashMap<String,Double> ();
		Map<String,Double>  mapMeasureDataPre = new HashMap<String,Double> ();
		
		//获取遥测数据
		mapMeasureData.putAll(hisDataDao.getYcMapData4Strategy(0,0,0,1));
		
		//遥信数据
		mapMeasureData.putAll(hisDataDao.getYxMapData4Strategy(0,0,0,1));
		
		
		//获取遥测数据
		mapMeasureDataPre.putAll(hisDataDao.getYcMapData4Strategy(0,0,0,2));
		
		//遥信数据
		mapMeasureDataPre.putAll(hisDataDao.getYxMapData4Strategy(0,0,0,2));
		
		
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		int today_minute = today.get(Calendar.MINUTE);
		int today_sec = today.get(Calendar.SECOND); 
		int today_hour = today.get(Calendar.HOUR_OF_DAY);  		
        
		System.out.println("getWarn start"+ today_hour+":"+today_minute+":"+today_sec);
		
		List<YXData> listYX =new ArrayList<YXData>();
		
		
		for(int i =0;i<listStrategy.size();i++) {
			
			//遍历设备并获取该设备对应的策略参数
			s_warn_strategy strategy=listStrategy.get(i);
			
			//设备列表
			List<Commdev> listDevice=hisDataDao.getBujian(strategy.deviceType);
			byte deviceType=strategy.deviceType;	
			
			for (Commdev commdev : listDevice) {				
				
				if(strategy.listStrategyParam.size()>1) {
					
					List<Double> listValue=new ArrayList<Double>();
					for(int j=0;j<strategy.listStrategyParam.size();j++) {						
						cn.zup.iot.timerdecision.model.Warnfactorconfig strategyParam=strategy.listStrategyParam.get(j);				
						String key=String.format("%d-%d-%d",deviceType,commdev.getID(),strategyParam.getDeviceParam());
						Double value=mapMeasureData.get(key);	
						listValue.add(value);
					}
					byte yxvalue=Byte.parseByte(getValueByExpression(strategy.expression,listValue));
					YXData yx=new YXData();	
					yx.setBuJianLeiXing(deviceType);
					yx.setBuJianId(commdev.getID());
					//yx.setBuJianCanShu(strategy());	
					yx.setTime(String.valueOf(today.getTimeInMillis()));
					yx.setParamDataType(param_data_type.yx);					
					yx.setValue(yxvalue);						
					listYX.add(yx);	
				}
				else 
				for(int j=0;j<strategy.listStrategyParam.size();j++) {
					
					cn.zup.iot.timerdecision.model.Warnfactorconfig strategyParam=strategy.listStrategyParam.get(j);				
					String key=String.format("%d-%d-%d",deviceType,commdev.getID(),strategyParam.getDeviceParam());
					Double value=mapMeasureData.get(key);	
					boolean bFind=false;
					byte yxvalue=0;
					switch(strategyParam.getData_Type()) {				
						case param_data_type.yx://变位
							if(value>0) {	
								yxvalue=1;	
								bFind=true;
							}
							break;
						case param_data_type.yc_limit:
							if(value<strategyParam.getMinValue()||value>strategyParam.getMaxValue()) {
								yxvalue=1;
								bFind=true;
							}
							break;
						case param_data_type.yc_change:
							
							Double valuePre=mapMeasureDataPre.get(key);	
							if(Math.abs(value-valuePre)>0.000001) {
								yxvalue=1;
								bFind=true;
							}								
							break;
						default:
							break;
					}
					if(bFind){
						YXData yx=new YXData();	
						yx.setBuJianLeiXing(deviceType);
						yx.setBuJianId(commdev.getID());
						yx.setBuJianCanShu(strategyParam.getDeviceParam());	
						yx.setTime(String.valueOf(today.getTimeInMillis()));
						yx.setParamDataType(strategyParam.getData_Type());
						
						yx.setValue(yxvalue);						
						listYX.add(yx);			
					}
				}
			}	
		}			
		
			GenerateWarn(listYX);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}
	
	private  void  GenerateWarn(List<YXData> listYX) throws ParseException {		
		
		int today_minute = today.get(Calendar.MINUTE);
		int today_sec = today.get(Calendar.SECOND); 
		int today_hour = today.get(Calendar.HOUR_OF_DAY);  		
		
		List<PmWarnRecord> recordList = new ArrayList<PmWarnRecord>();
		int Index = 0;
		int warnNum = 0;
		final Map<String,Integer> mapCurrentWarn=new HashMap<String,Integer>();
		
		for(int i=0;i<listYX.size();i++)//判断是否需要推送告警			
		{			
			YXData yx=listYX.get(i);
			PmWarnRecord record = GetWarnRecordFlag(yx);//获取该参数对应的告警配置和告警记录
			warnNum++;		
			int paraDataType=yx.getParamDataType();
			
    		mapCurrentWarn.put(""+yx.getBuJianId()+yx.getBuJianCanShu(),paraDataType);
    		
			if(record==null || record.getWarn_Set_Id()==0 )//没有告警配置
				continue;
				
			if(record.getWarn_Record_Id()==0) //没有告警记录id，则插入
			{
				//推送告警记录 
				System.out.println("pust warns start..."+ today_hour+":"+today_minute+":"+today_sec);					
				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 			
				Date date = sdf.parse(yx.getTime());  						
				record.setOccur_Time(date);				
				record.setStation_Id(yx.getChangZhanId());
				record.setStation_Name(yx.getStationName());
				record.setCityName("");
				record.setProvinceName(yx.getProviceName());
				record.setWarn_Status(1);//1表示未处理。5表示已处理
				record.setSend_State(2);
				record.setWarn_Source(1);	
				record.setWarn_Recover_Type(0);//(添加告警恢复状态为未恢复0)
				System.out.println(record.getProvinceName()+record.getCityName()+record.getStation_Name()+record.getEquipment_Name()+record.getWarn_Name()+record.getAsset_Id());
				record = InsertWarnRecord(record);//告警记录插入到pms
				InsertWarnRecordToMonitor(record);	//将告警记录同步到monitor
				recordList.add(record);
				Index++;
			}
			else if (yx.getParamDataType()== param_data_type.yc_change){//针对遥测变化的更新告警次数 todo:
				
			}
		}
		System.out.println("插入完成 ，共插入"+Index+"条数据");	
		
		//取出现有告警记录
		int updateNum = 0;
		List<PmWarnRecord> listHistRecord =GetWarnRecordList();
		//取出系统时间作为恢复时间
		Date date = new Date();
		//格式化系统时间为String类型
		SimpleDateFormat riqi = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String nowDate=riqi.format(date);
		Date recoverDate = new Date();
		try {				
			recoverDate = riqi.parse(nowDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i=0;i<listHistRecord.size();i++){	
			
			//此处存在问题，应该加上部件类型
			String key=listHistRecord.get(i).getEquipment_Id()+""+listHistRecord.get(i).getReal_Warn_Id();
						
			if(!mapCurrentWarn.containsKey(key)){	
				
				//判断参数的数据类型，如果为param_data_type.yc_change，不能恢复。
				if(mapCurrentWarn.get(key)== param_data_type.yc_change)
					continue;

				System.out.println(listHistRecord.get(i).getProvinceName()+listHistRecord.get(i).getCityName()+listHistRecord.get(i).getStation_Name()+listHistRecord.get(i).getEquipment_Name()+listHistRecord.get(i).getWarn_Name()+listHistRecord.get(i).getAsset_Id());
				//更新pms中告警记录表，添加恢复时间
				updateWarnRecord(listHistRecord.get(i).getWarn_Record_Id(),nowDate);
				//更新monitor中告警记录表，添加恢复时间
				updateWarnRecordToMonitor(listHistRecord.get(i).getWarn_Record_Id(),nowDate);
				//同时将恢复信息推送
				listHistRecord.get(i).setOccur_Recover(recoverDate);
				listHistRecord.get(i).setWarn_Recover_Type(1);
				recordList.add(listHistRecord.get(i));
				updateNum++;
			}
		}
		System.out.println("共更新"+updateNum+"条数据");		
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
	 
	 static String getValueByExpression( String expression,List<Double>data) throws Exception
	 {
		 
		 final ScriptEngine engine =new ScriptEngineManager().getEngineByName("js");         
        		 
        Pattern pattern   = Pattern.compile("\\B#x?\\w*");
        Matcher matcher   = pattern.matcher(expression);
        String expCalculate=expression;     
  
        
        while(matcher.find()){
    		//每一个符合正则的字符串
    		String jsVar = matcher.group(); 
    		int index=Integer.parseInt(jsVar.substring(1));
     		String value ="("+data.get(index)+")";
     		expCalculate=expCalculate.replace(jsVar, value);
    		System.out.println(jsVar);
        }   
        if(expCalculate.contains("/(0.0)"))
       	return "0.0";
        Object result = engine.eval(expCalculate);   
        System.out.println("结果类型:" + result.getClass().getName() + ",计算结果:" + result);
   	 	return result.toString();
   	
	 }
	 
	
	
	/**
	 * 获取告警更新信息
	 * @param yx
	 * @return
	 */
	public PmWarnRecord GetWarnRecordFlag(YXData yx){
		return scadaWarnPushDao.GetWarnRecordFlag(yx);
	}
	/**
	 * 获取告警记录
	 * @return
	 */
	public List<PmWarnRecord> GetWarnRecordList(){
		return scadaWarnPushDao.GetWarnRecordList();
	}

	/**
	 * 获取通讯告警记录
	 * @return
	 */
	public List<PmWarnRecord> GetStatusWarnRecordList(){
		return scadaWarnPushDao.GetStatusWarnRecordList();
	}
	
	/***
	 * 插入警告信息 
	 * @param pmWarnRecord
	 */
	public PmWarnRecord InsertWarnRecord(PmWarnRecord pmWarnRecord){
		 return scadaWarnPushDao.InsertWarnRecord(pmWarnRecord);
	
	}
	/***
	 * 插入警告信息 
	 * @param pmWarnRecord
	 */
	public void InsertWarnRecordToMonitor(PmWarnRecord pmWarnRecord){
		deviceDao.InsertWarnRecordToMonitor(pmWarnRecord);

	}
	
	/***
	 * 更新恢复时间 
	 * @param warnRecordId
	 * @param nowDate
	 */
	public void updateWarnRecord(Integer warnRecordId , String nowDate){
		scadaWarnPushDao.updateWarnRecord(warnRecordId, nowDate);
	}
	/***
	 * 更新恢复时间 toMonitor
	 * @param warnRecordId
	 * @param nowDate
	 */
	public  void updateWarnRecordToMonitor(Integer warnRecordId, String nowDate){
		deviceDao.updateWarnRecordToMonitor(warnRecordId,nowDate);
	}


	
	
	

}

