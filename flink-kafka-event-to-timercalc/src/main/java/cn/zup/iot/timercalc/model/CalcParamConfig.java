package cn.zup.iot.timercalc.model;

/***
 * 
 * @author samson
 * @date 2019-03-21 16
 *
 * @des 计算数据参数实体类
 */
public class CalcParamConfig {
	private Integer calcID;//计算ID
	private Integer paramOrder;//参数序号
	private Integer dataType;//数据类型    1遥测 2电度 3遥信 4能源 
	private Integer deviceType;//部件类型ID
	private Integer deviceID;//部件ID
	private Integer deviceParam;//部件参数ID
	private Integer paramStatisType; //参数统计类型 0 代表无  1代表日统计 2 代表月统计 3 代表年统计 4 代表周统计5代表更新各月某一周的统计值
	private double fCoef; //计算系数
	private double fOffset; //计算偏移
	
	public Integer getCalcID() {
		return calcID;
	}
	public void setCalcID(Integer calcID) {
		this.calcID = calcID;
	}
	public Integer getParamOrder() {
		return paramOrder;
	}
	public void setParamOrder(Integer paramOrder) {
		this.paramOrder = paramOrder;
	}
	public Integer getDataType() {
		return dataType;
	}
	public void setDataType(Integer dataType) {
		this.dataType = dataType;
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
	public Integer getParamStatisType() {
		return paramStatisType;
	}
	public void setParamStatisType(Integer paramStatisType) {
		this.paramStatisType = paramStatisType;
	}
	public double getfCoef() {
		return fCoef;
	}
	public void setfCoef(double fCoef) {
		this.fCoef = fCoef;
	}
	public double getfOffset() {
		return fOffset;
	}
	public void setfOffset(double fOffset) {
		this.fOffset = fOffset;
	}
	
}
