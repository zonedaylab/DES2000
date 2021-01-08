package cn.zup.iot.timerdecision.service;

import cn.zup.iot.timerdecision.dao.*;
import cn.zup.iot.timerdecision.model.*;
import cn.zup.iot.timerdecision.service.settings.BJLX;
import cn.zup.iot.timerdecision.service.settings.StationChannelId;

import java.text.SimpleDateFormat;
import java.util.*;

public class DecisionEngineService{
	private DecisionTreePmsDao decisionTreePmsDao = new DecisionTreePmsDao();

	private ActivityNodeService activityNodeService = new ActivityNodeService();

	private DecisionDao decisionDao = new DecisionDao();

	private DiagnosisDao diagnosisDao = new DiagnosisDao();

	private HisDataDao hisDataDao = new HisDataDao();

	private DeviceDao deviceDao = new DeviceDao();

	private ScadaWarnPushDao scadaWarnPushDao = new ScadaWarnPushDao();




	private HashMap<Integer, ActivityNode> listAOV = new HashMap<Integer, ActivityNode>();
	private int m_nFlowID;
	private Map<String,String> map=new HashMap<String,String>();



	/**
	 * 对决策树进行初始化，并利用初始化的决策树进行决策。
	 * 每一个设备都必须完成初始化和决策，因为节点的状态是会改变的
	 * @Author 史善力
	 * @date 2020年12月23日21:25:46
	 * @param deviceList 某个决策树下的所有设备信息
	 * @param treeId 决策树id
	 */
	public void BFSAll(List<Device> deviceList,Integer treeId) {
		//每次运行时清空map
		map.clear();
		int counts = 0;
		//设备可以是电站，也可以是具体的设备，因为咱们要完成的目标是要把所有的设备可配置
		for(Device item:deviceList)
		{
			System.out.println("初始化决策树");
			//对每一个设备都要初始化决策树，因为对一个设备进行决策后，决策树就已经变了
			InitAOV(treeId);
			//广度遍历，对刚初始化的这棵树进行遍历
			System.out.println("广度遍历");
			BFS(item);
		}
	}

	/**
	 * 利用传来的treeId对该决策树进行初始化
	 * listAOV的格式是HashMap<Integer, ActivityNode>，key为节点码，value为节点
	 * @Author 史善力
	 * @date 2020年12月23日20:16:54
	 * @param treeId
	 * @return boolean
	 */
	public boolean InitAOV(Integer treeId) {
		listAOV.clear();
		listAOV = decisionTreePmsDao.initDecisionTree(treeId);
		return true;
	}

