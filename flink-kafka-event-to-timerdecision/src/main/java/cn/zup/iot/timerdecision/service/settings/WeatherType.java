package cn.zup.iot.timerdecision.service.settings;

/**
 * 区域类型 
 * @author samson
 *
 */
public enum WeatherType
{
	/** 
	 晴
	*/
	qing(0),
	/** 
	 多云
	*/
	duoyun(1),
	/** 
	 阴
	*/
	yin(2),
	/** 
	 阵雨
	*/
	zhenyu(3),
	/** 
	 雷阵雨
	*/
	leizhenyu(4),
	/** 
	 雷阵雨伴有冰雹
	*/
	leizhenyubanyoubingbao(5),
	/** 
	 雨夹雪
	*/
	yujiaxue(6),
	/** 
	 小雨
	*/
	xiaoyu(7),
	/** 
	 中雨
	*/
	zhongyu(8),
	/** 
	 大雨
	*/
	dayu(9),
	/** 
	 暴雨
	*/
	baoyu(10),
	/** 
	 大暴雨
	*/
	dabaoyu(11),
	/** 
	 特大暴雨
	*/
	tedabaoyu(12),
	/** 
	 阵雪
	*/
	zhenxue(13),
	/** 
	 小雪
	*/
	xiaoxue(14),
	/** 
	中雪	*/
	zhongxue(15),
	/** 
	 大雪
	*/
	daxue(16),
	/** 
	 暴雪
	*/
	baoxue(17),
	/** 
	雾
	*/
	wu(18),
	/** 
	 冻雨
	*/
	dongyu(19),
	/** 
	 沙尘暴
	*/
	shachenbao(20),
	/** 
	 小雨-中雨
	*/
	xiaoyuzhongyu(21),
	/** 
	 中雨-大雨
	*/
	zhongyudayu(22),
	/** 
	大雨-暴雨
	*/
	dayubaoyu(23),
	/** 
	 暴雨-大暴雨
	*/
	baoyudabaoyu(24),
	/** 
	大暴雨特大暴雨
	*/
	dabaoyutedabaoyu(25),
	/** 
	 小雪-中雪
	*/
	xiaoxuezhongxue(26),
	/** 
	 中雪-大雪
	*/
	zhongxuedaxue(27),
	/** 
	 大雪-暴雪
	*/
	daxuebaoxue(28),
	/** 
	 浮沉
	*/
	fuchen(29),
	/** 
	 扬沙
	*/
	yangsha(30),
	/** 
	 强沙尘暴
	*/
	qiangshachenbao(31),
	/** 
	 霾
	*/
	mai(53);

	private int intValue;
	private static java.util.HashMap<Integer, WeatherType> mappings;
	private synchronized static java.util.HashMap<Integer, WeatherType> getMappings()
	{
		if (mappings == null)
		{
			mappings = new java.util.HashMap<Integer, WeatherType>();
		}
		return mappings;
	}

	private WeatherType(int value)
	{
		intValue = value;
		WeatherType.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static WeatherType forValue(int value)
	{
		return getMappings().get(value);
	}
}
