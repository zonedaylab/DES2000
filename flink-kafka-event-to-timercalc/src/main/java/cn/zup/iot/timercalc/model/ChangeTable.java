package cn.zup.iot.timercalc.model;

/***
 * 
 * @author samson
 * @date 2019-06-14 
 * @des 换表记录
 *
 */
public class ChangeTable {
	private Integer changeId;//换表ID
	private String changName;//换表名称
	private Integer deviceType;//部件类型ID
	private Integer deviceID;//部件ID
	private Integer deviceParam;//部件参数ID
	private double oldValue; //旧表止码
	private double newValue; //新表起码
	private double calcCoef; //计算系数
	private String memo;//备注
	private Integer changeState;//换表状态
	public Integer getChangeId() {
		return changeId;
	}
	public void setChangeId(Integer changeId) {
		this.changeId = changeId;
	}
	public String getChangName() {
		return changName;
	}
	public void setChangName(String changName) {
		this.changName = changName;
	}
	public Integer getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(Integer deviceType) {
		this.deviceType = deviceType;
	}
	public Integer getDeviceID() {
		return deviceID;
	}
	public void setDeviceID(Integer deviceID) {
		this.deviceID = deviceID;
	}
	public Integer getDeviceParam() {
		return deviceParam;
	}
	public void setDeviceParam(Integer deviceParam) {
		this.deviceParam = deviceParam;
	}
	public double getOldValue() {
		return oldValue;
	}
	public void setOldValue(double oldValue) {
		this.oldValue = oldValue;
	}
	public double getNewValue() {
		return newValue;
	}
	public void setNewValue(double newValue) {
		this.newValue = newValue;
	}
	
	public double getCalcCoef() {
		return calcCoef;
	}
	public void setCalcCoef(double calcCoef) {
		this.calcCoef = calcCoef;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public Integer getChangeState() {
		return changeState;
	}
	public void setChangeState(Integer changeState) {
		this.changeState = changeState;
	}
	
}
