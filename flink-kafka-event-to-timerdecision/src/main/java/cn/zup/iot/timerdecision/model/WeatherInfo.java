package cn.zup.iot.timerdecision.model;

import java.util.Date;

public class WeatherInfo {
	private Integer weather_Id;//天气id
	private Date time;//时间
    private String city_Code; //城市编码
    private String city_Name; //城市名
    private Float temperature;//温度
    private Float humidity;//湿度
    private String wind;//风力
    private Integer weatherTypeFa;//fa 天气标识
    private Integer weatherTypeFb;//fa 天气标识
    private String weatherTypeName;//天气信息描述
    
    private String startTime;  //查询开始时间
    private String endTime;    //查询结束时间
    
	public Integer getWeather_Id() {
		return weather_Id;
	}
	public void setWeather_Id(Integer weatherId) {
		weather_Id = weatherId;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public String getCity_Code() {
		return city_Code;
	}
	public void setCity_Code(String cityCode) {
		city_Code = cityCode;
	}
	public String getCity_Name() {
		return city_Name;
	}
	public void setCity_Name(String cityName) {
		city_Name = cityName;
	}
	public Float getTemperature() {
		return temperature;
	}
	public void setTemperature(Float temperature) {
		this.temperature = temperature;
	}
	public Float getHumidity() {
		return humidity;
	}
	public void setHumidity(Float humidity) {
		this.humidity = humidity;
	}
	public String getWind() {
		return wind;
	}
	public void setWind(String wind) {
		this.wind = wind;
	}
	public Integer getWeatherTypeFa() {
		return weatherTypeFa;
	}
	public void setWeatherTypeFa(Integer weatherTypeFa) {
		this.weatherTypeFa = weatherTypeFa;
	}
	public Integer getWeatherTypeFb() {
		return weatherTypeFb;
	}
	public void setWeatherTypeFb(Integer weatherTypeFb) {
		this.weatherTypeFb = weatherTypeFb;
	}
	public String getWeatherTypeName() {
		return weatherTypeName;
	}
	public void setWeatherTypeName(String weatherTypeName) {
		this.weatherTypeName = weatherTypeName;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
    
}
