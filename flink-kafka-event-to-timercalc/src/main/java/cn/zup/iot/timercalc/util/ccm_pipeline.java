package cn.zup.iot.timercalc.util;

//====================================================================
//前置模块和其他模块使用永久管道通讯的数据结构定义
//====================================================================
public  class ccm_pipeline {
	
	//常量定义
	static int  SB_RESERVE_BYTE=		16;
	static int  NET_MAX_INF	=			64;
	static int CCM_ADM_BUF_LEN=		8196;
	static int  MAX_TQ_NUM	=			8;		// 同期遥控数据个数

	//质量码定义，使用位标志
	public interface QUALITY_TYPE{
	
		public int  QUALITY_DATA_NORMAL		= 0x00;		// 数据正常
		public int  QUALITY_DATA_NOTINIT		= 0x01;		// 数据未初始化
		public int  QUALITY_DATA_NOCHANGE	= 0x02;		// 数据不变化
		public int  QUALITY_DATA_NOVALID		= 0x04;		// 数据无效标志
		public int  QUALITY_DATA_UPLIMIT		= 0x08;		// 数据越有效上限
		public int  QUALITY_DATA_DOWNLIMIT	= 0x10;		// 数据越有效下限
		public int  QUALITY_RTUTUI			= 0x20;		// RTU退出
		public int  QUALITY_DATA_NOFRESH		= 0x40;		// 数据不刷新
		public int  QUALITY_DATA_NOSURE		= 0x80;		// 可疑数据，不确定
	}

	public static class s_data_id {
		public byte  byBJType;		// 部件类型
		public int   wBJID;			// 部件ID
		public byte  byBJParam;		// 部件参数
		public short wReserved;		// 保留
		public int wCZID;			// 厂站ID
	}

//CCM 传输 ADM 的字节（遥信）数据结构
public static class  s_bytedata_com {
	public s_data_id	dataid;			// 数据 ID
	public byte		byValue;		// 值
	public int	qtQualityCode;		// 质量码
	public byte		byDataSourceID;		// 数据源标识
	 public long		DateTime;		// 时间
}

//CCM 传输 ADM 的短整型数据结构
public static class  s_shortdata_com {
	public s_data_id	dataid;			// 数据 ID
	public int		shValue;		// 值
	public int	qtQualityCode;		// 质量码
	public byte		byDataSourceID;		// 数据源标识
	public long		DateTime;		// 时间
}

//CCM 传输 ADM 的长整型数据结构
public static class  s_longdata_com {
	s_data_id	dataid;			// 数据 ID
	public int		nValue;			// 值
	int	qtQualityCode;		// 质量码
	public byte		byDataSourceID;		// 数据源标识
	public long		DateTime;		// 时间
}

//CCM 传输 ADM 的浮点(遥测)数据结构()
public static class  s_floatdata_com {
	s_data_id	dataid;			// 数据 ID
	public float		fValue;			// 值
	int	qtQualityCode;	// 质量码
	public byte		byDataSourceID;		// 数据源标识
	public long		DateTime;			// 时间
}

//CCM 传输 ADM 的双精度(电度)数据结构
public static class  s_doubledata_com {
	s_data_id	dataid;			// 数据 ID
	double		dValue;			// 值
	int	qtQualityCode;		// 质量码
	public byte		byDataSourceID;		// 数据源标识
	public long		DateTime;		// 时间
}

//定义窗口值数据
public static class s_meterwindow_data {
	double		dValue;			// 总窗口值
	double		dJFValue;		// 尖峰窗口值
	double		dGFValue;		// 高峰窗口值
	double		dPGValue;		// 平谷窗口值
	double		dDGValue;		// 低谷窗口值
}

//CCM 传输 ADM 的电表窗口值数据结构
public static class s_meterdata_com {
	s_data_id	dataid;			// 数据 ID
	s_meterwindow_data value;		// 窗口值数据
	int	qtQualityCode;		// 质量码
	public byte		byDataSourceID;		// 数据源标识
	public long		DateTime;		// 时间
}

//CCM 传送的 SOE 数据结构
public static class s_soe_com {
	s_data_id	dataid;			// 数据 ID
	public short		wYear;			// 年
	public byte		byMonth;		// 月
	public byte		byDay;			// 日
	public byte		byHour;			// 时
	public byte		byMinute;		// 分
	public byte		bySecond;		// 秒
	public short	wMSecond;		// 毫秒
	public byte		byYXState;		// 遥信状态(D0 位表示当前状态, '1' 代表合; '0' 代表分)
	int	qtQualityCode;		// 质量码
	public byte		byDataSourceID;		// 数据源标识
}

//CCM 传送的微机保护 SOE 数据结构
public static class s_wjbh_soe_com {
	public byte		byType;			// 类型(0:事件 1:自诊断 2:保护复归 3:SEL351事件 4:SEL351调试事件)
	public int		wChangZhanID;		// 厂站 ID
	public byte		byGuiHao;		// 柜号
	public byte		byBianHao;		// 保护编号
	public short		wDeviceType;		// 保护装置类型	2000.08.16 ZB +
	public short		nShGTZNum;		// 事故跳闸次数 (SEL351保护中为重合闸次数)
	public short		nShGType;		// 事故类型(SEL351保护中为定值组号)
	public short		nDZhType;		// 动作类型(SEL351保护中为事件类型)
	public short		nCLType;		// 测量类型(SEL351保护中为故障电流)
	public short		wCanShu;		// 参数(SEL351保护中为故障距离)
	public int		wRTU;			// RTU ID