	/**
	 * 利用决策树对设备进行决策
	 * @Author 史善力
	 * @date 2020年12月23日21:35:19
	 * @param device 包括设备id和设备名称
	 */
	public void BFS(Device device) {
		int i;
		//存放决策树所有运行过的节点
		java.util.Stack<ActivityNode> stackNodesZeroInEdges = new java.util.Stack<ActivityNode>();
		//只是用来存放子节点的，临时栈
		java.util.Stack<ActivityNode> stackNodesZeroInEdgesTmp = new java.util.Stack<ActivityNode>();
		ArrayList<ActivityNode> listOut = new ArrayList<ActivityNode>(); //输出队列
		StringBuilder strMsg = new StringBuilder();
		strMsg.append(String.format("决策流程ID[%1$s],拓扑序列[",(new Integer(this.m_nFlowID)).toString()));
		try
		{
			//对决策树所有的结点进行遍历
			for (Map.Entry<Integer, ActivityNode> item : listAOV.entrySet())
			{
				//只有父节点的入边才为0
				if (item.getValue().getInEdges() == 0)
				{
					stackNodesZeroInEdges.push(item.getValue());
				}
			}
			//下面是对决策树所有的节点进行遍历,把把决策结果放入listOut中，把遍历的节点放入stackNodesZeroInEdges中
			while (stackNodesZeroInEdges.size() > 0)
			{
				while (stackNodesZeroInEdges.size() > 0)
				{
					//栈顶元素出栈
					ActivityNode node = stackNodesZeroInEdges.pop();
					//方便在activityNodeService对这个节点操作
					activityNodeService.node = node;
					System.out.println("准备进入的节点码为"+node.getnActivityCode());
					//对刚出栈的节点进行判断，是不是能够执行业务方法决策，是不是要走的决策路线，进而把决策结果、节点的类型给更新
					activityNodeService.ChangeActivityProperty(listAOV, device);
					//将对操作后的节点放入对列元素中，供后期决策结果查看和入库
					listOut.add(node);
					System.out.println("已经进入的节点码为"+node.getnActivityCode());
					if (node.getStrNextActivityCodes().length() == 0)
					{
						continue;
					}
					String[] nextAllBraches = node.getStrNextActivityCodes().split("[,]", -1);
//					System.out.println("子节点的值"+nextAllBraches[0]+nextAllBraches[1]);
					for (String branch : nextAllBraches)
					{
						ActivityNode childNode = listAOV.get(Integer.parseInt(branch));

						/* 广度优先便利栈顶元素相邻的顶点，并将邻接顶点入度减1，
						 * 找出入度为0的顶点入临时栈stackNodesZeroInEdgesTmp
						 */
						//先令子节点的入度为0
						childNode.setInEdges(childNode.getInEdges()-1);
						//如果子节点的入度真的为0，那就把这个节点放到栈中
						if (childNode.getInEdges() == 0)
						{
							stackNodesZeroInEdgesTmp.push(childNode);
						}
					}
				}
				//将tmp栈中所有的元素都放到正式无入边栈中
				for (ActivityNode  node : stackNodesZeroInEdgesTmp)
				{
					stackNodesZeroInEdges.push(node);
				}
				stackNodesZeroInEdgesTmp.clear();
			}
			//决策结果变量
			StringBuilder strResult=new StringBuilder();
			//决策结果变量
			StringBuilder strResultCode=new StringBuilder();
			Integer treeId= 0;
			boolean warnFlag = false;
			int warnSourceId = 0;
			System.out.println(listAOV.size());
			//当拓扑序列中的元素个数为总元素个数，则拓扑排序成功并输入序列
			if (listOut.size() == listAOV.size())
			{
				//将节点遍历的顺序打印出来
				for (i = 0; i < listOut.size(); i++)
				{
					strMsg.append((new Integer(listOut.get(i).getnActivityCode())).toString());
					if (i < listOut.size() - 1)
					{
						strMsg.append("-->");
					}
					//finish 节点 ，died 节点
				}
				strMsg.append("]");
				System.out.println("全部节点顺序"+strMsg);
				//将决策结果打印出来
				for (i = 0; i < listOut.size(); i++)
				{
					ActivityNode  node=listOut.get(i);
					//因为只有节点类型为finished的节点，才能执行出决策结果
					if(node.getByActivityNodeType()!="FINISHED") {
						continue;
					}
					if(node.getMessage()!=null && !node.getMessage().equals(""))
					{
						strResultCode.append(node.getnActivityCode());
						strResult.append("决策编号"+node.getTreeId()+"-"+node.getnActivityCode());
						strResult.append(":");
						strResult.append(node.getMessage());
						warnFlag = node.getWarnFlag();
						warnSourceId = node.getWarnSourceId();
						treeId = node.getTreeId();

					}
//					strResult.append(node.getnActivityCode());
//					strResult.append(":");
//					strResult.append(node.getActrualValue()+node.getMessage());
//					if (i < listOut.size() - 1)
//					{
//						strResult.append("-->");
//					}
				}
				System.out.println("警告："+strResult);
//				strResult.append("]");
			}
			else
			{
				strMsg.append("The network has cycle"); //当拓扑序列中元素个数少于总元素个数，则AOV网中存在环路，拓扑排序失败
			}

			//插入结果  PM_DIAGNOSIS
//			 DEVICE_ID,DEVICE_TYPE,DIAGNOSIS_RESULT(strResult),DIAGNOSIS_TIME

			/**
			 * warntrueflag为告警flag
			 * 根据电站ID、厂站类型和手动推送告警类型去获取
			 * pm_warn_record和warnrecord表中WARN_TYPE=2为手动告警，1为自动告警
			 * warn_level=5运维提示
			 * equipment_id厂站Id
			 * strResultCode=6为决策6分组电量对比
			 * 获取当前设备是否存在运维提示告警warntrueflag
			 * 存在告警进行告警重复和告警恢复判断、
			 * 不存在告警进行诊断结果添加判断
			 */
			//如果决策结果不为空，判断告警是否存在，如果不存在，就根据warntrueflag判断是否推送告警（即入库）
			if(!strResult.toString().equals(""))
			{//诊断存在告警信息
				//保存诊断结果
				SaveDiagnosis(device,strResult.toString(),strResultCode.toString(), treeId);
				//推送告警标记
				boolean warntrueflag = true;
				//手动推送告警信息获取，判断告警是否存在，如果库里存在就不再推送
				List<PmWarnRecord> warnList = scadaWarnPushDao.GetWarntrueflagList(device.getId(),device.getId());
				if(warnList.size()>0){//手动推送告警存在
					warntrueflag = false;
				}
				if(warntrueflag) //无告警
				{
					//通过决策编号和电站ID查询告警是否存在，如果不存在则插入告警信息
					//判断节点是否插入告警
					if(warnFlag)
					{
						map.put(strResultCode.toString()+"-"+device.getId(), strResult.toString());
						//保存基于设备的告警记录
						//基于电站的告警推送
						//AddWarnRecord(device,strResult.toString(),strResultCode.toString(),warnSourceId, treeId);
						//基于设备的告警推送，基于wanSourceId
						AddDeviceWarnRecord(device,strResult.toString(),strResultCode.toString(),warnSourceId, treeId);
					}
				}
			//决策结果为空，进行手动推送的告警恢复操作，即将告警信息的属性置为该告警已经解决
			}else{
				//推送告警标记
				boolean warntrueflag = false;
				//手动推送告警信息获取
				List<PmWarnRecord> warnList = scadaWarnPushDao.GetWarntrueflagList(device.getId(),device.getId());
				if(warnList.size()>0){//手动推送告警存在
					warntrueflag = true;
				}
				if(warntrueflag) //存在告警
				{
					//已手动推送告警，且诊断结果正常，自动恢复告警
					Date date = new Date();
					//格式化系统时间为String类型
					SimpleDateFormat riqi = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String nowDate=riqi.format(date);
					for(int m=0;m<warnList.size();m++){
						//更新pms中告警记录表，添加恢复时间
						scadaWarnPushDao.updateWarnRecord(warnList.get(m).getWarn_Record_Id(),nowDate);
						//更新monitor中告警记录表，添加恢复时间
						deviceDao.updateWarnRecordToMonitor(warnList.get(m).getWarn_Record_Id(),nowDate);
					}
				}
			}
			//ObjectManager.instance.ShowLog(strMsg.toString(),bInfoLevel);
		}
		catch (RuntimeException ex)
		{
			//	ObjectManager.instance.ShowLogError("BFS执行异常", ex);
		}
	}

