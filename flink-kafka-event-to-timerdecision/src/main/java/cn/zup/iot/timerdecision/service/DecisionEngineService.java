package cn.zup.iot.timerdecision.service;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import cn.zup.iot.timerdecision.dao.*;
import cn.zup.iot.timerdecision.model.*;
import cn.zup.iot.timerdecision.service.settings.BJLX;
import cn.zup.iot.timerdecision.service.settings.StationChannelId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DecisionEngineService implements Serializable{

	@Autowired
	private InitTreeDao initTreeDao;

	@Autowired
	private ActivityNodeService activityNodeService;

	@Autowired
	private DecisionDao decisionDao;

	@Autowired
	private DiagnosisDao diagnosisDao;

	@Autowired
	private HisDataDao hisDataDao;

	@Autowired
	private DeviceDao deviceDao;

	@Autowired
	private ScadaWarnPushDao scadaWarnPushDao;



	///#region 基于AOV网的广度优先搜索算法
	private HashMap<Integer, ActivityNode> listAOV = new HashMap<Integer, ActivityNode>();
	private int m_nFlowID;
	private Map<String,String> map=new HashMap<String,String>();

	public void BFS(Device device) {
		int i;
		java.util.Stack<ActivityNode> stackNodesZeroInEdges = new java.util.Stack<ActivityNode>();
		java.util.Stack<ActivityNode> stackNodesZeroInEdgesTmp = new java.util.Stack<ActivityNode>();
		java.util.ArrayList<ActivityNode> listOut = new java.util.ArrayList<ActivityNode>(); //输出队列
		StringBuilder strMsg = new StringBuilder();
		strMsg.append(String.format("决策流程ID[%1$s],拓扑序列[",(new Integer(this.m_nFlowID)).toString()));
		try
		{
			//循环检测邻接表入度为0的活动，并将其压栈，同期入栈中的为并行活动
			for (java.util.Map.Entry<Integer, ActivityNode> item : listAOV.entrySet())
			{
				//每个节点的入边都是一个
				if (item.getValue().getInEdges() == 0)
				{
					stackNodesZeroInEdges.push(item.getValue());
				}
			}
			while (stackNodesZeroInEdges.size() > 0)
			{
				while (stackNodesZeroInEdges.size() > 0)
				{
					//取出元素，每次只取出一个结点
					ActivityNode node = stackNodesZeroInEdges.pop(); //栈顶元素出栈
					System.out.println(node.getnActivityCode());
					//目的是activityNodeService也能对node的属性进行改变
					activityNodeService.node = node;
					//listAOV是这个树的架构，device是含有设备id和设备名称，这样如果有错，也知道哪里错了
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

			StringBuilder strResult=new StringBuilder();//决策结果变量
			StringBuilder strResultCode=new StringBuilder();//决策结果变量
			Integer treeId= 0;
			boolean warnFlag = false;
			int warnSourceId = 0;
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
						//AddWarnRecord(device,strResult.toString(),strResultCode.toString(),warnSourceId, treeId);
						//基于设备的告警推送
						AddDeviceWarnRecord(device,strResult.toString(),strResultCode.toString(),warnSourceId, treeId);
					}
				}

			}else{//诊断不存在告警信息，进行手动推送的告警恢复操作
				//推送告警标记
				boolean warntrueflag = false;
				//手动推送告警信息获取
				scadaWarnPushDao = new ScadaWarnPushDao();
				deviceDao = new DeviceDao();
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
	//deviceList:设备list，获取到所有的设备，比如济南所有的电站
	public void BFSAll(List<Device> deviceList,Integer treeId) {

		map.clear(); //清空map
		int counts = 0;
		for(Device item:deviceList)//设备可以是电站，也可以是具体的设备，因为咱们要完成的目标是要把所有的设备可配置
		{
			System.out.println("初始化决策树");
			//对每一个设备都要初始化决策树，为什么，因为对每一个设备都要进行决策，判断有没有问题，如果一个设备有问题，那这颗树某个节点的属性就改变啦，所以进行初始化
			InitAOV(treeId);
			//广度遍历，对刚初始化的这棵树进行遍历
			System.out.println("广度遍历");
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
		PmWarnRecord warnRecord = diagnosisDao.GetDeviceWarnRecordFlag(strResultCode.toString(),device.getId(),warnSourceId, BJLX.yuanshendianbiao.getValue());
		List<DeviceInfo> deviceInfoList = decisionDao.getDeviceInfo(device.getId(),6,54);

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
	//初始化
	public boolean InitAOV(Integer treeId) {
		//每一个设备都重新初始化,虽然是一条树，但如果这个设备检测到错误，那这课树节点的状态也就会改变啦，所以要初始化
		listAOV.clear();
		listAOV = initTreeDao.getTree(treeId);
	//	ActivityNode node;
		return true;
//		//父节点流转规则
//		//整体是个二叉树
//		if(treeId == 1) {
//			//1.初始化父亲节点 判断电站状态
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(1);
//			node.setnAcitivityID(1);//1，2，3，主键自增
//			node.setnActivityCode(1);//节点的编码，和发票码一样，有意义
//			node.setnParentCount(0);//父母节点数量
//			node.setInEdges(node.getnParentCount());//入边
//			node.setByActivityNodeType(node_type.FINISHED);//活动节点状态，
//			node.setStrNextActivityCodes("2,3");//id是没有意义的，
//			node.setDataAddress("1");//判定业务的标识，也就是数据接口地址，而接口地址返回一个值，用这个值和rule里的Judge_value进行判断
//			node.setWarnFlag(false);//是否产生告警，true为告警，false为不告警
//			//判断 Map 集合对象中是否包含指定的键名。如果 Map 集合中包含指定的键名，则返回 true，否则返回 false。键是活动序号，值是node
//			if (!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//
//			ArrayList<NodeRule> ruleList = new ArrayList<NodeRule>(); //Judge rules 判断依据
//			NodeRule rule = new NodeRule();
//			//规则
//			rule.setCONDITIONS(3); //3代表等于，2代表，1代表
//			rule.setJUDGE_VALUE("1"); //1代表值 如果为1则true 否则0false
//			rule.setGOTO_ACTIVITY(3);
//			ruleList.add(rule);
//			rule = new NodeRule();
//			rule.setCONDITIONS(3); //3代表等于
//			rule.setJUDGE_VALUE("0"); //1代表值 如果为1则true 否则0false
//			rule.setGOTO_ACTIVITY(2);
//			ruleList.add(rule);
//			node.setRuleList(ruleList);
//
//
//			//2.初始化子节点 电站状态正常后逆变器状态
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(1);
//			node.setnAcitivityID(2);
//			node.setnActivityCode(2);
//			node.setnParentCount(1);
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.SLEEP);
//			node.setStrNextActivityCodes("4,5");
//			node.setDataAddress("2");
//			node.setWarnFlag(true);
//			node.setWarnSourceId(1000); //告警配置ID，判断逆变器通讯异常
//			if (!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//
//			ruleList = new ArrayList<NodeRule>(); //Judge rules 判断依据
//			rule = new NodeRule();
//			rule.setCONDITIONS(3); //3代表等于
//			rule.setJUDGE_VALUE("1"); //1代表值 如果为1则true 否则false
//			rule.setGOTO_ACTIVITY(5);
//			ruleList.add(rule);
//			rule = new NodeRule();
//			rule.setCONDITIONS(3); //3代表等于
//			rule.setJUDGE_VALUE("0"); //1代表值 如果为1则true 否则false
//			rule.setGOTO_ACTIVITY(4);
//			ruleList.add(rule);
//			node.setRuleList(ruleList);
//
//
//			//电站状态 通讯异常节点
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(1);
//			node.setnActivityCode(3);
//			node.setnAcitivityID(3);
//			node.setnParentCount(1);
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.SLEEP);
//			node.setStrNextActivityCodes("");
//			node.setWarnFlag(false);
//
//			if (!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//
//
//			//逆变器状态 通讯正常 判断电压电流
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(1);
//			node.setnActivityCode(4);
//			node.setnAcitivityID(4);
//			node.setnParentCount(1);
//			node.setDataAddress("3");
//			node.setWarnFlag(true);
//			node.setWarnSourceId(1001); //判断电压电流离散率 是否破损
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.SLEEP);
//			node.setStrNextActivityCodes("6,7");
//
//			if (!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);//存的是节点码和节点
//
//			ruleList = new ArrayList<NodeRule>(); //Judge rules 判断依据
//			rule = new NodeRule();
//			rule.setCONDITIONS(3); //3代表等于
//			rule.setJUDGE_VALUE("1"); //1代表值 如果为1则true 否则false
//			rule.setGOTO_ACTIVITY(7);
//			ruleList.add(rule);
//			rule = new NodeRule();
//			rule.setCONDITIONS(3); //3代表等于
//			rule.setJUDGE_VALUE("0"); //1代表值 如果为1则true 否则false
//			rule.setGOTO_ACTIVITY(6);
//			ruleList.add(rule);
//			node.setRuleList(ruleList);
//
//
//			//逆变器状态 通讯异常节点
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(1);
//			node.setnActivityCode(5);
//			node.setnAcitivityID(5);
//			node.setnParentCount(1);
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.SLEEP);
//			node.setStrNextActivityCodes("");
//			node.setWarnFlag(false);
//
//			if (!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//
//
//			//电流电压正常 判断分组电站电量
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(1);
//			node.setnActivityCode(6);
//			node.setnAcitivityID(6);
//			node.setnParentCount(1);
//			node.setDataAddress("4");
//			node.setWarnFlag(true);
//			node.setWarnSourceId(1002); //判断分组电站电量   需要清洗
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.SLEEP);
//			node.setStrNextActivityCodes("8,9,10");
//
//			if (!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//
//			ruleList = new ArrayList<NodeRule>(); //Judge rules 判断依据
//			rule = new NodeRule();
//			rule.setCONDITIONS(3); //3代表等于
//			rule.setJUDGE_VALUE("1"); //1代表值 如果为0则正常 1代表 需要现场人员处理2代表需要清洗
//			rule.setGOTO_ACTIVITY(9);
//			ruleList.add(rule);
//			rule = new NodeRule();
//			rule.setCONDITIONS(3); //3代表等于
//			rule.setJUDGE_VALUE("0"); //1代表值 如果为0则正常 1代表 需要现场人员处理2代表需要清洗
//			rule.setGOTO_ACTIVITY(8);
//			ruleList.add(rule);
//			rule = new NodeRule();
//			rule.setCONDITIONS(3); //3代表等于
//			rule.setJUDGE_VALUE("2"); //1代表值 如果为0则正常 1代表 需要现场人员处理2代表需要清洗
//			rule.setGOTO_ACTIVITY(10);
//			ruleList.add(rule);
//			node.setRuleList(ruleList);
//
//
//			//电流电压异常判断
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(1);
//			node.setnActivityCode(7);
//			node.setnAcitivityID(7);
//			node.setnParentCount(1);
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.SLEEP);
//			node.setStrNextActivityCodes("");
//			node.setWarnFlag(false);
//
//			if (!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//
//
//			//分组正常判断 正常
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(1);
//			node.setnActivityCode(8);
//			node.setnAcitivityID(8);
//			node.setnParentCount(1);
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.SLEEP);
//			node.setStrNextActivityCodes("");
//			node.setWarnFlag(false);
//
//			if (!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//
//
//			//分组异常判断 需要运维人员处理
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(1);
//			node.setnActivityCode(9);
//			node.setnAcitivityID(9);
//			node.setnParentCount(1);
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.SLEEP);
//			node.setStrNextActivityCodes("");
//			node.setWarnFlag(false);
//
//			if (!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//
//			//分组异常判断 需要清洗
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(1);
//			node.setnActivityCode(10);
//			node.setnAcitivityID(10);
//			node.setnParentCount(1);
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.SLEEP);
//			node.setStrNextActivityCodes("");
//			node.setWarnFlag(false);
//
//			if (!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//		}
//		else if(treeId == 3){
//			//初始化父亲节点 判断电站状态
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(treeId);
//			node.setnAcitivityID(1);
//			node.setnActivityCode(1);
//			node.setnParentCount(0);
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.FINISHED);
//			node.setStrNextActivityCodes("2,3");
//			node.setDataAddress("6");
//			node.setWarnFlag(false);
//			if(!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//			ArrayList<NodeRule> ruleList = new ArrayList<NodeRule>(); //Judge rules 判断依据
//			NodeRule rule = new NodeRule();
//			rule.setCONDITIONS(3); //3代表等于
//			rule.setJUDGE_VALUE("0"); //1代表值 如果为1则true 否则0false
//			rule.setGOTO_ACTIVITY(2);
//			ruleList.add(rule);
//			rule = new NodeRule();
//			rule.setCONDITIONS(3); //3代表等于
//			rule.setJUDGE_VALUE("1"); //1代表值 如果为1则true 否则0false
//			rule.setGOTO_ACTIVITY(3);
//			ruleList.add(rule);
//			node.setRuleList(ruleList);
//
//
//			//2.初始化子节点 电站状态正常后逆变器状态
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(3);
//			node.setnAcitivityID(2);
//			node.setnActivityCode(2);
//			node.setnParentCount(1);
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.SLEEP);
//			node.setStrNextActivityCodes("");
//			node.setDataAddress("5");
//			node.setWarnFlag(true);
//			node.setWarnSourceId(1001);//配置告警对应的部件参数（warn_source表中warn_id）
//			if (!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//
//
//			//电站状态 通讯异常节点
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(3);
//			node.setnActivityCode(3);
//			node.setnAcitivityID(3);
//			node.setnParentCount(1);
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.SLEEP);
//			node.setStrNextActivityCodes("");
//			node.setWarnFlag(false);
//
//			if (!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//		}
//		else if(treeId == 4){
//			//初始化父亲节点 判断单个电表当前是否存在scada来源的告警。
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(treeId);
//			node.setnAcitivityID(1);
//			node.setnActivityCode(1);
//			node.setnParentCount(0);
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.FINISHED);
//			node.setStrNextActivityCodes("2,3");
//			//判断该电表是否当前存在scada来源的告警
//			node.setDataAddress("9");
//			node.setWarnFlag(false);
//			if(!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//			ArrayList<NodeRule> ruleList = new ArrayList<NodeRule>(); //Judge rules 判断依据
//			NodeRule rule = new NodeRule();
//			rule.setCONDITIONS(3); //3代表等于
//			rule.setJUDGE_VALUE("0"); //1代表值 如果为1则true(表示有来自scada的告警) 否则0false
//			rule.setGOTO_ACTIVITY(2);
//			ruleList.add(rule);
//			rule = new NodeRule();
//			rule.setCONDITIONS(3); //3代表等于
//			rule.setJUDGE_VALUE("1"); //1代表值 如果为1则true 否则0false
//			rule.setGOTO_ACTIVITY(3);
//			ruleList.add(rule);
//			node.setRuleList(ruleList);
//
//
//			//电表当前无告警，判断电表更新时间。
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(treeId);
//			node.setnAcitivityID(2);
//			node.setnActivityCode(2);
//			node.setnParentCount(1);
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.SLEEP);
//			node.setStrNextActivityCodes("4,5");
//			node.setDataAddress("7");//判断电表设备最新数据时间。
//			node.setWarnFlag(true);
//			node.setWarnSourceId(1010);//配置告警对应的部件参数（pm_warn_source表中warn_id）
//			if(!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//			ruleList = new ArrayList<NodeRule>(); //Judge rules 判断依据
//			rule = new NodeRule();
//			rule.setCONDITIONS(3); //3代表等于
//			rule.setJUDGE_VALUE("0"); //1代表值 如果为1则true 否则0false
//			rule.setGOTO_ACTIVITY(4);
//			ruleList.add(rule);
//			rule = new NodeRule();
//			rule.setCONDITIONS(3); //3代表等于
//			rule.setJUDGE_VALUE("1"); //1代表值 如果为1则true 否则0false
//			rule.setGOTO_ACTIVITY(5);
//			ruleList.add(rule);
//			node.setRuleList(ruleList);
//
//			//初始化子节点 电表有scada来源的告警，则不进行任何处理。
//
//
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(treeId);
//			node.setnActivityCode(3);
//			node.setnAcitivityID(3);
//			node.setnParentCount(1);
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.SLEEP);
//			node.setStrNextActivityCodes("");
//			node.setWarnFlag(false);
//
//			if (!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//
//			//4初始化子节点 判断设备状态
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(treeId);
//			node.setnAcitivityID(4);
//			node.setnActivityCode(4);
//			node.setnParentCount(1);
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.SLEEP);
//			node.setStrNextActivityCodes("");
//			node.setDataAddress("8");
//			node.setWarnFlag(true);
//			node.setWarnSourceId(1011);//配置告警对应的部件参数（warn_source表中warn_id）
//			if (!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//
//
//			//电站状态
//			node = new ActivityNode();
//			node.Init();
//			node.setTreeId(treeId);
//			node.setnActivityCode(5);
//			node.setnAcitivityID(5);
//			node.setnParentCount(1);
//			node.setInEdges(node.getnParentCount());
//			node.setByActivityNodeType(node_type.SLEEP);
//			node.setStrNextActivityCodes("");
//			node.setWarnFlag(false);
//
//			if (!listAOV.containsKey(node.getnActivityCode()))
//				listAOV.put(node.getnActivityCode(), node);
//		}
//		return true;
	}

	//供定时器调用
	public void allDecision()
	{
		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		int today_minute = today.get(Calendar.MINUTE);
		int today_sec = today.get(Calendar.SECOND);
		int today_hour = today.get(Calendar.HOUR_OF_DAY);
		// ------------------ 开始执行 ---------------------------
		System.out.println("getPVDecision start"+ today_hour+":"+today_minute+":"+today_sec);

		//先获取所有的电站信息
//		List<Changzhan> list = decisionDao.getContainer(0,0,0);
		List<StationInfo>  stationList = decisionDao.getStationData("", "", "4", 1, 0, 0);
		List<Device> deviceList = new ArrayList<Device>();
		for(StationInfo item:stationList)
		{
//			if(item.getChangZhanID()==686){

			Device dev = new Device();
			dev.setId(item.getChangZhanID());
			dev.setName(item.getStationName());
			deviceList.add(dev);
//			}
		}
		//1.最初决策树；2.****；3.组串电流为0告警，电表日数据离散率比较大告警 By ZhangSC
		BFSAll(deviceList,1);

		// ------------------ 结束执行 ---------------------------
		System.out.println("getDecision end"+ today_hour+":"+today_minute+":"+today_sec);
	}
	public void allDeviceDecision()
	{ 	//获取当前的北京时间，“GMT+8”就是北京时间，
		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
		//Calendar.getInstance();//创一个日期的实例，获取到日期的毫秒显示形式
		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
		int today_minute = today.get(Calendar.MINUTE);//获取当前的分钟
		int today_sec = today.get(Calendar.SECOND);
		int today_hour = today.get(Calendar.HOUR_OF_DAY);
		// ------------------ 开始执行 ---------------------------
		System.out.println("getDecision start"+ today_hour+":"+today_minute+":"+today_sec);

		//先获取所有的设备信息;deviceType：区域类型。
		//		List<Changzhan> list = decisionDao.getContainer(0,0,0);
		//获取设备id为0，设备类型为6，部件类型为54的所有的设备。4
		List<DeviceInfo> deviceInfoList = decisionDao.getDeviceInfo(0,6,54);
		List<Device> deviceList = new ArrayList<Device>();
		for(DeviceInfo item:deviceInfoList)
		{
			//if(item.getChangZhanID()==686){

			Device dev = new Device();
			//新的list只存设备id和设备名字
			dev.setId(item.getDeviceId());
			dev.setName(item.getDeviceName());
			deviceList.add(dev);
			//			}
		}
		//1.最初决策树；2.****；3.电站数据维持不变推电站通讯关闭告警 By ZhangSC
		BFSAll(deviceList,1);

		// ------------------ 结束执行 ---------------------------
		System.out.println("getDecision end"+ today_hour+":"+today_minute+":"+today_sec);
	}

}