	public short		wYear;			// 年
	public byte		byMonth;		// 月
	public byte		byDay;			// 日
	public byte		byHour;			// 时
	public byte		byMinute;		// 分
	public byte		bySecond;		// 秒
	public short		wMSecond;		// 毫秒

	public short		nDZType2;		// SEL351保护中为故障指示
	public short		wCSValue2;		// SEL351保护中为系统频率
	int	qtQualityCode;		// 质量码
	public byte		byDataSourceID;		// 数据源标识
}

//其它SOE
//CCM 传送的 其它SOE 数据结构
public static class s_other_soe {
	public int		wRTUID;
	public byte		byBJType;
	public int		wBJID;
	public short		wBJCanShu;

	public short		wYear;			// 年
	public byte		byMonth;		// 月
	public byte		byDay;			// 日
	public byte		byHour;			// 时
	public byte		byMinute;		// 分
	public byte		bySecond;		// 秒
	public short		wMSecond;		// 毫秒
}

//ADM传送CCM遥控数据结构
public static class s_adm_yk_info_com
{
	s_data_id	  dataid;				// 数据ID
	public byte		YKType;				// 1: 分到合,或升,或RTU投；0：合到分,或降,或RTU退
	public float		fYTValue;			// 遥调值
	public int	wRTUNo;				// RTU通道切换/RTU投退的序号
	public float		fValidUpLimit;		// 有效上限
	public float		fValidDnLimit;		// 有效下限
	public float		fValidUpLimit1;		// 负有效上限
	public float		fValidDnLimit1;		// 负有效下限
	public float		fDeadValue;			// 死区值
}

//CCM 传送的遥控返校数据结构
public static class   s_fx_infor_com {
	public byte		byType;			// 返校类型 0：遥控 1：升降
	s_data_id	dataid;			// 数据 ID
	public byte		byResult;		// 返校结果(0分返校正确 1合返校正确 2返校错误)
}

//Wind-Mend<hujr 2012.03.27  
//CCM 省局下发svg定值数据结构
public static class   s_DingZhi_com {
	public byte		byBJType;	// 部件类型
	public int wID;		
	public byte  byBJParam;		// 部件参数
	public float		fResult;	// 定值
	boolean		bRemote;	// 调度发来的遥调值/本地人机界面发出的遥调值
}
//Wind-Mend> hujr 2012.03.27 


//CCM 通道事项传输数据结构
//解释通道事项有两种情况：RTU切换主工作通道、通道切换主工作模块
//若下面结构中wChannel1和wChannel2相等，就是通道（通道号为wChannel1）切换主工作模块，由byServer1主工作切换为byServer2主工作
//若下面结构中wChannel1和wChannel2不相等，就是RTU（RTU号为wRTUNum）切换主工作通道，由wChannel1主工作切换为wChannel2主工作
public static class   s_td_event
{
	public byte		byType;			// 通道事项类型
	public int		wRTUNum;		// RTU 号
	public int		wCZID;			// 厂站 ID
	public long		EventTime;		// 发生时间
	public short		wMSecond;		// 毫秒
	public byte		byCOM_SER_No;		// 服务器组号
	public byte		byServer1;		// 服务器1编号（0：A机，1：B机）
	public byte		byServer2;		// 服务器2编号（0：A机，1：B机）
	public int		wChannel1;		// 通道1 (自动切换事项使用)
	public int		wChannel2;		// 通道2 (自动切换事项使用)
	public byte		byChXReason;	// 通道切换原因
	public byte		byTDIndex;		// 通道序号, 事项翻译时用, 0:通道一, 1:通道二
}

//CCM 通道操作事项传输数据结构
//解释通道操作事项有两种情况：RTU切换主工作通道、通道切换主工作模块
//若下面结构中wChannel1和wChannel2相等，就是通道（通道号为wChannel1）切换主工作模块，由byServer1主工作切换为byServer2主工作
//若下面结构中wChannel1和wChannel2不相等，就是RTU（RTU号为wRTUNum）切换主工作通道，由wChannel1主工作切换为wChannel2主工作
public static class   s_td_czevent
{
	public byte		byCZType;		// 通道操作事项类型
	public byte		byOperType;		// 操作人员类型
	String		[]strName;		// 操作人员名字
	String		[]strMacName;		// 操作机器名字
	public int		wRTUNum;		// RTU 号
	public int		wCZID;			// 厂站 ID
	public long		EventTime;		// 发生时间
	public byte		byCOM_SER_No;		// 服务器组号
	public byte		byServer1;		// 服务器1编号（0：A机，1：B机）
	public byte		byServer2;		// 服务器2编号（0：A机，1：B机）
	public short		wChannel1;		// 通道1
	public short		wChannel2;		// 通道2
}

//ADM 向 CCM 转发数据
//转发字节型(遥信)数据结构
public static class   s_adm_bytedata_com
{
	public byte	byType;			// 类型
	public short	wXuHao;			// 序号
	public byte	byValue;		// 值
	int	qtQualityCode;		// 质量码
	public long	DataTime;		// 时间
}

//转发短整型数据结构
public static class   s_adm_shortdata_com{
	public byte	byType;			// 类型
	public short	wXuHao;			// 序号
	public short	shValue;		// 值
	public int	qtQualityCode;		// 质量码
	public long	DataTime;		// 时间
}

//转发长整型数据结构
public static class   s_adm_longdata_com
{
	public byte	byType;			// 类型
	public short	wXuHao;			// 序号
	public int	nValue;			// 值
	public int	qtQualityCode;		// 质量码
	public long	DataTime;		// 时间
}

//转发浮点型(遥测)数据结构
public static class   s_adm_floatdata_com
{
	public byte	byType;			// 类型
	public short	wXuHao;			// 序号
	public float	fValue;			// 值
	public int	qtQualityCode;		// 质量码
	public long	DataTime;		// 时间
}



//转发双精度型数据结构
public static class   s_adm_doubledata_com
{
	public byte	byType;			// 类型
	public short	wXuHao;			// 序号
	double	dValue;			// 值
	public int	qtQualityCode;		// 质量码
	public long	DataTime;		// 时间
}
/*
//发送命令结构
public static class   s_send_command_com
{
	public byte	byType;			// 类型
	public byte	bySubType;		// 子类型
	public short wSource;		// 来源 0:ADM
	public byte	[]buffer=new byte[32];		// 命令内容
}
//2006.3.30
public static class   s_BJDEF
{
	byte  byBJType;		// 部件类型
	public int wBJID;			// 部件ID
	byte  byBJParam;		// 部件参数
}
//
public static class   s_YC
{
	s_BJDEF		dataid;
	RETYCV		value;
	public byte		byFresh;
}

//
public static class   s_SVG_YC
{
	s_data_id		dataid;
	public float		value;
}


//
public static class   s_YX
{
	s_BJDEF		dataid;
	RETYXV		value;
	public byte		byFresh;
}


*/
//修改原因：2009-11-28，合入戚鑫修改的实时库向rtclient送数模式
public static int intZFRTDATA_BUFLEN =	2048;		// 发送缓冲区长度(略小于软总线的64K)   // 2012.08.28 65500->2048 东营测试
//主类型
public static  int intCOMTYPE_GETRTDATA=	0x01;	// 主类型：获取实时数据
//子类型定义
public static int intTYPE_ZFRTDATA_ALL=	0x01;	// 转发实时数据-全数据
public static int intTYPE_ZFRTDATA_YC=	0x02;	// 转发实时数据-遥测数据
public static int intTYPE_ZFRTDATA_YX=	0x03;	// 转发实时数据-遥信数据
public static int intTYPE_ZFRTDATA_CHGYC=	0x04;	// 转发实时数据-变化遥测数据
public static int intTYPE_ZFRTDATA_CHGYX=	0x05;	// 转发实时数据-变化遥信数据
/*
//遥测结构
public static class   rt_YC
{
	s_BJDEF		dataid;
	RETYCV		value;

}

//遥信结构
public static class   rt_YX
{
	s_BJDEF		dataid;
	RETYXV		value;


}
*/
//帧头
public static class   tagRTDATAHEAD
{
	//Jchar	bySB_Reserve[16];		// SB 保留
	public byte	MainType;				// 主类型（方便其他应用通过此管道获取数据）
	public byte	SubType;				// 通讯类型
	public byte	endFlag;				// 结束帧标志（最后一帧置1，其他置0，可复用）
	public short wCount;					// 数据个数（YC则为rt_YC的个数，YX则为rt_YX的个数）

	
}

}
