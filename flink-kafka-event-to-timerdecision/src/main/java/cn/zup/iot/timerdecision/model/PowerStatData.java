package cn.zup.iot.timerdecision.model;


/**
 * 统计电量信息 
 * @author samson
 *
 */
public class PowerStatData {
	
	private Integer id;
	private String name;
	private Integer stateId;  //状态
	private String stateName;  //状态
	private Double dayPower; //日电量
	private Double monthPower; // 月电量
	private Double yearPower;  // 年电量
	private Double totalPower; // 总电量
	private Double dayTheoryPower; // 理论日电量
	private Double monthTheoryPower; // 理论月电量
	private Double yearTheoryPower; // 理论年电量
	private Double totalTheoryPower; // 理论总电量
	private Double capacity; // 额定容量
	private Double equivalentHours; // 等效利用小时数
	private Double activityPower; // 有功功率 
	private String countyName; // 等效利用小时数
	private String townName; // 有功功率 
	private String occurTime;//2017-1-22 15:15:39lixin增加发生时间
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getStateId() {
		return stateId;
	}
	public void setStateId(Integer stateId) {
		this.stateId = stateId;
	}
	public String getStateName() {
		return stateName;
	}
	public void setStateName(String stateName) {
		this.stateName = stateName;
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
	public Double getYearPower() {
		return yearPower;
	}
	public void setYearPower(Double yearPower) {
		this.yearPower = yearPower;
	}
	public Double getTotalPower() {
		return totalPower;
	}
	public void setTotalPower(Double totalPower) {
		this.totalPower = totalPower;
	}
	public Double getDayTheoryPower() {
		return dayTheoryPower;
	}
	public void setDayTheoryPower(Double dayTheoryPower) {
		this.dayTheoryPower = dayTheoryPower;
	}
	public Double getMonthTheoryPower() {
		return monthTheoryPower;
	}
	public void setMonthTheoryPower(Double monthTheoryPower) {
		this.monthTheoryPower = monthTheoryPower;
	}
	public Double getYearTheoryPower() {
		return yearTheoryPower;
	}
	public void setYearTheoryPower(Double yearTheoryPower) {
		this.yearTheoryPower = yearTheoryPower;
	}
	public Double getTotalTheoryPower() {
		return totalTheoryPower;
	}
	public void setTotalTheoryPower(Double totalTheoryPower) {
		this.totalTheoryPower = totalTheoryPower;
	}
	public Double getCapacity() {
		return capacity;
	}
	public void setCapacity(Double capacity) {
		this.capacity = capacity;
	}
	public Double getEquivalentHours() {
		return equivalentHours;
	}
	public void setEquivalentHours(Double equivalentHours) {
		this.equivalentHours = equivalentHours;
	}
	public Double getActivityPower() {
		return activityPower;
	}
	public void setActivityPower(Double activityPower) {
		this.activityPower = activityPower;
	}
	public String getCountyName() {
		return countyName;
	}
	public void setCountyName(String countyName) {
		this.countyName = countyName;
	}
	public String getTownName() {
		return townName;
	}
	public void setTownName(String townName) {
		this.townName = townName;
	}
	public String getOccurTime() {
		return occurTime;
	}
	public void setOccurTime(String occurTime) {
		this.occurTime = occurTime;
	}
	
	
}
