package cn.zup.iot.timerdecision.service;

import cn.zup.iot.timerdecision.dao.*;
import cn.zup.iot.timerdecision.model.*;
import cn.zup.iot.timerdecision.model.cal_param.calc_data;
import cn.zup.iot.timerdecision.service.settings.BJLX;
import cn.zup.iot.timerdecision.service.settings.ChangZhanParam;
import cn.zup.iot.timerdecision.service.settings.ChannelType;
import cn.zup.iot.timerdecision.service.settings.StationChannelId;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ScadaWarnPushService {


	private ScadaWarnPushDao scadaWarnPushDao;
	private DiagnosisDao diagnosisDao;
	private DecisionDao decisionDao;
	public DecisionDao getDecisionDao() {
		return decisionDao;
	}

	public void setDecisionDao(DecisionDao decisionDao) {
		this.decisionDao = decisionDao;
	}

	public DiagnosisDao getDiagnosisDao() {
		return diagnosisDao;
	}

	public void setDiagnosisDao(DiagnosisDao diagnosisDao) {
		this.diagnosisDao = diagnosisDao;
	}

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
	 * @param
	 * @return
	 */
	public List<PmWarnRecord> GetWarnRecordList(){
		return scadaWarnPushDao.GetWarnRecordList();
	}

	/**
	 * 获取通讯告警记录
	 * @param
	 * @return
	 */
	public List<PmWarnRecord> GetStatusWarnRecordList(){
		return scadaWarnPushDao.GetStatusWarnRecordList();
	}
	/**
	 * 获取通讯告警记录
	 * @param
	 * @return
	 */
	public List<PmWarnRecord> GetDeviceStatusWarnRecordList(){
		return scadaWarnPushDao.GetDeviceStatusWarnRecordList();
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
	 * @param
	 */
	public void updateWarnRecord(Integer warnRecordId , String nowDate){
		scadaWarnPushDao.updateWarnRecord(warnRecordId, nowDate);
	}
	/***
	 * 更新恢复时间 toMonitor
	 * @param
	 */
	public  void updateWarnRecordToMonitor(Integer warnRecordId, String nowDate){
		deviceDao.updateWarnRecordToMonitor(warnRecordId,nowDate);
	}

	
	/***
	 * 告警推送方法 
	 */
	public List<PmWarnRecord> WarnPushData()
	{  
        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		int today_minute = today.get(Calendar.MINUTE);
		int today_sec = today.get(Calendar.SECOND); 
		int today_hour = today.get(Calendar.HOUR_OF_DAY);  
        // ------------------ 开始执行 ---------------------------  
		System.out.println("getWarn start"+ today_hour+":"+today_minute+":"+today_sec);
        
        //获取告警信息
		List<YXData> listYX = hisDataDao.WarnPushData("22","2", "0");
		List<PmWarnRecord> recordList = new ArrayList<PmWarnRecord>();
		
		if(listYX!=null&&listYX.size()>0)
		{
			System.out.println(listYX.size());
//			if(listYX.size()>0){
//				for(int i=0;i<listYX.size();i++)
//				{
//					System.out.println(listYX.get(i).getXNbuJianId()+"厂站："+listYX.get(i).getChangZhanId()+"参数："+listYX.get(i).getXNbuJianCanShu()+"时间"+listYX.get(i).getTime()+"值："+listYX.get(i).getValue());
//				}
//			}
//			else 
//				System.out.println("no warnData");
			
			//遍历告警信息
			int Index = 0;
			int warnNum = 0;
			final Map<Integer,String> map=new HashMap<Integer,String>();
			for(int i=0;i<listYX.size();i++)
			{
				//判断是否需要推送告警
				YXData yx =deviceDao.getYXXNData(listYX.get(i).getXNbuJianId(),listYX.get(i).getTime());
				if(yx==null)
				{
					System.out.println(listYX.get(i).getXNbuJianId());
					continue;
				}
				yx.setTime(listYX.get(i).getTime());
				yx.setValue(listYX.get(i).getValue());
				yx.setXNbuJianId(listYX.get(i).getXNbuJianId());
				yx.setXNbuJianCanShu(listYX.get(i).getXNbuJianCanShu());
				yx.setXNbuJianLeiXing(listYX.get(i).getXNbuJianLeiXing());
				PmWarnRecord record = GetWarnRecordFlag(yx);
				//存成map
					
	    		map.put(warnNum++,yx.getBuJianCanShu()+""+yx.getBuJianId());

				if(record!=null && record.getWarn_Set_Id()!=0 && record.getWarn_Record_Id()==0)
				{
    				//推送告警记录 
					System.out.println("pust warns start..."+ today_hour+":"+today_minute+":"+today_sec);
					
					SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
					try { 
						Date date = sdf.parse(yx.getTime());  
						
						record.setOccur_Time(date);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					record.setStation_Id(yx.getChangZhanId());
					record.setStation_Name(yx.getStationName());
					record.setCityName(yx.getCityName());
					record.setProvinceName(yx.getProviceName());
					record.setWarn_Status(1);//1表示未处理。5表示已处理
					record.setSend_State(2);
					record.setWarn_Source(1);
					//插入电压电流
					record.setVoltage(yx.getVoltage());
					record.setCircuit(yx.getCircuit());
					//(添加告警恢复状态为未恢复0)
					record.setWarn_Recover_Type(0);
					System.out.println(record.getProvinceName()+record.getCityName()+record.getStation_Name()+record.getEquipment_Name()+record.getWarn_Name()+record.getAsset_Id());
					//告警记录插入到pms
					record = InsertWarnRecord(record);
					//将告警记录同步到monitor
    				InsertWarnRecordToMonitor(record);	
					recordList.add(record);
    				Index++;
    				
				}
				
			}
			System.out.println("共插入"+Index+"条数据");
			System.out.println(" 插入完成 ");
			
			//取出现有告警记录
			int updateNum = 0;
			List<PmWarnRecord> currentRecord =GetWarnRecordList();
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
			for(int i=0;i<currentRecord.size();i++){
				
				if(!map.containsValue(currentRecord.get(i).getReal_Warn_Id()+""+currentRecord.get(i).getEquipment_Id())){

					System.out.println(currentRecord.get(i).getProvinceName()+currentRecord.get(i).getCityName()+currentRecord.get(i).getStation_Name()+currentRecord.get(i).getEquipment_Name()+currentRecord.get(i).getWarn_Name()+currentRecord.get(i).getAsset_Id());
					//更新pms中告警记录表，添加恢复时间
					updateWarnRecord(currentRecord.get(i).getWarn_Record_Id(),nowDate);
					//更新monitor中告警记录表，添加恢复时间
					updateWarnRecordToMonitor(currentRecord.get(i).getWarn_Record_Id(),nowDate);
					//同时将恢复信息推送
					currentRecord.get(i).setOccur_Recover(recoverDate);
					currentRecord.get(i).setWarn_Recover_Type(1);
					recordList.add(currentRecord.get(i));
					updateNum++;
				}
			}
			System.out.println("共更新"+updateNum+"条数据");
			//更新电站通讯状态
			DtuStatusPushData();
		}
			
		return recordList;
	}
	

	/***
	 * 电站通讯状态推送方法 
	 */
	public List<PmWarnRecord> DtuStatusPushData()
	{  
        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		int today_minute = today.get(Calendar.MINUTE);
		int today_sec = today.get(Calendar.SECOND); 
		int today_hour = today.get(Calendar.HOUR_OF_DAY);  
        // ------------------ 开始执行 ---------------------------  
		System.out.println("getStatusPush start"+ today_hour+":"+today_minute+":"+today_sec);
		List<PmWarnRecord> recordList = new ArrayList<PmWarnRecord>();
		//8点30到17点00 
		if((today_hour>6 && today_hour<19) || (today_hour==8 && today_minute>30))
		{
        
	        //获取告警信息
			List<YCInfoData> listYCInfoData = hisDataDao.YCInfoPushData("20","2", "0"); // 20 代表了虚拟单精类型  2代表参数为 虚拟量值 0代表无参数 
			
			if(listYCInfoData!=null)
			{
				System.out.println(listYCInfoData.size());
				if(listYCInfoData.size()>0){
					for(int i=0;i<listYCInfoData.size();i++)
					{
	//					System.out.println(listYCInfoData.get(i).getXNbuJianId()+"厂站："+listYCInfoData.get(i).getChangZhanId()+"参数："+listYCInfoData.get(i).getXNbuJianCanShu()+"时间"+listYCInfoData.get(i).getTime()+"值："+listYCInfoData.get(i).getValue());
						YCInfoData yc = new YCInfoData();
						byte lastTimes=1 ;
						int temp=0;
						int dtuRealStat=0;
						yc.setBuJianLeiXing(BJLX.changzhan.getValue());
						yc.setBuJianCanShu(ChangZhanParam.stationrunstate.getValue());
						yc.setBuJianId(listYCInfoData.get(i).getBuJianId());
						yc.setValue(listYCInfoData.get(i).getValue());
						yc.setLasttimes(lastTimes);
	
						List<PmWarnRecord> currentRecord =GetStatusWarnRecordList();
						final Map<Integer,String> map=new HashMap<Integer,String>();
						for(PmWarnRecord item :currentRecord)
						{
							map.put(item.getStation_Id(), item.getWarn_Record_Id().toString());
						}
						
						List<YCInfoData> ycList = hisDataDao.getYcDataInfo(yc);
						if(ycList!=null&&ycList.size()>0)
						{
							temp = ycList.get(0).getLasttimes();
							if(yc.getValue()==ChannelType.CHANNEL_ERROR.getValue()
									|| yc.getValue() == ChannelType.CHANNEL_ERRCODE.getValue() 
									|| yc.getValue() == ChannelType.CHANNEL_NOUSE.getValue() 
									|| yc.getValue() == ChannelType.CHANNEL_WSHERR.getValue() 
							)
								dtuRealStat=0;						
							else
								dtuRealStat=1;
								
							if(dtuRealStat==0)//异常
								temp = temp*2;
							else
								temp = temp*2 + 1;
							yc.setLasttimes((byte)temp);
							
							if(((temp >> 1) & 0x1)==0 && ((temp >> 0) & 0x1) ==0)
							{
								if(ycList.get(0).getLastValue()==null || ycList.get(0).getLastValue()!=1)
								{
									yc.setTime(listYCInfoData.get(i).getTime());
									yc.setLastValue(1d);  //设置1 代表通讯关闭
								}
								else
								{
									yc.setLastValue(1d);  //设置1 代表通讯关闭
								}
							}
							else
							{
								yc.setTime(listYCInfoData.get(i).getTime());
								yc.setLastValue(0d); //设置0代表通讯正常
							}
							
							//设置统计状态
	//						int calsStat = ycList.get(0).getCalsStat();
	//						if(dtuRealStat==0)  //通讯异常
	//							calsStat = calsStat & ~(1<<today_hour);
	//						else
	//							calsStat = calsStat | 1<<today_hour;
	//						
	//						calsStat=calsStat%0xffffff;
							
							int calsStat = ycList.get(0).getCalsStat();
							if(dtuRealStat==0 )  //通讯异常
								calsStat = calsStat*2;
							else
								calsStat = calsStat*2+1;
	
							calsStat=calsStat & 0xfff;//通讯时间改为1个小时后推出
							
							PmWarnRecord record = new PmWarnRecord();
	
							if(calsStat>1)
							{
								yc.setValue(0d); //设置0代表通讯正常
								
								//通讯正常后告警恢复
								if(!map.isEmpty()&& map.containsKey(yc.getBuJianId()))
								{	
									//更新pms中告警记录表，添加恢复时间
									Date date = new Date();
									//格式化系统时间为String类型
									SimpleDateFormat riqi = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									String nowDate=riqi.format(date);
									String recordId = map.get(yc.getBuJianId()).split(",")[0];
									updateWarnRecord(Integer.parseInt(recordId),nowDate);
									//更新monitor中告警记录表，添加恢复时间
									updateWarnRecordToMonitor(Integer.parseInt(map.get(yc.getBuJianId()).split(",")[0]),nowDate);
								}
							}
							else
							{
								yc.setValue(1d);  //设置1 代表通讯关闭
								
								//通讯关闭后推送告警
								if(map.isEmpty() || !map.containsKey(yc.getBuJianId()))
								{
									YXData yx = new YXData();
									yx.setBuJianCanShu(StationChannelId.CHANNEL_ID.getValue());
									yx.setBuJianId(0);
									PmWarnRecord warnRecord = GetWarnRecordFlag(yx);
									
									
									List<StationInfo>  stationList = hisDataDao.getStationData("", "", "", 0, 0, yc.getBuJianId());
									if(stationList!=null&&stationList.size()>0 && warnRecord !=null )
									{
										record.setStation_Id(stationList.get(0).getChangZhanID());
										record.setEquipment_Id(stationList.get(0).getChangZhanID());
										record.setEquipment_Name(stationList.get(0).getStationName());
										record.setEquipment_Code(stationList.get(0).getStationCode());
										record.setAsset_Id(0);
										record.setReal_Warn_Id(0);
										record.setWarn_Name(warnRecord.getWarn_Name());
										record.setWarn_Set_Id(warnRecord.getWarn_Set_Id()); //设置为电站对应的告警配置
										record.setWarn_Level(warnRecord.getWarn_Level()); // 告警级别4代表 电站通讯异常
										record.setWarn_Type(warnRecord.getWarn_Type());
										record.setWarn_Status(1); //1代表未处理
										record.setStation_Name(stationList.get(0).getStationName());
										record.setCityName(stationList.get(0).getCityName());
										record.setProvinceName(stationList.get(0).getProvinceName());
										record.setWarn_Source(1); //来源为scada
										record.setSend_State(2); //发送微信 ;
										System.out.println(record.getProvinceName()+record.getCityName()+record.getStation_Name()+record.getEquipment_Name()+record.getWarn_Name()+record.getAsset_Id());
										record.getAsset_Id();
										
										Date dNow = new Date();   //当前时间
										Date dBefore = new Date();
										Calendar calendar = Calendar.getInstance(); //得到日历
										calendar.setTime(dNow);//把当前时间赋给日历
										calendar.add(Calendar.HOUR_OF_DAY, -1);  //设置为前两个小时
										dBefore = calendar.getTime();   //得到前一天的时间
										record.setOccur_Time(dBefore);  //因为通讯关闭需要持续24小时才有效，因此发生时间为24小时之前时间
										
										//告警记录插入到pms
										record = InsertWarnRecord(record);
										//将告警记录同步到monitor
					    				InsertWarnRecordToMonitor(record);
									}
								}
							}
							yc.setCalsStat(calsStat);
							
							hisDataDao.UpdateYcDataInfo(yc);
						}
						else
							hisDataDao.InsertYcDataInfo(yc);
					}
				}
				else 
					System.out.println("no warnData");
				
			}
		}
			
		return recordList;
	}

	/***
	 * Author By ZhangSC
	 * 电表通讯状态推送方法
	 */
	public void DeviceOnlineWarnPushData() throws ParseException {
		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		int today_minute = today.get(Calendar.MINUTE);
		int today_sec = today.get(Calendar.SECOND);
		int today_hour = today.get(Calendar.HOUR_OF_DAY);
		// ------------------ 开始执行 ---------------------------
		if(today_minute%5==4){
			System.out.println("getStatusPush start"+ today_hour+":"+today_minute+":"+today_sec);
			List<PmWarnRecord> recordList = new ArrayList<PmWarnRecord>();

			//获取缓存文件中对应设备的实时通讯状态数据，0表示通讯正常，1表示通讯故障；
			List<YCInfoData> listYCInfoData = hisDataDao.YCInfoPushData("20","2", "0"); // 20 代表了虚拟单精类型  2代表参数为 虚拟量值 0代表无参数
			//获取当前数据库中的已有通讯告警数据。用来与缓存文件中的数据进行对比；
			List<PmWarnRecord> currentRecord =GetDeviceStatusWarnRecordList();
			final Map<Integer,String> currentWarnMap=new HashMap<Integer,String>();
			SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = format.parse("2020-01-01 00:00:00");
			for(PmWarnRecord item :currentRecord)
			{
				long time = item.getOccur_Time().getTime();
				currentWarnMap.put(item.getEquipment_Id(), item.getWarn_Record_Id().toString()+","+time);
			}
			if(listYCInfoData!=null)
			{
				Map<Integer, Integer> warnDelayMapOfOnline =  WarnDelay.warnDelayMapOfOnline;
				Map<Integer, Integer> warnRecoverMapOfOnline =  WarnDelay.warnRecoverMapOfOnline;
				System.out.println(listYCInfoData.size());
				if(listYCInfoData.size()>0) {
					for (int i = 0; i < listYCInfoData.size(); i++) {
						calc_data calc_data = new calc_data();
						int buJianId = listYCInfoData.get(i).getBuJianId();
						if (listYCInfoData.get(i).getValue() == 0)//通讯正常,告警恢复及更新通讯时间;
						{
							//通讯正常，将告警恢复map告警数置为0。
							if(warnRecoverMapOfOnline.containsKey(buJianId)){
								warnRecoverMapOfOnline.put(buJianId,warnRecoverMapOfOnline.get(buJianId)-1);
							}else{
								warnRecoverMapOfOnline.put(buJianId,0);
							}
							if(warnRecoverMapOfOnline.get(buJianId)<0){
								warnRecoverMapOfOnline.put(buJianId,0);
								//通讯正常后告警恢复
								if (!currentWarnMap.isEmpty() && currentWarnMap.containsKey(listYCInfoData.get(i).getBuJianId())) {
									//更新pms中告警记录表，添加恢复时间
									Date dateNow = new Date();
									//格式化系统时间为String类型
									String nowDate = format.format(dateNow);
									String recordId = currentWarnMap.get(listYCInfoData.get(i).getBuJianId()).split(",")[0];
									updateWarnRecord(Integer.parseInt(recordId), nowDate);
									//更新monitor中告警记录表，添加恢复时间
									updateWarnRecordToMonitor(Integer.parseInt(recordId), nowDate);
								}
								//通讯正常，更新该装置的对应通讯时间

								//设置要读取的时间字符串格式
								if (listYCInfoData.get(i).getTime() != null && listYCInfoData.get(i).getTime() != "") {
									date = format.parse(listYCInfoData.get(i).getTime());
									calc_data.setDate(date);
									calc_data.setChangZhanID(listYCInfoData.get(i).getChangZhanId());
									calc_data.setDeviceID(listYCInfoData.get(i).getBuJianId());
									calc_data.setTargetTable(2);
									calc_data.setDeviceType(BJLX.yuanshendianbiao.getValue());
									calc_data.setDeviceParam(15);
									calc_data.setValue(date.getTime()/1000);
									saveOnlineTime(calc_data);
								}
							}
						} else //通讯关闭
						{
							//通讯关闭进入时间判断，五分钟运行一次告警判断，连续12个间隔（1小时）后，持续中断则推出告警
							if(warnDelayMapOfOnline.containsKey(buJianId)&&!currentWarnMap.containsKey(buJianId)){
								if(warnDelayMapOfOnline.get(buJianId)>=12){
									//判断持续通讯中断12次以上，推出该告警
									warnDelayMapOfOnline.put(buJianId,0);
									//如果告警数据中没有此条告警，则添加。
									Device device = new Device();
									device.setId(listYCInfoData.get(i).getBuJianId());
									String strResult = "采集器通讯关闭，导致电表数据无法采集";
									String warnSourceId = "1010";//源深电表通讯关闭的real_warnId,即部件参数
									AddDeviceWarnRecord(device, strResult, warnSourceId);
								}else{
									warnDelayMapOfOnline.put(buJianId,warnDelayMapOfOnline.get(buJianId)+1);
								}
							}else{
								warnDelayMapOfOnline.put(buJianId,1);
							}

							//添加通讯关闭时间
							String timeValue = "";
							if (currentWarnMap.containsKey(listYCInfoData.get(i).getBuJianId())){
								timeValue = currentWarnMap.get(listYCInfoData.get(i).getBuJianId()).split(",")[1];
								calc_data.setValue(Long.valueOf(timeValue)/1000);
							}else{
								date = format.parse(listYCInfoData.get(i).getTime());
								calc_data.setValue(date.getTime()/1000);
							}
							date = format.parse(listYCInfoData.get(i).getTime());
							calc_data.setDate(date);
							calc_data.setChangZhanID(listYCInfoData.get(i).getChangZhanId());
							calc_data.setDeviceID(listYCInfoData.get(i).getBuJianId());
							calc_data.setTargetTable(2);
							calc_data.setDeviceType(BJLX.yuanshendianbiao.getValue());
							calc_data.setDeviceParam(15);
							saveOnlineTime(calc_data);
						}
					}
				}else
					System.out.println("no warnData");
			}
		}
	}
	/*
	*存储设备通讯时间
	 */
	public void saveOnlineTime(calc_data data){
		if(hisDataDao.isNotNull(data)){
			hisDataDao.updateKwhData(data);
		}else {
			hisDataDao.insertKwhData(data);
		}
	}

	/***
	 * 对设备的诊断结果（告警数据）进行存库
	 * 诊断结果处理
	 * device
	 */
	public void AddDeviceWarnRecord(Device device, String strResult, String warnSourceId)
	{
		YXData yx = new YXData();
		yx.setBuJianCanShu(StationChannelId.CHANNEL_ID.getValue());
		yx.setBuJianId(0);
		PmWarnRecord warnRecord = diagnosisDao.GetDeviceWarnRecordFlag(warnSourceId,device.getId(),Integer.valueOf(warnSourceId), BJLX.yuanshendianbiao.getValue());
		List<DeviceInfo> deviceInfoList = decisionDao.getDeviceInfo(device.getId(),6,54);

		if(warnRecord!=null&&deviceInfoList != null&&deviceInfoList.size()>0 && warnRecord.getWarn_Record_Id()==0) {
			PmWarnRecord record = new PmWarnRecord();
			record.setStation_Id(deviceInfoList.get(0).getChangZhanID());
			record.setEquipment_Id(deviceInfoList.get(0).getDeviceId());
			record.setEquipment_Name(deviceInfoList.get(0).getDeviceName());
			record.setEquipment_Code(warnRecord.getEquipment_Code());
			record.setAsset_Id(0);
			record.setReal_Warn_Id(warnRecord.getReal_Warn_Id());
			record.setWarn_Name(warnRecord.getEquipment_Name()+":"+warnRecord.getWarn_Name() + "-" + strResult.toString());
			record.setWarn_Set_Id(warnRecord.getWarn_Set_Id()); //设置为电站对应的告警配置
			record.setWarn_Level(warnRecord.getWarn_Level()); // 告警级别4代表 电站通讯异常
			record.setWarn_Type(warnRecord.getWarn_Type());
			record.setWarn_Status(1); //1代表未处理
			record.setStation_Name(deviceInfoList.get(0).getStationName());
			record.setCityName(deviceInfoList.get(0).getCityName());
			record.setProvinceName(deviceInfoList.get(0).getProvinceName());
			record.setWarn_Source(1); //来源为scada
			record.setSend_State(2); //发送微信 ;
			System.out.println(record.getProvinceName() + record.getCityName() + record.getStation_Name() + record.getEquipment_Name() + record.getWarn_Name() + record.getAsset_Id());
			record.getAsset_Id();

			Date dateOccur = new Date();
			Calendar calendarHour = new GregorianCalendar();
			calendarHour.setTime(dateOccur);
			calendarHour.add(calendarHour.HOUR_OF_DAY, -1);//当前源深告警为延迟1小时推出，故告警时间提前1小时。
			record.setOccur_Time(calendarHour.getTime());

			//告警记录插入到pms的分析确认表中pm_analyze_event
			record = scadaWarnPushDao.InsertAnalyzeEvent(record);
			//			//告警记录插入到pms
			record = scadaWarnPushDao.InsertWarnRecord(record);
			//			//将告警记录同步到monitor
			deviceDao.InsertWarnRecordToMonitor(record);

		}
	}
}




