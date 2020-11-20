package cn.zup.iot.timerdecision.service.settings;

/**
 * 部件类型 
 * @author samson
 *
 */
public enum BJLX
{
	/** 
	 建筑/设备/系统/厂站
	 
	*/
	changzhan(6),
	
	/** 
	 通用设备
	 
	*/
	commdev(18),
	
	/** 
	 统计部件
	 
	*/
	staticbujian(19),
	
	/** 
	 虚拟单精模拟量
	 
	*/
	xunidj(20),
	
	/** 
	 虚拟双精模拟量
	 
	*/
	xunisj(21),
	
	/** 
	 虚拟状态量
	 
	*/
	xujiztl(22),

	/** 
	 华为逆变器sun2000
	 
	*/
	huaweinibianqi(26),
	/**
	 * 阳光逆变器
	 */
	yangguangnibianqi(28),

	/**
	 * 源深电表
	 */
	yuanshendianbiao(54),

	/**
	 * 源深电表
	 */
	yuanshennibianqi(55);

	private int intValue;
	private static java.util.HashMap<Integer, BJLX> mappings;
	private synchronized static java.util.HashMap<Integer, BJLX> getMappings()
	{
		if (mappings == null)
		{
			mappings = new java.util.HashMap<Integer, BJLX>();
		}
		return mappings;
	}

	private BJLX(int value)
	{
		intValue = value;
		BJLX.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static BJLX forValue(int value)
	{
		return getMappings().get(value);
	}
}
