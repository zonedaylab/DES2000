package cn.zup.iot.timerdecision.model;

import java.util.Date;

public class PmDiagnosis {
	private Integer result_Id;//诊断ID
	private Integer device_Id;//设备id
	private Integer device_Type;//设备类型
	private String device_Name;
	private String province_Name;
	private String city_Name;
	private String diagnosis_Result;//诊断结果
	private Date   diagnosis_Time;//诊断时间
	private String real_Reason;//实际原因
	private String reg_Person;//登记人
	private Date   reg_Date;//登记日期
	private String diagnosis_Code;//决策编号
	//以下为冗余字段
	private String diagnosisTimeString;
	private String regDateString;
	private Integer deviceTypeConfig;
	private String deviceTypeString;
	public Integer getResult_Id() {
		return result_Id;
	}
	public void setResult_Id(Integer resultId) {
		result_Id = resultId;
	}
	public Integer getDevice_Id() {
		return device_Id;
	}
	public void setDevice_Id(Integer deviceId) {
		device_Id = deviceId;
	}
	public Integer getDevice_Type() {
		return device_Type;
	}
	public void setDevice_Type(Integer deviceType) {
		device_Type = deviceType;
	}
	public String getDevice_Name() {
		return device_Name;
	}
	public void setDevice_Name(String deviceName) {
		device_Name = deviceName;
	}
	public String getProvince_Name() {
		return province_Name;
	}
	public void setProvince_Name(String provinceName) {
		province_Name = provinceName;
	}
	public String getCity_Name() {
		return city_Name;
	}
	public void setCity_Name(String cityName) {
		city_Name = cityName;
	}
	public String getDiagnosis_Result() {
		return diagnosis_Result;
	}
	public void setDiagnosis_Result(String diagnosisResult) {
		diagnosis_Result = diagnosisResult;
	}
	public Date getDiagnosis_Time() {
		return diagnosis_Time;
	}
	public void setDiagnosis_Time(Date diagnosisTime) {
		diagnosis_Time = diagnosisTime;
	}
	public String getReal_Reason() {
		return real_Reason;
	}
	public void setReal_Reason(String realReason) {
		real_Reason = realReason;
	}
	public String getReg_Person() {
		return reg_Person;
	}
	public void setReg_Person(String regPerson) {
		reg_Person = regPerson;
	}
	public Date getReg_Date() {
		return reg_Date;
	}
	public void setReg_Date(Date regDate) {
		reg_Date = regDate;
	}
	public String getDiagnosis_Code() {
		return diagnosis_Code;
	}
	public void setDiagnosis_Code(String diagnosisCode) {
		diagnosis_Code = diagnosisCode;
	}
	public String getDiagnosisTimeString() {
		return diagnosisTimeString;
	}
	public void setDiagnosisTimeString(String diagnosisTimeString) {
		this.diagnosisTimeString = diagnosisTimeString;
	}
	public String getRegDateString() {
		return regDateString;
	}
	public void setRegDateString(String regDateString) {
		this.regDateString = regDateString;
	}
	public Integer getDeviceTypeConfig() {
		return deviceTypeConfig;
	}
	public void setDeviceTypeConfig(Integer deviceTypeConfig) {
		this.deviceTypeConfig = deviceTypeConfig;
	}
	public String getDeviceTypeString() {
		return deviceTypeString;
	}
	public void setDeviceTypeString(String deviceTypeString) {
		this.deviceTypeString = deviceTypeString;
	}
	
}
