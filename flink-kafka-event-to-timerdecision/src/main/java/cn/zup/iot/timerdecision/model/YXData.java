package cn.zup.iot.timerdecision.model;



/**
 * @author samson
 * 
 */
public class YXData {


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
	private float circuit;
	private float voltage;
	private String time; //数据库查询出的时间转换为long类型
	byte value;
	private int paramDataType;//参数数据性类型 1.遥信 2.遥测越线 3.遥测变动
	
	
	public int getParamDataType() {
		return paramDataType;
	}
	public void setParamDataType(int i) {
		this.paramDataType = i;
	}
	public float getCircuit() {
		return circuit;
	}
	public void setCircuit(float circuit) {
		this.circuit = circuit;
	}
	public float getVoltage() {
		return voltage;
	}
	public void setVoltage(float voltage) {
		this.voltage = voltage;
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
	public byte getValue() {
		return value;
	}
	public void setValue(byte value) {
		this.value = value;
	}
	
}
