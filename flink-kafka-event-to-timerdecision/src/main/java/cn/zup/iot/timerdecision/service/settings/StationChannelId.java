package cn.zup.iot.timerdecision.service.settings;

/**
 * 通道类型 
 * @author samson
 *
 */
public enum StationChannelId
{
	/** 
	 通讯关闭 在scada中的配置
	 
	*/
	CHANNEL_ID(1000);

	private int intValue;
	private static java.util.HashMap<Integer, StationChannelId> mappings;
	private synchronized static java.util.HashMap<Integer, StationChannelId> getMappings()
	{
		if (mappings == null)
		{
			mappings = new java.util.HashMap<Integer, StationChannelId>();
		}
		return mappings;
	}

	private StationChannelId(int value)
	{
		intValue = value;
		StationChannelId.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static StationChannelId forValue(int value)
	{
		return getMappings().get(value);
	}
}