	/***
	 * 诊断结果处理
	 */
	public void SaveDiagnosis(Device device,String strResult,String strResultCode,Integer treeId)
	{
		//获取当天日电站诊断信息
		Date date = new Date();
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
		String dateString = df.format(date);
		PmDiagnosis pmDiagnosis= diagnosisDao.getDayDiagnosisInfo(device.getId(), dateString);
		if(pmDiagnosis==null)
		{
			pmDiagnosis = new PmDiagnosis();
			pmDiagnosis.setDevice_Id(device.getId());
			pmDiagnosis.setDiagnosis_Time(new Date());
			pmDiagnosis.setDiagnosis_Result(strResult);
			pmDiagnosis.setDiagnosis_Code(treeId+"-"+strResultCode); //决策编号

			List<StationInfo>  stationList = decisionDao.getStationData("", "", "", 0, 0, device.getId());
			if(stationList!=null&&stationList.size()>0)
			{
				pmDiagnosis.setCity_Name(stationList.get(0).getCityName());
				pmDiagnosis.setProvince_Name(stationList.get(0).getProvinceName());
				pmDiagnosis.setDevice_Name(stationList.get(0).getStationName());
				pmDiagnosis.setCity_Name(stationList.get(0).getCityName());
			}
			diagnosisDao.addDiagnosis(pmDiagnosis);
		}
		else if(pmDiagnosis.getReal_Reason()==null||pmDiagnosis.getReal_Reason().equals(""))
		{
			pmDiagnosis.setDiagnosis_Time(new Date());
			pmDiagnosis.setDiagnosis_Result(strResult);
			pmDiagnosis.setDiagnosis_Code(treeId+"-"+strResultCode); //决策编号
			diagnosisDao.editDiagnosis(pmDiagnosis);
		}
	}

