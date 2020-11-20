package cn.zup.iot.timerdecision.model;

public class BuJianParam {
	String bjlx;
	String bjcs;
	String bjid;
	String czid;
	String sanduanshi;
	boolean rs; //用于转换ct递归调用
	String value;
	//曲线展示标题
	String pname;
	
	String unit; //曲线单位
	String name; //echats 图标题
	
	
	public String getPname() {
		return pname;
	}

	public void setPname(String pname) {
		this.pname = pname;
	}

	public BuJianParam() {
	}

	public BuJianParam(String bjlx, String bjcs, String bjid, String czid) {
		this.bjlx =bjlx;
		this.bjcs =bjcs;
		this.bjid =bjid;
		this.czid =czid;
		this.sanduanshi = bjlx + "_" + bjcs + "_" + bjid;
	}
	
	public BuJianParam(String bjlx, String bjcs, String bjid) {
		this.bjlx =bjlx;
		this.bjcs =bjcs;
		this.bjid =bjid;
		this.sanduanshi = bjlx + "_" + bjcs + "_" + bjid;
	}

	public String getBjlx() {
		return bjlx;
	}

	public void setBjlx(String bjlx) {
		this.bjlx = bjlx;
	}

	public String getBjcs() {
		return bjcs;
	}

	public void setBjcs(String bjcs) {
		this.bjcs = bjcs;
	}

	public String getBjid() {
		return bjid;
	}

	public void setBjid(String bjid) {
		this.bjid = bjid;
	}

	public String getCzid() {
		return czid;
	}

	public void setCzid(String czid) {
		this.czid = czid;
	}

	public String getSanduanshi() {
		return sanduanshi;
	}

	public void setSanduanshi(String sanduanshi) {
		this.sanduanshi = sanduanshi;
	}

	public boolean isRs() {
		return rs;
	}

	public void setRs(boolean rs) {
		this.rs = rs;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
