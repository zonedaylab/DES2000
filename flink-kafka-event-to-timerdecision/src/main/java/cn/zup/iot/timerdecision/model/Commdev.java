package cn.zup.iot.timerdecision.model;

import java.beans.Transient;

public class Commdev {
	private int ID;
	private int BJLXID;
	private int ChangZhanID;
	private String MingZi;
	private String ShuoMing;
	
	private String Bujianleixingname;
	private String SubNetId;
	
	private Integer dayOnlinePower;//日上网电量
	private Integer monthOnlinePower;//月上网电量
	private Integer yearOnlinePower;//年上网电量
	private Integer totalOnlinePower;//累计上网电量
	
	public Commdev(int iD, int bJLXID, int changZhanID, String mingZi,String subNetId,
			String bujianleixingname,String shuoMing) {
		super();
		ID = iD;
		BJLXID = bJLXID;
		ChangZhanID = changZhanID;
		MingZi = mingZi;
		SubNetId = subNetId;
		Bujianleixingname = bujianleixingname;
		ShuoMing = shuoMing;
	}
	
	public Commdev() {
		super();
	}

	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public int getBJLXID() {
		return BJLXID;
	}
	public void setBJLXID(int bJLXID) {
		BJLXID = bJLXID;
	}
	public int getChangZhanID() {
		return ChangZhanID;
	}
	public void setChangZhanID(int changZhanID) {
		ChangZhanID = changZhanID;
	}
	public String getMingZi() {
		return MingZi;
	}
	public void setMingZi(String mingZi) {
		MingZi = mingZi;
	}
	public String getShuoMing() {
		return ShuoMing;
	}
	public void setShuoMing(String shuoMing) {
		ShuoMing = shuoMing;
	}
	public String getBujianleixingname() {
		return Bujianleixingname;
	}
	public void setBujianleixingname(String bujianleixingname) {
		Bujianleixingname = bujianleixingname;
	}
	@Transient
	public String getSubNetId() {
		return SubNetId;
	}

	public void setSubNetId(String subNetId) {
		SubNetId = subNetId;
	}

	public Integer getDayOnlinePower() {
		return dayOnlinePower;
	}

	public void setDayOnlinePower(Integer dayOnlinePower) {
		this.dayOnlinePower = dayOnlinePower;
	}

	public Integer getMonthOnlinePower() {
		return monthOnlinePower;
	}

	public void setMonthOnlinePower(Integer monthOnlinePower) {
		this.monthOnlinePower = monthOnlinePower;
	}

	public Integer getYearOnlinePower() {
		return yearOnlinePower;
	}

	public void setYearOnlinePower(Integer yearOnlinePower) {
		this.yearOnlinePower = yearOnlinePower;
	}

	public Integer getTotalOnlinePower() {
		return totalOnlinePower;
	}

	public void setTotalOnlinePower(Integer totalOnlinePower) {
		this.totalOnlinePower = totalOnlinePower;
	}

}