	/***
	 * 诊断结果处理
	 */
	public void AddWarnRecord(Device device,String strResult,String strResultCode,Integer warnSetId,Integer treeId)
	{
		YXData yx = new YXData();
		yx.setBuJianCanShu(StationChannelId.CHANNEL_ID.getValue());
		yx.setBuJianId(0);
		PmWarnRecord warnRecord = diagnosisDao.GetWarnRecordFlag(strResultCode,device.getId(),warnSetId);
		List<StationInfo>  stationList = decisionDao.getStationData("", "", "", 0, 0, device.getId());

		if(warnRecord!=null&&stationList != null&&stationList.size()>0 && warnRecord.getWarn_Record_Id()==0)
		{
			PmWarnRecord record = new PmWarnRecord();
			record.setStation_Id(stationList.get(0).getChangZhanID());
			record.setEquipment_Id(stationList.get(0).getChangZhanID());
			record.setEquipment_Name(stationList.get(0).getStationName());
			record.setEquipment_Code(stationList.get(0).getStationCode());
			record.setAsset_Id(0);
			record.setReal_Warn_Id(warnRecord.getReal_Warn_Id());
			record.setWarn_Name(warnRecord.getWarn_Name()+"-"+strResult.toString());
			record.setWarn_Set_Id(warnRecord.getWarn_Set_Id()); //设置为电站对应的告警配置
			record.setWarn_Level(warnRecord.getWarn_Level()); // 告警级别4代表 电站通讯异常
			record.setWarn_Type(warnRecord.getWarn_Type());
			record.setWarn_Status(1); //1代表未处理
			record.setStation_Name(stationList.get(0).getStationName());
			record.setCityName(stationList.get(0).getCityName());
			record.setProvinceName(stationList.get(0).getProvinceName());
			record.setWarn_Source(3); //来源为scada
			record.setSend_State(2); //发送微信 ;
			System.out.println(record.getProvinceName()+record.getCityName()+record.getStation_Name()+record.getEquipment_Name()+record.getWarn_Name()+record.getAsset_Id());
			record.getAsset_Id();

			Date dateOccur = new Date();
			Calendar calendarHour = new GregorianCalendar();
			calendarHour.setTime(dateOccur);
			calendarHour.add(calendarHour.HOUR_OF_DAY,-3);//把日期往后增加一天.整数往后推,负数往前移动

			record.setOccur_Time(calendarHour.getTime());

			//告警记录插入到pms的分析确认表中pm_analyze_event
			record = scadaWarnPushDao.InsertAnalyzeEvent(record);
//			//告警记录插入到pms
			record = scadaWarnPushDao.InsertWarnRecord(record);
//			//将告警记录同步到monitor
			deviceDao.InsertWarnRecordToMonitor(record);

		}
	}
	/***
	 * 对设备的诊断结果（告警数据）进行存库
	 * 诊断结果处理
	 * device
	 */
	public void AddDeviceWarnRecord(Device device,String strResult,String strResultCode,Integer warnSourceId,Integer treeId)
	{
		YXData yx = new YXData();
		yx.setBuJianCanShu(StationChannelId.CHANNEL_ID.getValue());
		yx.setBuJianId(0);
		PmWarnRecord warnRecord = diagnosisDao.GetDeviceWarnRecordFlag(strResultCode.toString(),device.getId(),warnSourceId, BJLX.huaweinibianqi.getValue());
		List<DeviceInfo> deviceInfoList = decisionDao.getDeviceInfo(device.getId(),6,BJLX.huaweinibianqi.getValue());

		if(warnRecord!=null&&deviceInfoList != null&&deviceInfoList.size()>0 && warnRecord.getWarn_Record_Id()==0)
		{
			PmWarnRecord record = new PmWarnRecord();
			record.setStation_Id(deviceInfoList.get(0).getChangZhanID());
			record.setEquipment_Id(deviceInfoList.get(0).getDeviceId());
			record.setEquipment_Name(deviceInfoList.get(0).getDeviceName());
			record.setEquipment_Code(warnRecord.getEquipment_Code());
			record.setAsset_Id(0);
			record.setReal_Warn_Id(warnRecord.getReal_Warn_Id());
			record.setWarn_Name(warnRecord.getWarn_Name()+"-"+strResult.toString());
			record.setWarn_Set_Id(warnRecord.getWarn_Set_Id()); //设置为电站对应的告警配置
			record.setWarn_Level(warnRecord.getWarn_Level()); // 告警级别4代表 电站通讯异常
			record.setWarn_Type(warnRecord.getWarn_Type());
			record.setWarn_Status(1); //1代表未处理
			record.setStation_Name(deviceInfoList.get(0).getStationName());
			record.setCityName(deviceInfoList.get(0).getCityName());
			record.setProvinceName(deviceInfoList.get(0).getProvinceName());
			record.setWarn_Source(3); //来源为决策树
			record.setSend_State(2); //发送微信 ;
			System.out.println(record.getProvinceName()+record.getCityName()+record.getStation_Name()+record.getEquipment_Name()+record.getWarn_Name()+record.getAsset_Id());
			record.getAsset_Id();

			Date dateOccur = new Date();
			Calendar calendarHour = new GregorianCalendar();
			calendarHour.setTime(dateOccur);
			calendarHour.add(calendarHour.HOUR_OF_DAY,-3);//把日期往后增加一天.整数往后推,负数往前移动

			record.setOccur_Time(calendarHour.getTime());

			//告警记录插入到pms的分析确认表中pm_analyze_event
			record = scadaWarnPushDao.InsertAnalyzeEvent(record);
			//			//告警记录插入到pms
			record = scadaWarnPushDao.InsertWarnRecord(record);
			//			//将告警记录同步到monitor
			deviceDao.InsertWarnRecordToMonitor(record);

		}
	}
	/***
	 * 诊断结果处理
	 */
	public void UpdateWarnRecord(Integer treeId)
	{
		//对map中告警信息进行恢复
		YXData yx = new YXData();
		yx.setBuJianCanShu(StationChannelId.CHANNEL_ID.getValue());
		yx.setBuJianId(0);
		List<PmWarnRecord> warnList = scadaWarnPushDao.GetWarnOperateRecordList("");
		//取出系统时间作为恢复时间
		Date date = new Date();
		Calendar calendarHour = new GregorianCalendar();
		calendarHour.setTime(date);
		calendarHour.set(calendarHour.HOUR_OF_DAY,17);//把日期往后增加一天.整数往后推,负数往前移动  
		//格式化系统时间为String类型
		SimpleDateFormat riqi = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
		String nowDate=riqi.format(calendarHour.getTime());

		int updateNum = 0;
		for(int i=0;i<warnList.size();i++){

			if(!map.containsKey(warnList.get(i).getReal_Warn_Id()+"-"+warnList.get(i).getEquipment_Id())){

				//更新pms中告警记录表，添加恢复时间
				scadaWarnPushDao.updateWarnRecord(warnList.get(i).getWarn_Record_Id(),nowDate);
				//更新monitor中告警记录表，添加恢复时间
				deviceDao.updateWarnRecordToMonitor(warnList.get(i).getWarn_Record_Id(),nowDate);
				//同时将恢复信息推送
				updateNum++;
			}
		}
		System.out.println("共更新"+updateNum+"条数据");
	}




