package cn.zup.iot.timerdecision.service;

import cn.zup.iot.timerdecision.dao.DecisionDao;
import cn.zup.iot.timerdecision.dao.InitTreeDao;
import cn.zup.iot.timerdecision.model.*;

import javax.xml.crypto.Data;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;



/*
 *
 * 故障决策树的写法
 *
 *   var jsondata = {
                title: "故障决策树1",
                "nodes": {
                    "1": { "name": "电站故障", "type": "node" },
                    "2": { "name": "电流离散率", "type": "node" },
                    "3": { "name": "日电量分析", "type": "node" },
                    "4": { "name": "天气分析", "type": "node" },
                },
                "rules": {
                    "1": {  "from": "1", "to": "2", "conditon": "==","JudgeData":"1" },

                }
            };
 * */

public class ActivityNodeService{

	private DataAdressService dataAddressService = new DataAdressService();
	private InitTreeDao initTreeDao = new InitTreeDao();
	ActivityNode node = new ActivityNode();

	//pop()出每一个节点，并更改节点属性
	public final void ChangeActivityProperty(HashMap<Integer, ActivityNode> listAOV, Device device) {
		//为这个节点设置哪个设备
		node.setDevice(device);
		//取出子节点的字节码
		String[] nextAllBraches = node.getStrNextActivityCodes().split("[,]", -1);
		//判断该节点的NodeType()类型，是sleep，died，还是finished
		switch (node.getByActivityNodeType()) {
			case "SLEEP":
				if (node.getnParentCount() > 0) {
					//如果父分支休眠数量>0, 更新子节点状态为SLEEP
					if (node.getnParentBranchSleepCount() > 0) {
						if (node.getStrNextActivityCodes().length() > 0)
							for (String branch : nextAllBraches)
								listAOV.get(Integer.parseInt(branch)).setByActivityNodeType("SLEEP");
						return;
					}
					//如果父分支已经全部僵死，则转化为僵死状态，
					//并更新自己的子节点父分支僵死数目。
					if (node.getnParentBranchDiedCount() == node.getnParentCount()) {
						node.setByActivityNodeType("DIED");
						if (node.getStrNextActivityCodes().length() > 0) //没有子节点
							for (String branch : nextAllBraches)
								listAOV.get(Integer.parseInt(branch)).setnParentBranchDiedCount(listAOV.get(Integer.parseInt(branch)).getnParentBranchDiedCount() + 1);
					}
					//三 如果父分支节点休眠数量=0，激活数量>0,
					//即：父亲分支数量=父分支激活数量+父分支僵死数量，则跳转到2激活状态
					else if (node.getnParentBranchActiveCount() > 0) {
						node.setByActivityNodeType("FINISHED");
						//执行finish功能
						NodeFinish(listAOV);
					}
				}
				break;
			case "FINISHED"://第一个节点默认为finished
				NodeFinish(listAOV);
				break;
			case "DIED":
				if (node.getStrNextActivityCodes().length() > 0) {
					for (String branch : nextAllBraches) {
						listAOV.get(Integer.parseInt(branch)).setnParentBranchDiedCount(listAOV.get(Integer.parseInt(branch)).getnParentBranchDiedCount() + 1);
					}
				}
				break;
		}
		return;
	}

	///节点结束处理工作
	void NodeFinish(HashMap<Integer, ActivityNode> listAOV) {
		//父亲节点完成，子分支可能会转化为僵死或者激活状态

		//根据设该节点，得到
		DecisionResult result = GetNodeData();
		//result.getActrualValue()，返回0代表正常，返回1代表不正常
		node.setActrualValue(result.getActrualValue());
		//只有在ActrualValue为1的情况下，才有异常信息
		node.setMessage(result.getMessage());
		//下面是取出所有的规则，二叉树有两个规则
		for (NodeRule rule : node.getRuleList()) {
			//如果判断值的长度为0，就结束此次循环，开始下次循环
			if (rule.getJUDGE_VALUE().length() == 0)
				continue;
			double value;//数值型判断条件
			ConditionType conditon = ConditionType.valueOf(rule.getCONDITIONS());
			boolean bConditionFlag = false;
			//根据ActrualValue和判断值的比较，大于等于小于,将bConditionFlag为true
			switch (conditon) {
				case MoreThan:                                       //1.大于
					value = Double.parseDouble(node.getActrualValue());
					if (value > Double.parseDouble(rule.getJUDGE_VALUE()))
						bConditionFlag = true;
					break;
				case MoreThanOrEqual:                                //2.大于且等于
					value = Double.parseDouble(node.getActrualValue());
					if (value >= Double.parseDouble(rule.getJUDGE_VALUE()))
						bConditionFlag = true;
					break;
				case Equal:                                          //3.等于
					value = Double.parseDouble(node.getActrualValue());
					if (value == Double.parseDouble(rule.getJUDGE_VALUE()))
						bConditionFlag = true;
					break;
				case LessThan:                                       //4.小于
					value = Double.parseDouble(node.getActrualValue());
					if (value < Double.parseDouble(rule.getJUDGE_VALUE()))
						bConditionFlag = true;
					break;
				case LessThanOrEqual:                                //5.小于且等于
					value = Double.parseDouble(node.getActrualValue());
					if (value <= Double.parseDouble(rule.getJUDGE_VALUE()))
						bConditionFlag = true;
					break;
				case Contain:                                        //6.包含
					if (node.getActrualValue().contains(rule.getJUDGE_VALUE()))
						bConditionFlag = true;
					break;
				case NoContain:                                      //7.不包含
					if (!node.getActrualValue().contains(rule.getJUDGE_VALUE()))
						bConditionFlag = true;
					break;
			}
			if (bConditionFlag)//如果判据满足，则激活一次，获取该节点并设置父母分支活跃节点+1
				listAOV.get(rule.getGOTO_ACTIVITY()).setnParentBranchActiveCount(listAOV.get(rule.getGOTO_ACTIVITY()).getnParentBranchActiveCount() + 1);
			else//不满足，则死亡一次，获取该节点并设置父母死亡节点节点+1
				listAOV.get(rule.getGOTO_ACTIVITY()).setnParentBranchDiedCount(listAOV.get(rule.getGOTO_ACTIVITY()).getnParentBranchDiedCount() + 1);
		}
	}

