package cn.zup.iot.timercalc.model;

import java.util.Date;

public class EnergyData {
	private double hourPower;
	private Date dataTime;
	private Integer buJianLeiXingID;
	private Integer buJianCanShuID;
	private Integer buJianID;
	private Integer changZhanID;
	private double value;
	private Integer valueFlag;
	public double getHourPower() {
		return hourPower;
	}
	public void setHourPower(double hourPower) {
		this.hourPower = hourPower;
	}
	public Date getDataTime() {
		return dataTime;
	}
	public void setDataTime(Date dataTime) {
		this.dataTime = dataTime;
	}
	public Integer getBuJianLeiXingID() {
		return buJianLeiXingID;
	}
	public void setBuJianLeiXingID(Integer buJianLeiXingID) {
		this.buJianLeiXingID = buJianLeiXingID;
	}
	public Integer getBuJianCanShuID() {
		return buJianCanShuID;
	}
	public void setBuJianCanShuID(Integer buJianCanShuID) {
		this.buJianCanShuID = buJianCanShuID;
	}
	public Integer getBuJianID() {
		return buJianID;
	}
	public void setBuJianID(Integer buJianID) {
		this.buJianID = buJianID;
	}
	public Integer getChangZhanID() {
		return changZhanID;
	}
	public void setChangZhanID(Integer changZhanID) {
		this.changZhanID = changZhanID;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public Integer getValueFlag() {
		return valueFlag;
	}
	public void setValueFlag(Integer valueFlag) {
		this.valueFlag = valueFlag;
	}
	
	
}
