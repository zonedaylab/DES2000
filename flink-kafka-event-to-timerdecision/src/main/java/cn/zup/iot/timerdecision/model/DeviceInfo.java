package cn.zup.iot.timerdecision.model;

/**
 * 通过部件实体
 * @author samson
 *
 */
public class DeviceInfo {
	private Integer deviceId;
	private String deviceName;
	private Integer changZhanID;
	private String StationName;
	private Integer cityId;
	private String cityName;
	private Integer provinceId;
	private String provinceName;
	private String shuoMing;
	private String Memo;
	
	public Integer getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public Integer getChangZhanID() {
		return changZhanID;
	}
	public void setChangZhanID(Integer changZhanID) {
		this.changZhanID = changZhanID;
	}
	public String getStationName() {
		return StationName;
	}
	public void setStationName(String stationName) {
		StationName = stationName;
	}
	public Integer getCityId() {
		return cityId;
	}
	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public Integer getProvinceId() {
		return provinceId;
	}
	public void setProvinceId(Integer provinceId) {
		this.provinceId = provinceId;
	}
	public String getProvinceName() {
		return provinceName;
	}
	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}
	public String getShuoMing() {
		return shuoMing;
	}
	public void setShuoMing(String shuoMing) {
		this.shuoMing = shuoMing;
	}
	public String getMemo() {
		return Memo;
	}
	public void setMemo(String memo) {
		Memo = memo;
	}
}
