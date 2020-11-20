package cn.zup.iot.timerdecision.service.settings;

/**
 * 告警级别
 * @author samson
 *
 */
public enum WarnLevel
{
	/** 
	 一般告警
	 
	*/
	warn(1),
	
	/** 
	 电站告警
	 
	*/
	stationWarn(2),
	
	/** 
	 外网告警
	 
	*/
	netWarn(3),
	
	/** 
	 通讯关闭
	 
	*/
	transferClose(4),
	
	/** 
	 运维提示
	 
	*/
	stationOperation(5);

	private int intValue;
	private static java.util.HashMap<Integer, WarnLevel> mappings;
	private synchronized static java.util.HashMap<Integer, WarnLevel> getMappings()
	{
		if (mappings == null)
		{
			mappings = new java.util.HashMap<Integer, WarnLevel>();
		}
		return mappings;
	}

	private WarnLevel(int value)
	{
		intValue = value;
		WarnLevel.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static WarnLevel forValue(int value)
	{
		return getMappings().get(value);
	}
}
