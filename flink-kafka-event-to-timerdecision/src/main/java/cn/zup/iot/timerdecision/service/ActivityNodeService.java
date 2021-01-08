package cn.zup.iot.timerdecision.service;

import cn.zup.iot.timerdecision.dao.DecisionTreePmsDao;
import cn.zup.iot.timerdecision.model.*;

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
	private DecisionTreePmsDao decisionTreePmsDao = new DecisionTreePmsDao();
	ActivityNode node = new ActivityNode();

	/**
	 * 判断该节点的状态，如果该节点的状态是sleep的，再判断该节点的父母节点死亡数量(nParentBranchDiedCount)，如果和父母节点数量相同，则将状态置为died
	 * 如果该节点的父母节点活跃数量(nParentBranchActiveCount)大于0,则将状态置为finished
	 * 因为决策路线只执行finished的节点
	 * @author shishanli
	 * @date 2021年1月6日00:04:35
	 * @param listAOV
	 * @param device
	 */
	public final void ChangeActivityProperty(HashMap<Integer, ActivityNode> listAOV, Device device) {
		//为节点设备的属性值初始化，用于对该设备执行具体业务方法
		node.setDevice(device);
		//取出子节点的字节码
		String[] nextAllBraches = node.getStrNextActivityCodes().split("[,]", -1);
		//判断该节点的NodeType()类型，是sleep，died，还是finished，主要是找到找到他的子节点，并且把将要走的子节点的类型定义为finished
		switch (node.getByActivityNodeType()) {
			case "SLEEP":
				if (node.getnParentCount() > 0) {
					//如果父分支休眠数量>0, 更新子节点状态为SLEEP，这个后面没对nParentBranchSleepCount初始化，所以不运行这个程序
					if (node.getnParentBranchSleepCount() > 0) {
						if (node.getStrNextActivityCodes().length() > 0) {
							for (String branch : nextAllBraches) {
								listAOV.get(Integer.parseInt(branch)).setByActivityNodeType("SLEEP");
							}
						}
						return;
					}
					//如果父分支已经全部僵死，则转化为僵死状态，
					//并更新自己的子节点父分支僵死数目。
					if (node.getnParentBranchDiedCount() == node.getnParentCount()) {
						node.setByActivityNodeType("DIED");
						if (node.getStrNextActivityCodes().length() > 0) //没有子节点
						{
							for (String branch : nextAllBraches) {
								listAOV.get(Integer.parseInt(branch)).setnParentBranchDiedCount(listAOV.get(Integer.parseInt(branch)).getnParentBranchDiedCount() + 1);
							}
						}
					}
					//三 如果父分支节点休眠数量=0，激活数量>0,
					//即：父亲分支数量=父分支激活数量+父分支僵死数量，则跳转到2激活状态
					else if (node.getnParentBranchActiveCount() > 0) {
						node.setByActivityNodeType("FINISHED");
						//执行finish功能
						nodeFinish(listAOV);
					}
				}
				break;
			case "FINISHED"://第一个节点默认为finished
				nodeFinish(listAOV);
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

	/**
	 * 对类型为finished的节点执行决策业务方法，并将返回值赋值给该节点，并根据返回的ActrualValue、判断条件、判断依据找到流转节点
	 * 如果找到流转节点，则对该节点的nParentBranchActiveCount（父母活跃节点数量）+1
	 * 如果找不到流转节点，则对该节点的nParentBranchDiedCount（父母死亡节点数量）+1
	 * 因为前面是对所有的节点进行遍历，而决策路线只走nParentBranchActiveCount>0的，nParentBranchDiedCount=父母节点数量的不走
	 * @author shishanli
	 * @date 2021年1月5日23:57:16
	 * @param listAOV
	 */
	void nodeFinish(HashMap<Integer, ActivityNode> listAOV) {
		//父亲节点完成，子分支可能会转化为僵死或者激活状态

		//返回该节点决策业务方法的执行结果
		DecisionResult result = getNodeData();
		//actrualValue，返回0代表正常，返回1代表不正常
		node.setActrualValue(result.getActrualValue());
		//只有在actrualValue为1的情况下，才有异常信息
		node.setMessage(result.getMessage());
		//下面是取出该节点所有规则信息
		for (NodeRule rule : node.getRuleList()) {
			//如果判断值的长度为0（为空），就结束此次循环，开始下次循环
			if (rule.getJUDGE_VALUE().length() == 0) {
				continue;
			}
			double value;//数值型判断条件
			ConditionType conditon = ConditionType.valueOf(rule.getCONDITIONS());
			boolean bConditionFlag = false;
			//根据ActrualValue和判断值的比较，大于等于小于,将bConditionFlag为true
			switch (conditon) {
				case MoreThan:                                       //1.大于
					value = Double.parseDouble(node.getActrualValue());
					if (value > Double.parseDouble(rule.getJUDGE_VALUE())) {
						bConditionFlag = true;
					}
					break;
				case MoreThanOrEqual:                                //2.大于且等于
					value = Double.parseDouble(node.getActrualValue());
					if (value >= Double.parseDouble(rule.getJUDGE_VALUE())) {
						bConditionFlag = true;
					}
					break;
				case Equal:                                          //3.等于
					value = Double.parseDouble(node.getActrualValue());
					if (value == Double.parseDouble(rule.getJUDGE_VALUE())) {
						bConditionFlag = true;
					}
					break;
				case LessThan:                                       //4.小于
					value = Double.parseDouble(node.getActrualValue());
					if (value < Double.parseDouble(rule.getJUDGE_VALUE())) {
						bConditionFlag = true;
					}
					break;
				case LessThanOrEqual:                                //5.小于且等于
					value = Double.parseDouble(node.getActrualValue());
					if (value <= Double.parseDouble(rule.getJUDGE_VALUE())) {
						bConditionFlag = true;
					}
					break;
				case Contain:                                        //6.包含
					if (node.getActrualValue().contains(rule.getJUDGE_VALUE())) {
						bConditionFlag = true;
					}
					break;
				case NoContain:                                      //7.不包含
					if (!node.getActrualValue().contains(rule.getJUDGE_VALUE())) {
						bConditionFlag = true;
					}
					break;
			}
			if (bConditionFlag)//如果判据满足，则激活一次，获取该节点并设置父母分支活跃节点+1
			{
				listAOV.get(rule.getGOTO_ACTIVITY()).setnParentBranchActiveCount(listAOV.get(rule.getGOTO_ACTIVITY()).getnParentBranchActiveCount() + 1);
			} else//不满足，则死亡一次，获取该节点并设置父母死亡节点节点+1
			{
				listAOV.get(rule.getGOTO_ACTIVITY()).setnParentBranchDiedCount(listAOV.get(rule.getGOTO_ACTIVITY()).getnParentBranchDiedCount() + 1);
			}
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

	/**
	 * 判断该节点有无业务方法，如果无就返回空值，有就对节点执行具体业务方法（决策），看是否出现问题，并返回执行的结果
	 * @author shishanli
	 * @date 2021年1月5日22:20:53
	 * @return DecisionResult ActrualValue属性为0时代表正常，为1时代表不正常，如果为1时，Message属性会含有故障信息
	 */
	public DecisionResult getNodeData(){
		//获取节点的设备
		Device device = node.getDevice();
		DecisionResult result = new DecisionResult();
		//如果该节点没有业务方法就不执行，直接返回空置，如果有就根据节点业务方法的地址进行调用
		if (Integer.valueOf(node.getDataAddress()) != 0) {
			Map<String,String> dataAdressMap = decisionTreePmsDao.getAddress();
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

