package cn.zup.iot.timerdecision.model;

public class PmStationGroup {
	private Integer park_Group_Id;
	
	private String park_Group_Name;
	
	private String station_Ids;
	
	/**		 电站装机容量  	*/
	private Integer capacity;
	
	public Integer getPark_Group_Id() {
		return park_Group_Id;
	}
	public void setPark_Group_Id(Integer parkGroupId) {
		park_Group_Id = parkGroupId;
	}
	public String getPark_Group_Name() {
		return park_Group_Name;
	}
	public void setPark_Group_Name(String parkGroupName) {
		park_Group_Name = parkGroupName;
	}
	public String getStation_Ids() {
		return station_Ids;
	}
	public void setStation_Ids(String stationIds) {
		station_Ids = stationIds;
	}
	public Integer getCapacity() {
		return capacity;
	}
	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}
	
}
