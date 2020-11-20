package cn.zup.iot.timerdecision.service.settings;

/**
 * 信息类型 
 * @author samson
 *
 */
public enum HuaWeiSunParam
{
	/** 
	 逆变器额定容量
	 
	*/
	rongliang(1),
	
	/** 
	 PV1输入电压
	 
	*/
	pv1dianya(3),
	
	/** 
	 PV1输入电流
	 
	*/
	pv1dianliu(4), 
	
	/** 
	 PV2输入电压
	 
	*/
	pv2dianya(5),
	
	/** 
	 PV2输入电流
	 
	*/
	pv2dianliu(6), 
	
	/** 
	 PV3输入电压
	 
	*/
	pv3dianya(7),
	/** 
	 PV1输入电流
	*/
	pv3dianliu(8), 
	/** 
	 PV4输入电压
	*/
	pv4dianya(9),
	/** 
	 PV4输入电流
	*/
	pv4dianliu(10), 
	
	/** 
	 PV5输入电压
	 
	*/
	pv5dianya(11),
	
	/** 
	 PV5输入电流
	 
	*/
	pv5dianliu(12), 
	
	/** 
	 PV6输入电压
	*/
	pv6dianya(13),
	/** 
	 PV6输入电流
	*/
	pv6dianliu(14), 
	
	/** 
	 电网AB相电压
	 
	*/
	abdianya(15),
	
	/** 
	 电网BC相电压
	 
	*/
	bcdianya(16),
	
	/** 
	 电网CA相电压
	 
	*/
	cadianya(17),
	
	/** 
	 电网A相电压
	 
	*/
	adianya(18),
	
	/** 
	 电网B相电压
	 
	*/
	bdianya(19),
	
	/** 
	 电网C相电压
	 
	*/
	cdianya(20),
	/** 
	 电网A相电流
	 
	*/
	adianliu(21),
	
	/** 
	 电网B相电流
	 
	*/
	bdianliu(22),
	
	/** 
	 电网C相电流
	 
	*/
	cdianliu(23),
	
	/** 
	 电网频率
	 
	*/
	dianwangpinlv(24),
	
	/** 
	 功率因数
	 
	*/
	gonglvyinshu(25),
	
	/** 
	 逆变器效率
	 
	*/
	nibianqixiaolv(26),
	
	/** 
	 机内温度
	 
	*/
	jineiwendu(27),
	
	/** 
	 逆变器状态
	 
	*/
	nibianqizhuangtai(28),
	
	/** 
	 当天峰值有功功率
	 
	*/
	fengzhiyougonggonglv(29),
	
	/** 
	 有功功率
	 
	*/
	yougonggonglv(30),
	
	/** 
	 无功功率
	 
	*/
	wugonggonglv(31),
	
	/** 
	 输入总功率
	 
	*/
	zonggonglv(32),
	
	/** 
	 当前发电量统计时间
	 
	*/
	tongjishijian(33),
	
	/** 
	 逆变器开机时间
	 
	*/
	nibianqikaijishijian(34),
	
	/** 
	 逆变器关机时间
	 
	*/
	nibianqiguanjishijian(35),
	
	/** 
	 二氧化碳减排量
	 
	*/
	eryanghuatan(96),
	
	/** 
	 当前小时发电量
	 
	*/
	hourpowers(97),
	
	/** 
	 当前日发电量
	 
	*/
	daypowers(98),
	
	/** 
	 当前月发电量
	 
	*/
	monthpowers(99),
	
	/** 
	 当前年发电量
	 
	*/
	yearpowers(100),
	
	/** 
	总发电量
	 
	*/
	totalpowers(101);
	private int intValue;
	private static java.util.HashMap<Integer, HuaWeiSunParam> mappings;
	private synchronized static java.util.HashMap<Integer, HuaWeiSunParam> getMappings()
	{
		if (mappings == null)
		{
			mappings = new java.util.HashMap<Integer, HuaWeiSunParam>();
		}
		return mappings;
	}

	private HuaWeiSunParam(int value)
	{
		intValue = value;
		HuaWeiSunParam.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static HuaWeiSunParam forValue(int value)
	{
		return getMappings().get(value);
	}
}
