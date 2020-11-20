package cn.zup.iot.timerdecision.model;

import java.util.Date;

/**
 * @author samson
 * 离散率实体
 */
public class PVDispersion {
	private String deviceName;
	private Date date;
	private double value;
	private boolean flag;
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public boolean isFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	
}
