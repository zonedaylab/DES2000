package cn.zup.iot.timercalc.model;

import java.util.Date;


public class StationInfo {
	
	private Integer changZhanID;
	private String StationCode;
	private String StationName;
	private String StationLat;
	private String StationLong;
	private Integer ChannelStat;
	private Integer StationType;
	private Integer cityId;
	private Integer provinceId;
	private String cityName;
	private String villageCode; 
	private String provinceName; 
	private float installCapacity; 
	private Integer poorNum; 
	private String buildAdd; 
	private String landType; 
	private float useTax; 
	private float totalInvestment; 
	private String investmentSubject; 
	private String buildSubject; 
	private String fundSource; 
	private String operationFoudSource; 
	private float expectPoorIncome; 
	private float poorFamilyIncome; 
	private Integer poorWay;
	private String progress; 
	private String linkman; 
	private String linkphone;
	private String installAngle;
	private Integer componentWay;
	private float floorArea;
	private float meanAtitude;
	private Date operationTime;
	private float operationPeriod;
	private Integer poorPopulation;
	private Integer pvPoorPopulation;
	private Integer StationStat;
	private String fullName;
	private String memo;
	
	private String townName;//乡镇
	private String countyName;//区县
	
	private Integer batch;//批次
	private String project;//所属项目
	
	public StationInfo(){}
	
	public StationInfo(Integer StationStat, Integer changZhanID, String stationCode,
                       String stationName, String stationLat, String stationLong,
                       Integer channelStat, Integer stationType, Integer cityId,
                       Integer provinceId, String cityName, String villageCode,
                       String provinceName, float installCapacity, Integer poorNum,
                       String buildAdd, String landType, float useTax,
                       float totalInvestment, String investmentSubject,
                       String buildSubject, String fundSource, String operationFoudSource,
                       float expectPoorIncome, float poorFamilyIncome, Integer poorWay,
                       String progress, String linkman, String linkphone, String installAngle,
                       Integer componentWay, float floorArea, float meanAtitude,
                       Date operationTime , float operationPeriod, Integer poorPopulation,
                       Integer pvPoorPopulation, String fullName, String memo) {
		super();
		this.StationStat = StationStat;
		this.changZhanID = changZhanID;
		StationCode = stationCode;
		StationName = stationName;
		StationLat = stationLat;
		StationLong = stationLong;
		ChannelStat = channelStat;
		StationType = stationType;
		this.cityId = cityId;
		this.provinceId = provinceId;
		this.cityName = cityName;
		this.villageCode = villageCode;
		this.provinceName = provinceName;
		this.installCapacity = installCapacity;
		this.poorNum = poorNum;
		this.buildAdd = buildAdd;
		this.landType = landType;
		this.useTax = useTax;
		this.totalInvestment = totalInvestment;
		this.investmentSubject = investmentSubject;
		this.buildSubject = buildSubject;
		this.fundSource = fundSource;
		this.operationFoudSource = operationFoudSource;
		this.expectPoorIncome = expectPoorIncome;
		this.poorFamilyIncome = poorFamilyIncome;
		this.poorWay = poorWay;
		this.progress = progress;
		this.linkman = linkman;
		this.linkphone = linkphone;
		this.installAngle=installAngle;
		this.componentWay=componentWay;
		this.floorArea=floorArea;
		this.meanAtitude=meanAtitude;
		this.operationTime=operationTime;
		this.operationPeriod=operationPeriod;
		this.poorPopulation=poorPopulation;
		this.pvPoorPopulation=pvPoorPopulation;
		this.fullName=fullName;
		this.memo=memo;
	}
	
	public Integer getStationStat() {
		return StationStat;
	}

	public void setStationStat(Integer stationStat) {
		StationStat = stationStat;
	}

