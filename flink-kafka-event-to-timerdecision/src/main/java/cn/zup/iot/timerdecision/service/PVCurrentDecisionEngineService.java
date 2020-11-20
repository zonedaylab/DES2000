package cn.zup.iot.timerdecision.service;

import cn.zup.iot.timerdecision.dao.*;
import cn.zup.iot.timerdecision.model.*;
import cn.zup.iot.timerdecision.service.settings.StationChannelId;

import java.text.SimpleDateFormat;
import java.util.*;

public class PVCurrentDecisionEngineService {

	private cn.zup.iot.timerdecision.service.ActivityNodeService activityNodeService ;

	private DecisionDao decisionDao;

	private DiagnosisDao diagnosisDao;
	private HisDataDao hisDataDao;

	private DeviceDao deviceDao;
	private ScadaWarnPushDao scadaWarnPushDao;

	public cn.zup.iot.timerdecision.service.ActivityNodeService getActivityNodeService() {
		return activityNodeService;
	}

	public void setActivityNodeService(cn.zup.iot.timerdecision.service.ActivityNodeService activityNodeService) {
		this.activityNodeService = activityNodeService;
	}

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

	public HisDataDao getHisDataDao() {
		return hisDataDao;
	}

	public void setHisDataDao(HisDataDao hisDataDao) {
		this.hisDataDao = hisDataDao;
	}

	public ScadaWarnPushDao getScadaWarnPushDao() {
		return scadaWarnPushDao;
	}

	public void setScadaWarnPushDao(ScadaWarnPushDao scadaWarnPushDao) {
		this.scadaWarnPushDao = scadaWarnPushDao;
	}

	public DeviceDao getDeviceDao() {
		return deviceDao;
	}

	public void setDeviceDao(DeviceDao deviceDao) {
		this.deviceDao = deviceDao;
	}

	///#region 基于AOV网的广度优先搜索算法
	private HashMap<Integer, ActivityNode> listAOV = new HashMap<Integer, ActivityNode>();
	private int m_nFlowID;
	private Map<String,String> map=new HashMap<String,String>();

