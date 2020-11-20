package cn.zup.iot.timerdecision.model;


/**
 * 统计日电量信息 
 * @author samson
 *
 */
public class PowerDayStat {
	
	private Integer stationId;
	private String stationName;
	private Integer townId;
	private String townName;
	private Integer countyId;
	private String countyName;
	private long  time; //数据库查询出的时间转换为long类型
	private String state;  //状态
	private String communicationState;  //通讯状态
	private Double dayPower; //日电量
	private Double monthPower; //日电量
	private Double totalPower; // 总电量
	private Double capacity; // 装机容量
	private String Memo; // 备注
	private String warnResolved;//未处理告警
	

	public Integer getStationId() {
		return stationId;
	}
	public void setStationId(Integer stationId) {
		this.stationId = stationId;
	}
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	public Integer getTownId() {
		return townId;
	}
	public void setTownId(Integer townId) {
		this.townId = townId;
	}
	public String getTownName() {
		return townName;
	}
	public void setTownName(String townName) {
		this.townName = townName;
	}
	public Integer getCountyId() {
		return countyId;
	}
	public void setCountyId(Integer countyId) {
		this.countyId = countyId;
	}
	public String getCountyName() {
		return countyName;
	}
	public void setCountyName(String countyName) {
		this.countyName = countyName;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getCommunicationState() {
		return communicationState;
	}
	public void setCommunicationState(String communicationState) {
		this.communicationState = communicationState;
	}
	public Double getDayPower() {
		return dayPower;
	}
	public void setDayPower(Double dayPower) {
		this.dayPower = dayPower;
	}
	public Double getMonthPower() {
		return monthPower;
	}
	public void setMonthPower(Double monthPower) {
		this.monthPower = monthPower;
	}
	public Double getTotalPower() {
		return totalPower;
	}
	public void setTotalPower(Double totalPower) {
		this.totalPower = totalPower;
	}
	
	public Double getCapacity() {
		return capacity;
	}
	public void setCapacity(Double capacity) {
		this.capacity = capacity;
	}
	public String getMemo() {
		return Memo;
	}
	public void setMemo(String memo) {
		Memo = memo;
	}
	public String getWarnResolved() {
		return warnResolved;
	}
	public void setWarnResolved(String warnResolved) {
		this.warnResolved = warnResolved;
	}
}