	public Integer getChangZhanID() {
		return changZhanID;
	}
	public void setChangZhanID(Integer changZhanID) {
		this.changZhanID = changZhanID;
	}
	public String getStationCode() {
		return StationCode;
	}
	public void setStationCode(String stationCode) {
		StationCode = stationCode;
	}
	public String getStationName() {
		return StationName;
	}
	public void setStationName(String stationName) {
		StationName = stationName;
	}
	public String getStationLat() {
		return StationLat;
	}
	public void setStationLat(String stationLat) {
		StationLat = stationLat;
	}
	public String getStationLong() {
		return StationLong;
	}
	public void setStationLong(String stationLong) {
		StationLong = stationLong;
	}
	public Integer getChannelStat() {
		return ChannelStat;
	}
	public void setChannelStat(Integer channelStat) {
		ChannelStat = channelStat;
	}
	public Integer getStationType() {
		return StationType;
	}
	public void setStationType(Integer stationType) {
		StationType = stationType;
	}
	public Integer getCityId() {
		return cityId;
	}
	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}
	public Integer getProvinceId() {
		return provinceId;
	}
	public void setProvinceId(Integer provinceId) {
		this.provinceId = provinceId;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getVillageCode() {
		return villageCode;
	}
	public void setVillageCode(String villageCode) {
		this.villageCode = villageCode;
	}
	public String getProvinceName() {
		return provinceName;
	}
	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}
	public float getInstallCapacity() {
		return installCapacity;
	}
	public void setInstallCapacity(float installCapacity) {
		this.installCapacity = installCapacity;
	}
	public Integer getPoorNum() {
		return poorNum;
	}
	public void setPoorNum(Integer poorNum) {
		this.poorNum = poorNum;
	}
	public String getBuildAdd() {
		return buildAdd;
	}
	public void setBuildAdd(String buildAdd) {
		this.buildAdd = buildAdd;
	}
	public String getLandType() {
		return landType;
	}
	public void setLandType(String landType) {
		this.landType = landType;
	}
	public float getUseTax() {
		return useTax;
	}
	public void setUseTax(float useTax) {
		this.useTax = useTax;
	}
	public float getTotalInvestment() {
		return totalInvestment;
	}
	public void setTotalInvestment(float totalInvestment) {
		this.totalInvestment = totalInvestment;
	}
	public String getInvestmentSubject() {
		return investmentSubject;
	}
	public void setInvestmentSubject(String investmentSubject) {
		this.investmentSubject = investmentSubject;
	}
	public String getBuildSubject() {
		return buildSubject;
	}
	public void setBuildSubject(String buildSubject) {
		this.buildSubject = buildSubject;
	}
	public String getFundSource() {
		return fundSource;
	}
	public void setFundSource(String fundSource) {
		this.fundSource = fundSource;
	}
	public String getOperationFoudSource() {
		return operationFoudSource;
	}
	public void setOperationFoudSource(String operationFoudSource) {
		this.operationFoudSource = operationFoudSource;
	}
	public float getExpectPoorIncome() {
		return expectPoorIncome;
	}
	public void setExpectPoorIncome(float expectPoorIncome) {
		this.expectPoorIncome = expectPoorIncome;
	}
	public float getPoorFamilyIncome() {
		return poorFamilyIncome;
	}
	public void setPoorFamilyIncome(float poorFamilyIncome) {
		this.poorFamilyIncome = poorFamilyIncome;
	}
	public Integer getPoorWay() {
		return poorWay;
	}
	public void setPoorWay(Integer poorWay) {
		this.poorWay = poorWay;
	}
	public String getProgress() {
		return progress;
	}
	public void setProgress(String progress) {
		this.progress = progress;
	}
	public String getLinkman() {
		return linkman;
	}
	public void setLinkman(String linkman) {
		this.linkman = linkman;
	}
	public String getLinkphone() {
		return linkphone;
	}
	public void setLinkphone(String linkphone) {
		this.linkphone = linkphone;
	} 
	public Integer getComponentWay() {
		return componentWay;
	}
	public void setComponentWay(Integer componentWay) {
		this.componentWay = componentWay;
	}
	public String getInstallAngle() {
		return installAngle;
	}
	public void setInstallAngle(String installAngle) {
		this.installAngle = installAngle;
	}
	public float getFloorArea() {
		return floorArea;
	}
	public void setFloorArea(float floorArea) {
		this.floorArea = floorArea;
	}
	public float getMeanAtitude() {
		return meanAtitude;
	}
	public void setMeanAtitude(float meanAtitude) {
		this.meanAtitude = meanAtitude;
	}
	public Date getOperationTime() {
		return operationTime;
	}
	public void setOperationTime(Date operationTime) {
		this.operationTime = operationTime;
	}
	public float getOperationPeriod() {
		return operationPeriod;
	}
	public void setOperationPeriod(float operationPeriod) {
		this.operationPeriod = operationPeriod;
	}
	public Integer getPoorPopulation() {
		return poorPopulation;
	}
	public void setPoorPopulation(Integer poorPopulation) {
		this.poorPopulation = poorPopulation;
	}
	public Integer getPvPoorPopulation() {
		return pvPoorPopulation;
	}
	public void setPvPoorPopulation(Integer pvPoorPopulation) {
		this.pvPoorPopulation = pvPoorPopulation;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getTownName() {
		return townName;
	}

	public void setTownName(String townName) {
		this.townName = townName;
	}

	public String getCountyName() {
		return countyName;
	}

	public void setCountyName(String countyName) {
		this.countyName = countyName;
	}

	public Integer getBatch() {
		return batch;
	}

	public void setBatch(Integer batch) {
		this.batch = batch;
	}

	public String getProject() {
		return project;
	}

	public void setProject(String project) {
		this.project = project;
	}
	
}
