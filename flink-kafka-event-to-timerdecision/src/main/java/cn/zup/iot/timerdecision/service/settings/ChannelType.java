package cn.zup.iot.timerdecision.service.settings;

/**
 * 通道类型 
 * @author samson
 *
 */
public enum ChannelType
{
	/** 
	 通道工作正常
	 
	*/
	CHANNEL_NORMAL(0),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_ERROR(1),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_ERROR_RESTORE(2),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_ERRCODE(3),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_ERRCODE_RESTORE(4),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_NODATA(5),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_NODATA_RESTORE(6),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_ONLYSYNWORD(7),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_ONLYSYNWORD_RESTORE(8),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_NOSYNWORD(9),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_NOSYNWORD_RESTORE(10),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_NOANSWER(11),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_NOANSWER_RESTORE(12),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_AUTO_SWITCH(13),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_MANURE_SWITCH(14),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_CCMTOADM_BREAK(15),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_CCMTOADM_RESTORE(16),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_NOUSE(17),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_NOUSE_RESTORE(18),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_LOCK(19),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_UNLOCK(20),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_DIAL_ERROR(21),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_WSHERR(22),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_RWSERR(23),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_RESERVED1(24),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_RTU_GET_ERROR(25),
	
	/** 
	 通道故障 
	 
	*/
	CHANNEL_RTU_GET_ERROR_RESTORE(26);

	private int intValue;
	private static java.util.HashMap<Integer, ChannelType> mappings;
	private synchronized static java.util.HashMap<Integer, ChannelType> getMappings()
	{
		if (mappings == null)
		{
			mappings = new java.util.HashMap<Integer, ChannelType>();
		}
		return mappings;
	}

	private ChannelType(int value)
	{
		intValue = value;
		ChannelType.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static ChannelType forValue(int value)
	{
		return getMappings().get(value);
	}
}
