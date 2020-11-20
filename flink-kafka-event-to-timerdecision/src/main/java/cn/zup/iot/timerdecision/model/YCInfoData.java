package cn.zup.iot.timerdecision.model;



/**
 * @author samson
 * 
 */
public class YCInfoData {

	private int Id;
	private int buJianLeiXing;
	private int XNbuJianLeiXing;
	private int buJianId;
	private int XNbuJianId;
	private int buJianCanShu;
	private int XNbuJianCanShu;
	private int changZhanId;
	private String stationName;
	private String stationCode;
	private String proviceName;
	private String cityName; 
	private String time; //数据库查询出的时间转换为long类型
	private byte lasttimes; // 持续次数
	private int calsStat; // 持续次数
	Double value; //统计值 设置正常值 0 代表正常 1代表不正常
	Double lastValue; //实时值 设置正常值 0 代表正常 1代表不正常
	private String bujiancanshuname; 
	public int getId() {
		return Id;
	}
	public void setId(int id) {
		Id = id;
	}
	public int getBuJianLeiXing() {
		return buJianLeiXing;
	}
	public void setBuJianLeiXing(int buJianLeiXing) {
		this.buJianLeiXing = buJianLeiXing;
	}
	public int getXNbuJianLeiXing() {
		return XNbuJianLeiXing;
	}
	public void setXNbuJianLeiXing(int xNbuJianLeiXing) {
		XNbuJianLeiXing = xNbuJianLeiXing;
	}
	public int getBuJianId() {
		return buJianId;
	}
	public void setBuJianId(int buJianId) {
		this.buJianId = buJianId;
	}
	public int getXNbuJianId() {
		return XNbuJianId;
	}
	public void setXNbuJianId(int xNbuJianId) {
		XNbuJianId = xNbuJianId;
	}
	public int getBuJianCanShu() {
		return buJianCanShu;
	}
	public void setBuJianCanShu(int buJianCanShu) {
		this.buJianCanShu = buJianCanShu;
	}
	public int getXNbuJianCanShu() {
		return XNbuJianCanShu;
	}
	public void setXNbuJianCanShu(int xNbuJianCanShu) {
		XNbuJianCanShu = xNbuJianCanShu;
	}
	public int getChangZhanId() {
		return changZhanId;
	}
	public void setChangZhanId(int changZhanId) {
		this.changZhanId = changZhanId;
	}
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	public String getStationCode() {
		return stationCode;
	}
	public void setStationCode(String stationCode) {
		this.stationCode = stationCode;
	}
	public String getProviceName() {
		return proviceName;
	}
	public void setProviceName(String proviceName) {
		this.proviceName = proviceName;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	public byte getLasttimes() {
		return lasttimes;
	}
	public void setLasttimes(byte lasttimes) {
		this.lasttimes = lasttimes;
	}
	public int getCalsStat() {
		return calsStat;
	}
	public void setCalsStat(int calsStat) {
		this.calsStat = calsStat;
	}
	public Double getLastValue() {
		return lastValue;
	}
	public void setLastValue(Double lastValue) {
		this.lastValue = lastValue;
	}
	public String getBujiancanshuname() {
		return bujiancanshuname;
	}
	public void setBujiancanshuname(String bujiancanshuname) {
		this.bujiancanshuname = bujiancanshuname;
	}
	
}
