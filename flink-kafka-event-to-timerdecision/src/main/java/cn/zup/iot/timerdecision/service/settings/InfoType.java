package cn.zup.iot.timerdecision.service.settings;

/**
 * 信息类型 
 * @author samson
 *
 */
public enum InfoType
{
	/** 
	 遥测
	 
	*/
	yaoce(1),
	
	/** 
	 电度
	 
	*/
	diandu(2),

	/** 
	 遥信
	 
	*/
	yaoxin(3);

	private int intValue;
	private static java.util.HashMap<Integer, InfoType> mappings;
	private synchronized static java.util.HashMap<Integer, InfoType> getMappings()
	{
		if (mappings == null)
		{
			mappings = new java.util.HashMap<Integer, InfoType>();
		}
		return mappings;
	}

	private InfoType(int value)
	{
		intValue = value;
		InfoType.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static InfoType forValue(int value)
	{
		return getMappings().get(value);
	}
}
