package cn.zup.iot.timerdecision.service;

import cn.zup.iot.timerdecision.dao.*;
import cn.zup.iot.timerdecision.model.*;
import cn.zup.iot.timerdecision.service.settings.StationChannelId;

import java.text.SimpleDateFormat;
import java.util.*;

public class PVDecisionEngineService{

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
		java.util.Stack<ActivityNode> stackNodesZeroInEdges = new java.util.Stack<ActivityNode>();
		java.util.Stack<ActivityNode> stackNodesZeroInEdgesTmp = new java.util.Stack<ActivityNode>();
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

			if(!strResult.toString().equals(""))
			{
				//保存诊断结果
				SaveDiagnosis(device,strResult.toString(),strResultCode.toString(), treeId);
				
				//通过电站ID查询告警是否存在，如果不存在则插入告警信息
				//判断节点是否插入告警
				if(warnFlag)
				{
					map.put(strResultCode.toString()+"-"+device.getId(), strResult.toString());
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
		
	}

	/***
	 * 诊断结果处理
	 */
	public void AddWarnRecord(Device device,String strResult,String strResultCode,Integer warnSetId,Integer treeId)
	{
		
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
		List<PmWarnRecord> warnList = scadaWarnPushDao.GetWarnOperateRecordList("4");
		//取出系统时间作为恢复时间
		Date date = new Date();
		Calendar calendarHour = new GregorianCalendar();  
		calendarHour.setTime(date);
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
	
	public boolean InitAOV(Integer treeId) {
		listAOV.clear();
		ActivityNode node;		
		//父节点流转规则
		
		//1.初始化父亲节点 判断电站状态  
		node = new ActivityNode();
		node.Init();
		node.setTreeId(2);
		node.setnAcitivityID(1);
		node.setnActivityCode(1);
		node.setnParentCount(0);
		node.setInEdges(node.getnParentCount()); 
		node.setByActivityNodeType("FINISHED");
		node.setStrNextActivityCodes("2,3");	
		node.setDataAddress("1");
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
		
		
		//2.初始化子节点 电站状态正常后逆变器状态
		node = new ActivityNode();
		node.Init();
		node.setTreeId(2);
		node.setnAcitivityID(2);
		node.setnActivityCode(2);
		node.setnParentCount(1);
		node.setInEdges(node.getnParentCount()); 
		node.setByActivityNodeType("SLEEP");
		node.setStrNextActivityCodes("4,5");		
		node.setDataAddress("2");
		node.setWarnFlag(false);
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
		
		
		//电站状态 通讯异常节点
		node = new ActivityNode();
		node.Init();
		node.setTreeId(2);
		node.setnActivityCode(3); 			
		node.setnAcitivityID(3); 
		node.setnParentCount(1); 
		node.setInEdges(node.getnParentCount());  
		node.setByActivityNodeType("SLEEP");
		node.setStrNextActivityCodes("");	
		node.setWarnFlag(false);	
		
		if(!listAOV.containsKey(node.getnActivityCode()))
			listAOV.put(node.getnActivityCode(), node);
		
		
		//逆变器状态 通讯正常 判断电压电流
		node = new ActivityNode();
		node.Init();
		node.setTreeId(2);
		node.setnActivityCode(4); 			
		node.setnAcitivityID(4); 
		node.setnParentCount(1); 
		node.setDataAddress("3");
		node.setWarnFlag(true);
		node.setWarnSourceId(1001); //判断电压电流离散率 是否破损
		node.setInEdges(node.getnParentCount());  
		node.setByActivityNodeType("SLEEP");
		node.setStrNextActivityCodes("6,7");		
		
		if(!listAOV.containsKey(node.getnActivityCode()))
			listAOV.put(node.getnActivityCode(), node);
		
	  	ruleList=new ArrayList<NodeRule>() ; //Judge rules 判断依据   	
		rule = new NodeRule();
		rule.setCONDITIONS(3); //3代表等于
		rule.setJUDGE_VALUE("1"); //1代表值 如果为1则true 否则false
		rule.setGOTO_ACTIVITY(7);
		ruleList.add(rule);
		rule = new NodeRule();
		rule.setCONDITIONS(3); //3代表等于
		rule.setJUDGE_VALUE("0"); //1代表值 如果为1则true 否则false
		rule.setGOTO_ACTIVITY(6);
		ruleList.add(rule);
		node.setRuleList(ruleList);
		
		
		//逆变器状态 通讯异常节点
		node = new ActivityNode();
		node.Init();
		node.setTreeId(2);
		node.setnActivityCode(5); 			
		node.setnAcitivityID(5); 
		node.setnParentCount(1); 
		node.setInEdges(node.getnParentCount());  
		node.setByActivityNodeType("SLEEP");
		node.setStrNextActivityCodes("");
		node.setWarnFlag(false);		
		
		if(!listAOV.containsKey(node.getnActivityCode()))
			listAOV.put(node.getnActivityCode(), node);

		
		//电流电压正常 判断分组电站电量 
		node = new ActivityNode();
		node.Init();
		node.setTreeId(2);
		node.setnActivityCode(6); 			
		node.setnAcitivityID(6); 
		node.setnParentCount(1); 
		node.setInEdges(node.getnParentCount());  
		node.setByActivityNodeType("SLEEP");
		node.setStrNextActivityCodes("");		
		
		if(!listAOV.containsKey(node.getnActivityCode()))
			listAOV.put(node.getnActivityCode(), node);
		
		
		
		//电流电压异常判断 
		node = new ActivityNode();
		node.Init();
		node.setTreeId(2);
		node.setnActivityCode(7); 			
		node.setnAcitivityID(7); 
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
	public void allPVDecision()
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
		BFSAll(deviceList,2);

        // ------------------ 结束执行 ---------------------------  
		System.out.println("getDecision end"+ today_hour+":"+today_minute+":"+today_sec);
	}
	
}
