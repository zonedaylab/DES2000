package cn.zup.iot.timerdecision.service.settings;

/**
 * 厂站部件参数
 * @author samson
 *
 */
public enum ChangZhanParam
{
	/** 
	 当前有功功率
	 
	*/
	yougonggonglv(227),
	
	/** 
	 当前无功功率
	 
	*/
	wugonggonglv(228),
	
	/** 
	 当前日发电量
	 
	*/
	daypowers(229),
	
	/** 
	 当前月统计电量值
	 
	*/
	monthpowers(230),
	
	/** 
	 当前年统计电量值
	 
	*/
	yearpowers(231),
	
	/** 
	 当前总发电量值
	 
	*/
	totalpowers(232),
	
	/** 
	 二氧化碳减排量
	 
	*/
	co2(233),
	
	/** 
	 A相电流
	 
	*/
	acurrent(234),
	
	/** 
	 B相电流
	 
	*/
	bcurrent(235),
	
	/** 
	 C相电流
	 
	*/
	ccurrent(236),
	
	/** 
	 输入总功率
	 
	*/
	totalgonglv(237),
	/** 
	 电站运行状态
	 
	*/
	stationrunstate(238),
	
	/** 
	 A相电压
	*/
	aVoltage(239),
	
	/** 
	 B相电压
	*/
	bVoltage(300),
	
	/** 
	 C相电压
	*/
	cVoltage(301),
	/**
	 * 日上网电量
	 */
	dayOnlinePower(239),
	/**
	 * 月上网电量
	 */
	monthOnlinePower(240),
	/**
	 * 总上网电量
	 */
	totalOnlinePower(241),
	/**
	 * 年上网电量
	 */
	yearOnlinePower(1000),
	
	/**
	 * 季度发电量,与冰飞那边没有对应，就是一个数值
	 */
	quarterPower(1001),
	/**
	 * 日用电量
	 */
	dayConsumPower(242),
	/**
	 * 月用电量
	 */
	monthConsumPower(243),
	/**
	 * 年用电量
	 */
	yearConsumPower(244),
	/**
	 * 总用电量
	 */
	totalConsumPower(245),
	
	/**
	 * 标杆电量
	 */
	benchmarkElectricity(255),
	
	/**
	 * 电站离散率
	 */
	dispersionRatio(248),
	/**
	 * 小时离散率
	 */
	dispersionRatio_hour(254),
	/**
	 * 小时标杆电量
	 */
	benchmarkElectricity_hour(225)
	;

	private int intValue;
	private static java.util.HashMap<Integer, ChangZhanParam> mappings;
	private synchronized static java.util.HashMap<Integer, ChangZhanParam> getMappings()
	{
		if (mappings == null)
		{
			mappings = new java.util.HashMap<Integer, ChangZhanParam>();
		}
		return mappings;
	}

	private ChangZhanParam(int value)
	{
		intValue = value;
		ChangZhanParam.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static ChangZhanParam forValue(int value)
	{
		return getMappings().get(value);
	}
}
