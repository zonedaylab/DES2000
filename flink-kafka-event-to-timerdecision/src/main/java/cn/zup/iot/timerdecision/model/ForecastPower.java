package cn.zup.iot.timerdecision.model;

import java.util.Date;

public class ForecastPower {
	
	private Date riqi;
	private Integer buJianLeiXingID;
	private Integer buJianID;
	private Integer buJianCanShuID;
	private Integer changZhanID;
	private Double value;
	private Double hourPower;//小时发电量
	private Integer valueFlag = 0;
	public Date getRiqi() {
		return riqi;
	}
	public void setRiqi(Date riqi) {
		this.riqi = riqi;
	}
	public Integer getBuJianLeiXingID() {
		return buJianLeiXingID;
	}
	public void setBuJianLeiXingID(Integer buJianLeiXingID) {
		this.buJianLeiXingID = buJianLeiXingID;
	}
	public Integer getBuJianID() {
		return buJianID;
	}
	public void setBuJianID(Integer buJianID) {
		this.buJianID = buJianID;
	}
	public Integer getBuJianCanShuID() {
		return buJianCanShuID;
	}
	public void setBuJianCanShuID(Integer buJianCanShuID) {
		this.buJianCanShuID = buJianCanShuID;
	}
	public Integer getChangZhanID() {
		return changZhanID;
	}
	public void setChangZhanID(Integer changZhanID) {
		this.changZhanID = changZhanID;
	}
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	public Integer getValueFlag() {
		return valueFlag;
	}
	public void setValueFlag(Integer valueFlag) {
		this.valueFlag = valueFlag;
	}
	public Double getHourPower() {
		return hourPower;
	}
	public void setHourPower(Double hourPower) {
		this.hourPower = hourPower;
	}
	
	

}
