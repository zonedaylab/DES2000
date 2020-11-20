package cn.zup.iot.timerdecision.model;

import java.io.Serializable;
import java.util.ArrayList;

public class ActivityNode implements Serializable {
	private int nAcitivityID; 	//活动ID
	private int nActivityCode; 	//活动序号，对应WF_ACTIVITY--> ACTIVITY_CODE
	private String DataAddress;	//数据接口地址		
  	private ArrayList<cn.zup.iot.timerdecision.model.NodeRule> ruleList=new ArrayList<cn.zup.iot.timerdecision.model.NodeRule>() ; //Judge rules 判断依据
  	
  	private String actrualValue;
  	private String message;
  	private boolean warnFlag; //判断是否存入告警
  	private int warnSourceId;//告警配置ID
  	private int treeId; //节点所属决策树ID
  	
  	
  //private java.util.ArrayList<ActivityNode> listChildren = new java.util.ArrayList<ActivityNode>();
	
//      
//       * 节点状态：0 休眠 1 激活状态  3 完成状态 4僵死状态
//       * 0 休眠状态Sleep。
//       *  
//       *  一 如果父亲节点休眠数量+激活数量>0, 则保持原状态。
//       *  二 如果父亲节点已经全部僵死，父亲节点数量=父节点僵死数量，则转化为僵死状态，
//       *     并更新自己的子节点父亲僵死数目。
//       *  
//       *  三 如果父亲节点完成数量>0,即：父亲节点数量=父节点完成数量+父节
//       *     点僵死数量，则跳转到2激活状态。更新子节点的父亲分支激活数目
//       *  
// 
//       *  
//       * 2 完成状态  
//       * 首先要判断子节点的僵死分支与该节点是否相同，
//       * 不相同则更新子节点的父分支完成数目++（休眠--）
//       *   
//       * 3 僵死状态
//       * 更新子节点的父分支僵死数目（休眠--）
//       * 
//       * 此处的依据包括
//       * 1 激活状态 完成状态不会相互转换
//       * 2只有休眠状态向激活状态的转换
//      
	private String byActivityNodeType;

         
//          父亲节点数量            PARENT_COUNT 
//       
//          父亲分支状态：  1 休眠  2激活  3僵死状态
//          1 父亲分支休眠数量：  PARENT_SLEEP_COUNT   
//          2 父亲分支激活数量    PARENT_ACTIVE_COUNT  
//          3 父亲分支僵死数量：  PARENT_DIED_COUNT
//       
//          PARENT_COUNT=PARENT_SLEEP_COUNT+PARENT_ACTIVE_COUNT+PARENT_DIED_COUNT  
	private int nParentCount;
	private int nParentBranchActiveCount;
	private int nParentBranchDiedCount;
	
	//注：nParentBranchSleepCount不是自变量，而是因变量。最初数目等于父节点的数量
	public final int getnParentBranchSleepCount()
	{
		return nParentCount - nParentBranchDiedCount - nParentBranchActiveCount;
	}
	private String strDiedParentBranchs;
	private String strNextActivityCodes;
//      
//       * 用于遍历所有节点，构建支撑网络
//       * 循环检测邻接表入度为0的活动，并将其压栈，同期入栈中的为并行活动
//       * 初始化时等于父亲节点数量
//       
	private int inEdges; //入度域
	Device device ;
	public final void Init()
	{
		nParentCount = 0;
		nParentBranchDiedCount = 0;
		inEdges = 0;
		byActivityNodeType = "SLEEP";
		strNextActivityCodes = "";
	}
	public int getnAcitivityID() {
		return nAcitivityID;
	}
	public void setnAcitivityID(int nAcitivityID) {
		this.nAcitivityID = nAcitivityID;
	}
	public int getnActivityCode() {
		return nActivityCode;
	}
	public void setnActivityCode(int nActivityCode) {
		this.nActivityCode = nActivityCode;
	}
	public String getDataAddress() {
		return DataAddress;
	}
	public void setDataAddress(String dataAddress) {
		DataAddress = dataAddress;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public ArrayList<cn.zup.iot.timerdecision.model.NodeRule> getRuleList() {
		return ruleList;
	}
	public void setRuleList(ArrayList<cn.zup.iot.timerdecision.model.NodeRule> ruleList) {
		this.ruleList = ruleList;
	}
	public String getActrualValue() {
		return actrualValue;
	}
	public void setActrualValue(String actrualValue) {
		this.actrualValue = actrualValue;
	}
	public String getByActivityNodeType() {
		return byActivityNodeType;
	}
	public void setByActivityNodeType(String byActivityNodeType) {
		this.byActivityNodeType = byActivityNodeType;
	}
	public int getnParentCount() {
		return nParentCount;
	}
	public void setnParentCount(int nParentCount) {
		this.nParentCount = nParentCount;
	}
	public int getnParentBranchActiveCount() {
		return nParentBranchActiveCount;
	}
	public void setnParentBranchActiveCount(int nParentBranchActiveCount) {
		this.nParentBranchActiveCount = nParentBranchActiveCount;
	}
	public int getnParentBranchDiedCount() {
		return nParentBranchDiedCount;
	}
	public void setnParentBranchDiedCount(int nParentBranchDiedCount) {
		this.nParentBranchDiedCount = nParentBranchDiedCount;
	}
	public String getStrDiedParentBranchs() {
		return strDiedParentBranchs;
	}
	public void setStrDiedParentBranchs(String strDiedParentBranchs) {
		this.strDiedParentBranchs = strDiedParentBranchs;
	}
	public String getStrNextActivityCodes() {
		return strNextActivityCodes;
	}
	public void setStrNextActivityCodes(String strNextActivityCodes) {
		this.strNextActivityCodes = strNextActivityCodes;
	}
	public int getInEdges() {
		return inEdges;
	}
	public void setInEdges(int inEdges) {
		this.inEdges = inEdges;
	}
	public Device getDevice() {
		return device;
	}
	public void setDevice(Device device) {
		this.device = device;
	}
	public boolean getWarnFlag() {
		return warnFlag;
	}
	public void setWarnFlag(boolean warnFlag) {
		this.warnFlag = warnFlag;
	}
	public int getWarnSourceId() {
		return warnSourceId;
	}
	public void setWarnSourceId(int warnSourceId) {
		this.warnSourceId = warnSourceId;
	}
	public int getTreeId() {
		return treeId;
	}
	public void setTreeId(int treeId) {
		this.treeId = treeId;
	}
	
}
