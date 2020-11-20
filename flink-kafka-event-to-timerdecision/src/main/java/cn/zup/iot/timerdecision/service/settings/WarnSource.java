package cn.zup.iot.timerdecision.service.settings;

/**
 * 告警级别
 * @author samson
 *
 */
public enum WarnSource
{
	/** 
	 scada
	 
	*/
	scada(1),
	
	/** 
	 运维提示
	 
	*/
	handOperate(2);

	private int intValue;
	private static java.util.HashMap<Integer, WarnSource> mappings;
	private synchronized static java.util.HashMap<Integer, WarnSource> getMappings()
	{
		if (mappings == null)
		{
			mappings = new java.util.HashMap<Integer, WarnSource>();
		}
		return mappings;
	}

	private WarnSource(int value)
	{
		intValue = value;
		WarnSource.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static WarnSource forValue(int value)
	{
		return getMappings().get(value);
	}
}
