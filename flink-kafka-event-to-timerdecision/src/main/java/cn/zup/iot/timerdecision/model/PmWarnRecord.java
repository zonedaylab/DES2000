package cn.zup.iot.timerdecision.model;

import java.util.Date;


public class PmWarnRecord {
	private Integer warn_Record_Id;//告警处理id
	private Integer equipment_Id; //设备id
	private String equipment_Code;//设备编码
	private String equipment_Name;//设备名称
	private Integer asset_Id;//资产id
	private Integer real_Warn_Id;//告警实时库id
	private String warn_Name;//告警名称
	private Integer warn_Set_Id;//告警配置id
	private Integer warn_Level;//告警等级
	private Integer warn_Type;//告警类型
	private Integer station_Id;//电站id
	private Date occur_Time;//发生时间
	private Date occur_Recover;//解除时间
	private Integer warn_Status;//告警状态
	private Integer send_State;//告警消息发送状态
	private String station_Name;//电站名称 
	private String provinceName;//所属区县为了以后任务管理中使用的冗余字段
	private String cityName;//所属乡镇
	private Integer warn_Source; //告警来源 
	private Float voltage; //告警时a相电压
	private Float circuit; //告警时a相电流
	private Integer recover_Way;//恢复方式
	private Integer warn_Recover_Type;//告警恢复状态

	public Integer getWarn_Record_Id() {
		return warn_Record_Id;
	}
	public void setWarn_Record_Id(Integer warnRecordId) {
		warn_Record_Id = warnRecordId;
	}
	public Integer getEquipment_Id() {
		return equipment_Id;
	}
	public void setEquipment_Id(Integer equipmentId) {
		equipment_Id = equipmentId;
	}
	public String getEquipment_Code() {
		return equipment_Code;
	}
	public void setEquipment_Code(String equipmentCode) {
		equipment_Code = equipmentCode;
	}
	public String getEquipment_Name() {
		return equipment_Name;
	}
	public void setEquipment_Name(String equipmentName) {
		equipment_Name = equipmentName;
	}
	public Integer getAsset_Id() {
		return asset_Id;
	}
	public void setAsset_Id(Integer assetId) {
		asset_Id = assetId;
	}
	public Integer getReal_Warn_Id() {
		return real_Warn_Id;
	}
	public void setReal_Warn_Id(Integer realWarnId) {
		real_Warn_Id = realWarnId;
	}
	public String getWarn_Name() {
		return warn_Name;
	}
	public void setWarn_Name(String warnName) {
		warn_Name = warnName;
	}
	public Integer getWarn_Set_Id() {
		return warn_Set_Id;
	}
	public void setWarn_Set_Id(Integer warnSetId) {
		warn_Set_Id = warnSetId;
	}
	public Integer getWarn_Level() {
		return warn_Level;
	}
	public void setWarn_Level(Integer warnLevel) {
		warn_Level = warnLevel;
	}
	public Integer getWarn_Type() {
		return warn_Type;
	}
	public void setWarn_Type(Integer warnType) {
		warn_Type = warnType;
	}
	public Integer getStation_Id() {
		return station_Id;
	}
	public void setStation_Id(Integer stationId) {
		station_Id = stationId;
	}
	public Date getOccur_Time() {
		return occur_Time;
	}
	public void setOccur_Time(Date occurTime) {
		occur_Time = occurTime;
	}
	public Date getOccur_Recover() {
		return occur_Recover;
	}
	public void setOccur_Recover(Date occurRecover) {
		occur_Recover = occurRecover;
	}
	public Integer getWarn_Status() {
		return warn_Status;
	}
	public void setWarn_Status(Integer warnStatus) {
		warn_Status = warnStatus;
	}
	public Integer getSend_State() {
		return send_State;
	}
	public void setSend_State(Integer sendState) {
		send_State = sendState;
	}
	public String getStation_Name() {
		return station_Name;
	}
	public void setStation_Name(String stationName) {
		station_Name = stationName;
	}
	public String getProvinceName() {
		return provinceName;
	}
	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public Integer getWarn_Source() {
		return warn_Source;
	}
	public void setWarn_Source(Integer warnSource) {
		warn_Source = warnSource;
	}

	public Float getVoltage() {
		return voltage;
	}
	public void setVoltage(Float voltage) {
		this.voltage = voltage;
	}
	public Float getCircuit() {
		return circuit;
	}
	public void setCircuit(Float circuit) {
		this.circuit = circuit;
	}
	public Integer getRecover_Way() {
		return recover_Way;
	}
	public void setRecover_Way(Integer recoverWay) {
		recover_Way = recoverWay;
	}

	public Integer getWarn_Recover_Type() {
		return warn_Recover_Type;
	}
	public void setWarn_Recover_Type(Integer warnRecoverType) {
		warn_Recover_Type = warnRecoverType;
	}
}
