package cn.zup.iot.timerdecision.model;



public class YCData {


	int partid;
	String partName;
	
	long  time; //数据库查询出的时间转换为long类型
	Double value;
	int valueInt;
	
	
	
	String tmptime;
	public YCData(){}
	
	public YCData(int partid,String partName,Double value,int valueInt){
		this.partid = partid;
		this.partName =partName;
		this.value = value;
		this.valueInt = valueInt;
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
	public String getName() {
		return partName;
	}
	public void setName(String partName) {
		this.partName = partName;
	}

	public int getValueInt() {
		return valueInt;
	}

	public void setValueInt(int valueInt) {
		this.valueInt = valueInt;
	}

	public int getPartid() {
		return partid;
	}

	public void setPartid(int partid) {
		this.partid = partid;
	}
	
	
}