	void NodeDie(HashMap<Integer, ActivityNode> listAOV) {
		String[] nextAllBraches = node.getStrNextActivityCodes().split("[,]", -1);
		if (node.getStrNextActivityCodes().length() > 0) {
			for (String branch : nextAllBraches) {
				listAOV.get(Integer.parseInt(branch)).setnParentBranchDiedCount(listAOV.get(Integer.parseInt(branch)).getnParentBranchDiedCount() + 1);
			}
		}
	}

	//返回节点的当前数据
	public DecisionResult GetNodeData(){
		//获取故障信息
		Device device = node.getDevice();//设备信息
		DecisionResult result = new DecisionResult();
		//DataAddress，
		if (Integer.valueOf(node.getDataAddress()) != 0) {
			/**
			 * 进行对应决策
			 * result结果为null，则决策正常，反之，结果不正常
			 * 根据决策树配置页面的Data
			 */
			Map<String,String> dataAdressMap = initTreeDao.getAddress();
			try{
				Class dataAdressClass = dataAddressService.getClass();
				Method method = dataAdressClass.getMethod(dataAdressMap.get(node.getDataAddress()),Device.class, Date.class);
				result = (DecisionResult)method.invoke(dataAddressService,device,new Date());
			}catch (InvocationTargetException exception){
				exception.getStackTrace();
				exception.getMessage();
			}catch (IllegalAccessException exception){
				exception.getStackTrace();
			}catch (NoSuchMethodException exception){
				exception.getStackTrace();
			}
//
//			if (node.getDataAddress().equals("1")) {
//				result = dataAddressService.getStationStatus(tmp, null);
//			} else if (node.getDataAddress().equals("2"))//逆变器通讯异常信息，
//			{
//				result = dataAddressService.getStationDeviceStatus(tmp, new Date());
//			} else if (node.getDataAddress().equals("3"))//获取逆变器采集异常判断，
//			{
//				if (node.getTreeId() == 1) {
//					result = dataAddressService.getPVData(tmp, null);//判断电流直流侧信息
//					if (!result.getActrualValue().equals("1"))
//						result = dataAddressService.getStationShelter(tmp, new Date()); //判断遮挡逻辑
//				} else if (node.getTreeId() == 2)
//					result = dataAddressService.getPVDataByHour(tmp, null);
//			} else if (node.getDataAddress().equals("4"))//获取分组标杆电量对比，
//			{
//				result = dataAddressService.getGroupData(tmp, null);
//			} else if (node.getDataAddress().equals("5"))//获取电站下某种设备的小时数据对比，
//			{
//				result = dataAddressService.getStationDeviceNetState(tmp, new Date());
//			} else if (node.getDataAddress().equals("6")) {
//				result = dataAddressService.getStationNetStatus(tmp, null);
//			} else if (node.getDataAddress().equals("7")) {
//				result = dataAddressService.getDeviceNetState(tmp, new Date());
//			} else if (node.getDataAddress().equals("8")) {
//				result = dataAddressService.getDeviceOnlineState(tmp, new Date());
//			} else if (node.getDataAddress().equals("9"))//判断当前电表是否存在scada告警
//			{
//				result = dataAddressService.getDeviceNowState(tmp, new Date());
//			} else if (node.getDataAddress().equals("10"))//判断电站当前是否有告警
//			{
//				result = dataAddressService.getStationStatus(tmp, new Date());
//			} else if (node.getDataAddress().equals("11"))//判断逆变设备否有组串告警
//			{
//				result = dataAddressService.getPVCurrentData(tmp, new Date());
//			} else if (node.getDataAddress().equals("12"))//判断逆变设备否有组串告警
//			{
//				result = dataAddressService.getDbCvData(tmp, new Date());
//			}
		}
		return result;
	}


}