	/**
	 * 运行决策树程序
	 * 得到符合定时信号类型的决策树，通过决策树信息获取所有符合该决策树决策的设备
	 * @Author 史善力
	 * @date 2020年12月23日20:55:39
	 * @param timerType 定时信号类型，有可能是5分钟信号，1小时信号，24小时信号
	 */
	public void runDeviceDecision(int timerType)
	{
		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		int today_minute = today.get(Calendar.MINUTE);//获取当前的分钟
		int today_sec = today.get(Calendar.SECOND);
		int today_hour = today.get(Calendar.HOUR_OF_DAY);
		// ------------------ 开始执行 ---------------------------
		System.out.println("getDecision start"+ today_hour+":"+today_minute+":"+today_sec);
		//从数据中取出所有符合信号类型的有效决策树
		List<DeviceParm> treeIdInfoList = decisionTreePmsDao.listDecisionTree(timerType);
		for(DeviceParm deviceParm:treeIdInfoList){
			//获取所有符合该决策树设备ID、设备类型、区域类型的设备信息;
//			List<DeviceInfo> deviceInfoList = decisionDao.getDeviceInfo(deviceParm.getDeviceId(),deviceParm.getDeviceType(),deviceParm.getBujianType());
			//利用源深目前的一些数据进行测试
			List<DeviceInfo> deviceInfoList = decisionDao.getYuanshenTest();
			List<Device> deviceList = new ArrayList<Device>();
			for(DeviceInfo item:deviceInfoList)
			{
				Device dev = new Device();
				//新的list只存设备id和设备名字
				dev.setId(item.getDeviceId());
				dev.setName(item.getDeviceName());
				deviceList.add(dev);
			}
//			DeviceInfo item = deviceInfoList.get(1);
//			Device dev = new Device();
//			dev.setId(item.getDeviceId());
//			dev.setName(item.getDeviceName());
//			deviceList.add(dev);
			//利用决策树对设备列表进行依次决策
			BFSAll(deviceList,deviceParm.getTreeId());
		}


		// ------------------ 结束执行 ---------------------------
		System.out.println("getDecision end"+ today_hour+":"+today_minute+":"+today_sec);
	}

}
