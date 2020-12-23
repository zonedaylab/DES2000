package cn.zup.iot.timercalc.util;
import java.util.List;

public class HisDataTimeAndValue {
	
	long partid;
	
	long time; //数据库查询出的时间转换为long类型
	float value;
	
	List<Float> listValue;
	
	float value1;
	float value2;
	float value3;
	float value4;
	float value5;
	
	String tmptime;
	
	public List<Float> getListValue() {
		return listValue;
	}
	public void setListValue(List<Float> listValue) {
		this.listValue = listValue;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public float getValue() {
		return value;
	}
	public void setValue(float value) {
		this.value = value;
	}
	public String getTmptime() {
		return tmptime;
	}
	public void setTmptime(String tmptime) {
		this.tmptime = tmptime;
	}
	public float getValue1() {
		return value1;
	}
	public void setValue1(float value1) {
		this.value1 = value1;
	}
	public float getValue2() {
		return value2;
	}
	public void setValue2(float value2) {
		this.value2 = value2;
	}
	public float getValue3() {
		return value3;
	}
	public void setValue3(float value3) {
		this.value3 = value3;
	}
	public float getValue4() {
		return value4;
	}
	public void setValue4(float value4) {
		this.value4 = value4;
	}
	
	
}
