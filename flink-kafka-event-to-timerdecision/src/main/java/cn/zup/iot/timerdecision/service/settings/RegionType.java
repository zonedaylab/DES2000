package cn.zup.iot.timerdecision.service.settings;

/**
 * 区域类型 
 * @author samson
 *
 */
public enum RegionType
{
	/** 
	 省份
	 
	*/
	province(1),
	/** 
	 市
	 
	*/
	city(2),
	/** 
	 县
	 
	*/
	county(3),
	
	/** 
	 乡镇
	 
	*/
	town(4),

	/** 
	 村庄
	 
	*/
	village(5),

	/** 
	 逆变器
	 
	*/
	commdev(6);

	private int intValue;
	private static java.util.HashMap<Integer, RegionType> mappings;
	private synchronized static java.util.HashMap<Integer, RegionType> getMappings()
	{
		if (mappings == null)
		{
			mappings = new java.util.HashMap<Integer, RegionType>();
		}
		return mappings;
	}

	private RegionType(int value)
	{
		intValue = value;
		RegionType.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static RegionType forValue(int value)
	{
		return getMappings().get(value);
	}
}
