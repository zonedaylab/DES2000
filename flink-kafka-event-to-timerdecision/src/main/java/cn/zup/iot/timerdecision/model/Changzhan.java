package cn.zup.iot.timerdecision.model;

public class Changzhan {
	int id;
	String name;
	int netid;
	public Changzhan(int id,String name,int netid){
		this.id = id;
		this.name =name;
		this.netid = netid;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNetid() {
		return netid;
	}
	public void setNetid(int netid) {
		this.netid = netid;
	}
	
	
}
