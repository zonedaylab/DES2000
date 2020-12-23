package cn.zup.iot.timercalc.model;

/***
 * 
 * @author samson
 * @date 2019-03-21 16
 * @des 数据实体类
 *
 */
public class DataValue {
	
	long partid;
	
	long time; //数据库查询出的时间转换为long类型
	double value;
	public long getPartid() {
		return partid;
	}
	public void setPartid(long partid) {
		this.partid = partid;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	
}
