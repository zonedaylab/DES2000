package cn.zup.iot.timerdecision.service.settings;

/**
 * 顶级区域
 * mxf
 * 2017年5月17日14:52:11
 *
 */
public enum TopRegion
{
	/**
	 * 济南市
	 */
	jinan(284),
	/**
	 * 杭州市
	 */
	hangzhou(285),
	/**
	 * 洪楼园区电站
	 */
	hongjialou(526);

	private int intValue;
	private static java.util.HashMap<Integer, TopRegion> mappings;
	private synchronized static java.util.HashMap<Integer, TopRegion> getMappings()
	{
		if (mappings == null)
		{
			mappings = new java.util.HashMap<Integer, TopRegion>();
		}
		return mappings;
	}

	private TopRegion(int value)
	{
		intValue = value;
		TopRegion.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static TopRegion forValue(int value)
	{
		return getMappings().get(value);
	}
}
