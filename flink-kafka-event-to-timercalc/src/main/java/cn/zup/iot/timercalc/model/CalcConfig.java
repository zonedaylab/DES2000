package cn.zup.iot.timercalc.model;

/***
 * 
 * @author samson
 * @date 2019-03-21 16
 * @des 计算数据实体类
 *
 */
public class CalcConfig {
	private Integer calcID;//计算ID
	private String calcName;//计算名称
	private Integer formula;//公式 在config表中配置（1求和，2求差，3求乘，4相除，5总累加，6求平均）
	private Integer deviceType;//部件类型ID
	private Integer deviceID;//部件ID
	private Integer deviceParam;//部件参数ID
	private Integer changZhanID;//厂站ID
	private Integer calcInterval;//计算周期   0代表1分钟   1代表5分钟  2代表15分钟  3代表1小时  4代表日  5代表月  6代表年  7代表周统计
	private Integer targetTable;//存库（1yaoce  2kwh   3byte   4energy）
	private String expression;//特殊公式表达式
	private Integer startType;//启动类型  0不启动  1启动
	private double maxPositiveValue; //正数有效上限
	private double minPositiveValue; //正数有效下限
	private double maxNegativeValue; //负数有效上限
	private double minNegativeValue; //负数有效下限
	private Integer calcIntervalTime;//参数统计时间点 如8 代表8点统计
	private Integer weekFlag;//厂站ID
	public Integer getCalcID() {
		return calcID;
	}
	public void setCalcID(Integer calcID) {
		this.calcID = calcID;
	}
	public String getCalcName() {
		return calcName;
	}
	public void setCalcName(String calcName) {
		this.calcName = calcName;
	}
	public Integer getFormula() {
		return formula;
	}
	public void setFormula(Integer formula) {
		this.formula = formula;
	}
	public Integer getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(Integer deviceType) {
		this.deviceType = deviceType;
	}
	public Integer getDeviceID() {
		return deviceID;
	}
	public void setDeviceID(Integer deviceID) {
		this.deviceID = deviceID;
	}
	public Integer getDeviceParam() {
		return deviceParam;
	}
	public void setDeviceParam(Integer deviceParam) {
		this.deviceParam = deviceParam;
	}
	public Integer getChangZhanID() {
		return changZhanID;
	}
	public void setChangZhanID(Integer changZhanID) {
		this.changZhanID = changZhanID;
	}
	public Integer getCalcInterval() {
		return calcInterval;
	}
	public void setCalcInterval(Integer calcInterval) {
		this.calcInterval = calcInterval;
	}
	public Integer getStartType() {
		return startType;
	}
	public void setStartType(Integer startType) {
		this.startType = startType;
	}
	public double getMaxPositiveValue() {
		return maxPositiveValue;
	}
	public void setMaxPositiveValue(double maxPositiveValue) {
		this.maxPositiveValue = maxPositiveValue;
	}
	public double getMinPositiveValue() {
		return minPositiveValue;
	}
	public void setMinPositiveValue(double minPositiveValue) {
		this.minPositiveValue = minPositiveValue;
	}
	public double getMaxNegativeValue() {
		return maxNegativeValue;
	}
	public void setMaxNegativeValue(double maxNegativeValue) {
		this.maxNegativeValue = maxNegativeValue;
	}
	public double getMinNegativeValue() {
		return minNegativeValue;
	}
	public void setMinNegativeValue(double minNegativeValue) {
		this.minNegativeValue = minNegativeValue;
	}
	public Integer getCalcIntervalTime() {
		return calcIntervalTime;
	}
	public void setCalcIntervalTime(Integer calcIntervalTime) {
		this.calcIntervalTime = calcIntervalTime;
	}
	public Integer getWeekFlag() {
		return weekFlag;
	}
	public void setWeekFlag(Integer weekFlag) {
		this.weekFlag = weekFlag;
	}

	public Integer getTargetTable() {
		return targetTable;
	}

	public void setTargetTable(Integer targetTable) {
		this.targetTable = targetTable;
	}
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}




}