	public void BFS(Device device) {
		int i;
		Stack<ActivityNode> stackNodesZeroInEdges = new Stack<ActivityNode>();
		Stack<ActivityNode> stackNodesZeroInEdgesTmp = new Stack<ActivityNode>();
		ArrayList<ActivityNode> listOut = new ArrayList<ActivityNode>(); //输出队列
		StringBuilder strMsg = new StringBuilder();
		strMsg.append(String.format("决策流程ID[%1$s],拓扑序列[",(new Integer(this.m_nFlowID)).toString()));
		try
		{
			//循环检测邻接表入度为0的活动，并将其压栈，同期入栈中的为并行活动
			for (Map.Entry<Integer, ActivityNode> item : listAOV.entrySet())
			{
				if (item.getValue().getInEdges() == 0)
				{
					stackNodesZeroInEdges.push(item.getValue());
				}
			}
			while (stackNodesZeroInEdges.size() > 0)
			{
				while (stackNodesZeroInEdges.size() > 0)
				{
					ActivityNode node = stackNodesZeroInEdges.pop(); //栈顶元素出栈
					activityNodeService.node = node;
					activityNodeService.ChangeActivityProperty(listAOV, device);
					listOut.add(node); //将栈顶元素放入拓扑序列数组中，同时也表示了已经进入拓扑序列中的元素个数
					if (node.getStrNextActivityCodes().length() == 0)
					{
						continue;
					}
					String[] nextAllBraches = node.getStrNextActivityCodes().split("[,]", -1);
					for (String branch : nextAllBraches)
					{
						ActivityNode childNode = listAOV.get(Integer.parseInt(branch));

						/* 广度优先便利栈顶元素相邻的顶点，并将邻接顶点入度减1，
						 * 找出入度为0的顶点入临时栈stackNodesZeroInEdgesTmp
						 */
						childNode.setInEdges(childNode.getInEdges()-1);
						if (childNode.getInEdges() == 0)
						{
							stackNodesZeroInEdgesTmp.push(childNode);
						}
					}
				}
				for (ActivityNode  node : stackNodesZeroInEdgesTmp)
				{
					stackNodesZeroInEdges.push(node);
				}
				stackNodesZeroInEdgesTmp.clear();
			}

			StringBuilder strResult=new StringBuilder();//决策结果变量
			StringBuilder strResultCode=new StringBuilder();//决策结果变量
			Integer treeId= 0;
			boolean warnFlag = false;
			int warnSetId = 0;
			if (listOut.size() == listAOV.size()) //当拓扑序列中的元素个数为总元素个数，则拓扑排序成功并输入序列
			{
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

				//输出决策结果
				for (i = 0; i < listOut.size(); i++)
				{
					ActivityNode  node=listOut.get(i);
					if(node.getByActivityNodeType()!="FINISHED")
						continue;
					if(node.getMessage()!=null && !node.getMessage().equals(""))
					{
						strResultCode.append(node.getnActivityCode());
						strResult.append("决策编号"+node.getTreeId()+"-"+node.getnActivityCode());
						strResult.append(":");
						strResult.append(node.getMessage());
						warnFlag = node.getWarnFlag();
						warnSetId = node.getWarnSourceId();
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
			//根据诊断是否存在告警信息进行判断
			if(!strResult.toString().equals(""))
			{//诊断存在告警信息
				//保存诊断结果
				SaveDiagnosis(device,strResult.toString(),strResultCode.toString(), treeId);
				//推送告警标记
				boolean warntrueflag = true;
				//手动推送告警信息获取
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
						//AddWarnRecord(device,strResult.toString(),strResultCode.toString(),warnSetId, treeId);
						//基于设备的告警推送
						AddDeviceWarnRecord(device,strResult.toString(),strResultCode.toString(),warnSetId, treeId);
					}
				}

			}else{//诊断不存在告警信息，进行手动推送的告警恢复操作
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

	public void BFSAll(List<Device> deviceList,Integer treeId) {

		map.clear(); //清空map
		int counts = 0;
		for(Device item:deviceList)
		{
			InitAOV(treeId);
			BFS(item);
			System.out.println(counts++);
		}
		//UpdateWarnRecord(treeId);
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
			pmDiagnosis.setDiagnosis_Result(strResult.toString());
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
			pmDiagnosis.setDiagnosis_Result(strResult.toString());
			pmDiagnosis.setDiagnosis_Code(treeId+"-"+strResultCode); //决策编号,暂定为treeId-code
			diagnosisDao.editDiagnosis(pmDiagnosis);
		}
	}
	/***
	 * 对设备的诊断结果（告警数据）进行存库
	 * 诊断结果处理
	 * device
	 */
	public void AddDeviceWarnRecord(Device device,String strResult,String strResultCode,Integer warnSoueceId,Integer treeId)
	{
		YXData yx = new YXData();
		yx.setBuJianCanShu(StationChannelId.CHANNEL_ID.getValue());
		yx.setBuJianId(0);
		PmWarnRecord warnRecord = diagnosisDao.GetWarnRecordFlag(strResultCode,device.getId(),warnSoueceId);

		List<StationInfo> deviceInfoList = decisionDao.getStationData(device.getName(),null,null,0,0,device.getId());

		if(warnRecord!=null&&deviceInfoList != null&&deviceInfoList.size()>0 && warnRecord.getWarn_Record_Id()==0)
		{
			PmWarnRecord record = new PmWarnRecord();
			record.setStation_Id(deviceInfoList.get(0).getChangZhanID());
			record.setEquipment_Id(deviceInfoList.get(0).getChangZhanID());
			record.setEquipment_Name(deviceInfoList.get(0).getStationName());
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

			record.setOccur_Time(calendarHour.getTime());

			//告警记录插入到pms的分析确认表中pm_analyze_event
			record = scadaWarnPushDao.InsertAnalyzeEvent(record);
			//			//告警记录插入到pms
			record = scadaWarnPushDao.InsertWarnRecord(record);
			//			//将告警记录同步到monitor
			deviceDao.InsertWarnRecordToMonitor(record);

		}
	}

	public boolean InitAOV(Integer treeId) {
		listAOV.clear();
		ActivityNode node;		
		//父节点流转规则
		
		//1.初始化父亲节点 判断电站状态  
		node = new ActivityNode();
		node.Init();
		node.setTreeId(treeId);
		node.setnAcitivityID(1);
		node.setnActivityCode(1);
		node.setnParentCount(0);
		node.setInEdges(node.getnParentCount()); 
		node.setByActivityNodeType("FINISHED");
		node.setStrNextActivityCodes("2,3");	
		node.setDataAddress("10");
		node.setWarnFlag(false);
		if(!listAOV.containsKey(node.getnActivityCode()))
			listAOV.put(node.getnActivityCode(), node);

	  	ArrayList<NodeRule> ruleList=new ArrayList<NodeRule>() ; //Judge rules 判断依据   	
		NodeRule rule = new NodeRule();
		rule.setCONDITIONS(3); //3代表等于
		rule.setJUDGE_VALUE("1"); //1代表值 如果为1则true 否则0false
		rule.setGOTO_ACTIVITY(3);
		ruleList.add(rule);
		rule = new NodeRule();
		rule.setCONDITIONS(3); //3代表等于
		rule.setJUDGE_VALUE("0"); //1代表值 如果为1则true 否则0false
		rule.setGOTO_ACTIVITY(2);
		ruleList.add(rule);
		node.setRuleList(ruleList);
		
		
		//2.初始化子节点 电站状态正常后逆变器组串信息状态
		node = new ActivityNode();
		node.Init();
		node.setTreeId(treeId);
		node.setnAcitivityID(2);
		node.setnActivityCode(2);
		node.setnParentCount(1);
		node.setInEdges(node.getnParentCount()); 
		node.setByActivityNodeType("SLEEP");
		node.setStrNextActivityCodes("4,5");		
		node.setDataAddress("11");
		node.setWarnFlag(false);
		node.setWarnSourceId(1012);//配置告警对应的部件参数（warn_source表中warn_id）
		if(!listAOV.containsKey(node.getnActivityCode()))
			listAOV.put(node.getnActivityCode(), node);

	  	ruleList=new ArrayList<NodeRule>() ; //Judge rules 判断依据   	
		rule = new NodeRule();
		rule.setCONDITIONS(3); //3代表等于
		rule.setJUDGE_VALUE("1"); //1代表值 如果为1则true 否则false
		rule.setGOTO_ACTIVITY(5);
		ruleList.add(rule);
		rule = new NodeRule();
		rule.setCONDITIONS(3); //3代表等于
		rule.setJUDGE_VALUE("0"); //1代表值 如果为1则true 否则false
		rule.setGOTO_ACTIVITY(4);
		ruleList.add(rule);
		node.setRuleList(ruleList);
		
		
		//电站有告警，跳到这个节点，不做任何处理，决策树结束
		node = new ActivityNode();
		node.Init();
		node.setTreeId(treeId);
		node.setnActivityCode(3); 			
		node.setnAcitivityID(3); 
		node.setnParentCount(1); 
		node.setInEdges(node.getnParentCount());  
		node.setByActivityNodeType("SLEEP");
		node.setStrNextActivityCodes("");	
		node.setWarnFlag(false);	
		
		if(!listAOV.containsKey(node.getnActivityCode()))
			listAOV.put(node.getnActivityCode(), node);
		
		
		//逆变器状态 正常 判断电表离散率
		node = new ActivityNode();
		node.Init();
		node.setTreeId(treeId);
		node.setnActivityCode(4); 			
		node.setnAcitivityID(4); 
		node.setnParentCount(1); 
		node.setDataAddress("12");
		node.setWarnFlag(false);
		node.setWarnSourceId(1013); //判断电表离散率 是否破损
		node.setInEdges(node.getnParentCount());  
		node.setByActivityNodeType("SLEEP");
		node.setStrNextActivityCodes("");
		
		if(!listAOV.containsKey(node.getnActivityCode()))
			listAOV.put(node.getnActivityCode(), node);
		
		
		//逆变器状态 通讯异常节点
		node = new ActivityNode();
		node.Init();
		node.setTreeId(treeId);
		node.setnActivityCode(5);
		node.setnAcitivityID(5); 
		node.setnParentCount(1); 
		node.setInEdges(node.getnParentCount());  
		node.setByActivityNodeType("SLEEP");
		node.setStrNextActivityCodes("");
		node.setWarnFlag(false);		
		
		if(!listAOV.containsKey(node.getnActivityCode()))
			listAOV.put(node.getnActivityCode(), node);
		
		return true;
	}
	
	//供定时器调用
	public void allPVCurrentDecision()
	{
		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		int today_minute = today.get(Calendar.MINUTE);
		int today_sec = today.get(Calendar.SECOND); 
		int today_hour = today.get(Calendar.HOUR_OF_DAY);  
        // ------------------ 开始执行 ---------------------------  
		System.out.println("getDecision start"+ today_hour+":"+today_minute+":"+today_sec);
		
		//先获取所有的电站信息
		List<Changzhan> list = decisionDao.getContainer(0,0,0);
		List<Device> deviceList = new ArrayList<Device>();
		for(Changzhan item:list)
		{
			Device dev = new Device();
			dev.setId(item.getId());
			dev.setName(item.getName());
			deviceList.add(dev);
		}
		BFSAll(deviceList,3);

        // ------------------ 结束执行 ---------------------------  
		System.out.println("getDecision end"+ today_hour+":"+today_minute+":"+today_sec);
	}
	
}
