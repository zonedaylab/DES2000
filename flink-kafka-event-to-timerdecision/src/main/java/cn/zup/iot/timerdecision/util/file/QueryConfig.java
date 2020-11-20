/********************************************************************
 *	版权所有 (C) 2009-2013 积成电子股份有限公司
 *	保留所有版权
 *	
 *	作者：	侯培彬
 *	日期：	2013-3-14
 *	摘要：	参数缓存类
 *  功能：      读取配置文件，把配置参数在此文件中缓存，此类只能读取，不能设置
 *
 *********************************************************************/

package cn.zup.iot.timerdecision.util.file;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;

public class QueryConfig {

	// 单例模式
	private static QueryConfig instance = null;
	private String test = "0"; // 并发测试标志，默认不测试
	private String style = "guowang"; // 风格，例如nanwang
	private String model = "dms1000e"; // 型号，例如dms1000e
	private String yanzhengma = "0";   //验证码使用标志
	private String userlimit = "500";   //用户登录上限
	private String userlimitLog = "0";   //是否打印用户登录上限测试日志
	private String userlimitFlag = "0";//是否启用用户登录上限
	private String version = "jixian"; // 版本，例如jixian
	private int csczEventSize = 1051; // 版本，例如jixian
	private int xtEventSize = 66; // 版本，例如jixian
	private String indexName = "CBF7D2FDC4BFC2BC475241"; //设置主页svg文件的名称
	private String indexFlag = "0"; //设置主页svg文件的名称
	
	private String duotuyuan = "0";  //设置是否为多图元
	private String dataSource = "0";  //数据库型号
	private String shebei = "0";  //设置设备
	private long pasinvalidtime = 0; //设置密码失效时间，以分钟为单位

	private String realIp = "192.168.10.89"; // 连接webdatas服务ip地址
	private String realPort = "1289"; // 连接webdatas服务端口号

	private String webproxyIp = "127.0.0.1"; // 连接webproxy服务ip地址
	private String webproxyPort = "15001"; // 连接webproxy服务端口号

	private String secserverFlag = "0"; // 安全使用标志
	private String grarightFlag = "0"; // 安全使用标志
	

	private String rtEventFlag = "0"; // 实时事项使用标志
	private String cacheNum = "10241"; // 后台缓存事项最大个数
	private String flexNum = "200"; // 前台显示事项最大个数

	private String ftuFlag = "0"; // 是否获取终端实时在线统计数据标志，1为获取，0为不获取
	private String fturealIp = ""; // 连接实时服务获取终端实时在线统计数据ip地址，其实就是实时服务运行机器ip地址
	private String fturealPort = ""; // 连接实时服务获取终端实时在线统计数据端口号
	
	private String ydddFlag = "0"; // 是否获取用电调度数据标志，1为获取，0为不获取
	private String ydddrealIp = ""; // 连接实时服务获取用电调度ip地址，其实就是实时服务运行机器ip地址
	private String ydddrealPort = ""; // 连接实时服务获取用电调度端口号
	
	private String usertimeoutflag="0";//用户访问超时标志
	private String usertimeout="0";//用户访问超时时间
	
	private String ftumessageip="127.0.0.1"; //终端报文IP
	private String ftumessageport="37006";//终端报文端口
	private String receivetime="10";//每隔几毫秒读取缓冲区的数据
	private String flexnum="30"; //前端显示的记录条数
	private String ftumessageflag="0";//终端报文使用标志
	
	
	private String hiseventAlarm = "0";

	
	public static QueryConfig getInstance() {
		if (instance == null) {
			instance = new QueryConfig();
		}
		return instance;
	}

	/**
	 * 从queryParam.xml配置文件中，读取配置参数
	 */
	private QueryConfig() {
		
		String configPath = this.getClass().getResource("/").getPath()+"config/queryParam.xml";
		System.out.println("读取配置文件开始，路径为："+ configPath);
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			File configFile = new File(configPath);
			Document configDoc = builder.parse(configFile);
			XPath xpath = XPathFactory.newInstance().newXPath();
			test = (String) xpath.evaluate("/params/test", configDoc,
					XPathConstants.STRING);
			style = (String) xpath.evaluate("/params/system/style", configDoc,
					XPathConstants.STRING);
			model = (String) xpath.evaluate("/params/system/model", configDoc,
					XPathConstants.STRING);
			yanzhengma = (String) xpath.evaluate("/params/system/yanzhengma", configDoc,
					XPathConstants.STRING);
			userlimit = (String) xpath.evaluate("/params/system/userlimit", configDoc,
					XPathConstants.STRING);
			if(userlimit.equals("")){
				userlimit = "0";
			}
			userlimitLog = (String) xpath.evaluate("/params/system/userlimitLog", configDoc,
					XPathConstants.STRING);
			userlimitFlag = (String)xpath.evaluate("/params/system/userlimitFlag", configDoc,
					XPathConstants.STRING);
			dataSource = (String) xpath.evaluate("/params/system/dataSource", configDoc,
					XPathConstants.STRING);
			version = (String) xpath.evaluate("/params/system/version", configDoc,
					XPathConstants.STRING);
			duotuyuan = (String) xpath.evaluate("/params/system/svg/duotuyuan", configDoc,
					XPathConstants.STRING);
			indexName = (String) xpath.evaluate("/params/system/svg/index", configDoc,
					XPathConstants.STRING);
			indexFlag = (String) xpath.evaluate("/params/system/svg/indexFlag", configDoc,
					XPathConstants.STRING);
			shebei = (String) xpath.evaluate("/params/system/svg/shebei", configDoc,
					XPathConstants.STRING);
				
				
			System.out.println("测试标志："+ test);
			System.out.println("模板为："+ style);
			System.out.println("验证码使用标志为："+ yanzhengma);
			System.out.println("用户登录上限个数为："+ userlimit);
			System.out.println("用户登录上限日志标志："+ userlimitLog);
			System.out.println("用户登录上限个数启用为："+ userlimitFlag);
	    	System.out.println("系统类型为："+ model);
	    	System.out.println("程序版本为："+ version);
	    	System.out.println("主页svg名称为："+ indexName);
	    	System.out.println("web打开主索引默认的图形名称 ，是否使用责任区区分主索引标志："+ indexFlag);
	    	System.out.println("多图元标志为："+ duotuyuan);
	    	System.out.println("数据库型号（0：oracle;1:dm）："+ dataSource);
	    	System.out.println("设备定位标志为："+ shebei);
		    try{	
		    	csczEventSize = Integer.valueOf((String)(xpath.evaluate("/params/system/"+version+"/csczevent", configDoc,
						XPathConstants.STRING)));
		    	xtEventSize = Integer.valueOf((String)(xpath.evaluate("/params/system/"+version+"/xtevent", configDoc,
						XPathConstants.STRING)));
		    	pasinvalidtime = Integer.valueOf((String)(xpath.evaluate("/params/system/user/pasinvalidtime", configDoc,
						XPathConstants.STRING)));
		    	System.out.println("csczevent："+ csczEventSize);
		    	System.out.println("xtevent："+ xtEventSize);
		    	System.out.println("pasinvalidtime："+ pasinvalidtime);
			} catch (Exception err) {
				System.out.println("/params/system/"+version+"/csczevent 不存在");
				System.out.println("/params/system/"+version+"/xtevent 不存在");
				System.out.println("/params/system/user/pasinvalidtime 不存在");
				System.out.println("csczevent默认值："+ csczEventSize);
		    	System.out.println("xtevent默认值："+ xtEventSize);
		    	System.out.println("pasinvalidtime默认值："+ pasinvalidtime);
			}
			
			
			realIp = (String) xpath.evaluate("/params/webdatas/ip", configDoc,
					XPathConstants.STRING);
			realPort = (String) xpath.evaluate("/params/webdatas/port",
					configDoc, XPathConstants.STRING);
			System.out.println("webdatas运行机器配置ip为："+ realIp);
	    	System.out.println("webdatas运行机器配置端口为："+ realPort);

			webproxyIp = (String) xpath.evaluate("/params/webproxy/ip",
					configDoc, XPathConstants.STRING);
			webproxyPort = (String) xpath.evaluate("/params/webproxy/port",
					configDoc, XPathConstants.STRING);
			System.out.println("webproxy运行机器配置ip为："+ webproxyIp);
	    	System.out.println("webproxy运行机器配置端口为："+ webproxyPort);

			secserverFlag = (String) xpath.evaluate(
					"/params/webproxy/secserver/enable", configDoc,
					XPathConstants.STRING);
			System.out.println("责任区使用标志为："+ secserverFlag);
			
			grarightFlag = (String) xpath.evaluate(
					"/params/webproxy/graright/enable", configDoc,
					XPathConstants.STRING);
			System.out.println("图形权限使用标志为："+ grarightFlag);
			

			rtEventFlag = (String) xpath.evaluate(
					"/params/webproxy/rtevent/enable", configDoc,
					XPathConstants.STRING);
			cacheNum = (String) xpath.evaluate(
					"/params/webproxy/rtevent/cacheNum", configDoc,
					XPathConstants.STRING);
			flexNum = (String) xpath.evaluate(
					"/params/webproxy/rtevent/flexNum", configDoc,
					XPathConstants.STRING);
			System.out.println("实时事项使用标志为："+ rtEventFlag);
			System.out.println("实时事项后台缓存数据条数为："+ rtEventFlag);
			System.out.println("实时事项前台缓存数据条数为："+ rtEventFlag);

			ftuFlag = (String) xpath.evaluate("/params/ftu/enable", configDoc,
					XPathConstants.STRING);
			fturealIp = (String) xpath.evaluate("/params/ftu/ip", configDoc,
					XPathConstants.STRING);
			fturealPort = (String) xpath.evaluate("/params/ftu/port",
					configDoc, XPathConstants.STRING);
			System.out.println("终端在线统计使用标志为："+ ftuFlag);
			System.out.println("终端在线统计ip为："+ fturealIp);
			System.out.println("终端在线统计端口为："+ fturealPort);
			
			ydddFlag = (String) xpath.evaluate("/params/yddd/enable", configDoc,
					XPathConstants.STRING);
			ydddrealIp = (String) xpath.evaluate("/params/yddd/ip", configDoc,
					XPathConstants.STRING);
			ydddrealPort = (String) xpath.evaluate("/params/yddd/port",
					configDoc, XPathConstants.STRING);
			System.out.println("用电调度使用标志为："+ ydddFlag);
			System.out.println("用电调度ip为："+ ydddrealIp);
			System.out.println("用电调度端口为："+ ydddrealPort);
			
			usertimeoutflag=(String) xpath.evaluate("/params/system/usertimeoutflag", configDoc,
					XPathConstants.STRING);
			usertimeout=(String) xpath.evaluate("/params/system/usertimeout", configDoc,
					XPathConstants.STRING);	

			System.out.println("用户访问超时标志："+ usertimeoutflag);
			System.out.println("用户访问超时时间："+ usertimeout);
			
			
			ftumessageflag=(String) xpath.evaluate("/params/ftumessage/ftumessageflag", configDoc,
					XPathConstants.STRING);
			ftumessageip=(String) xpath.evaluate("/params/ftumessage/ip", configDoc,
					XPathConstants.STRING);
			ftumessageport=(String) xpath.evaluate("/params/ftumessage/port", configDoc,
					XPathConstants.STRING);
			receivetime=(String) xpath.evaluate("/params/ftumessage/receivetime", configDoc,
					XPathConstants.STRING);
			flexnum=(String) xpath.evaluate("/params/ftumessage/num", configDoc,
					XPathConstants.STRING);
 
			System.out.println("终端报文使用标志："+ ftumessageflag);
			System.out.println("终端报文IP："+ ftumessageip);
			System.out.println("终端报文端口："+ ftumessageport);
			System.out.println("终端socket读取速率："+ receivetime);
			System.out.println("终端报文前端显示条数："+ flexnum);
			
			hiseventAlarm = (String) xpath.evaluate("/params/hisevent/alarmMode", configDoc,
					XPathConstants.STRING);
		} catch (Exception err) {
			System.out.println("读取配置文件【" + configPath + "】异常：" + err);
		}
	}

	public String getUserlimitFlag() {
		return userlimitFlag;
	}

	public void setUserlimitFlag(String userlimitFlag) {
		this.userlimitFlag = userlimitFlag;
	}

	public String getTest() {
		return test;
	}

	public void setTest(String test) {
		this.test = test;
	}

	public String getStyle() {
		return style;
	}

	public String getModel() {
		return model;
	}

	public String getRealIp() {
		return realIp;
	}

	public String getRealPort() {
		return realPort;
	}

	public String getWebproxyIp() {
		return webproxyIp;
	}

	public String getWebproxyPort() {
		return webproxyPort;
	}

	public String getSecserverFlag() {
		return secserverFlag;
	}

	public String getRtEventFlag() {
		return rtEventFlag;
	}

	public String getCacheNum() {
		return cacheNum;
	}

	public String getFlexNum() {
		return flexNum;
	}

	public String getFtuFlag() {
		return ftuFlag;
	}

	public String getFturealIp() {
		return fturealIp;
	}

	public String getGrarightFlag() {
		return grarightFlag;
	}

	public void setGrarightFlag(String grarightFlag) {
		this.grarightFlag = grarightFlag;
	}

	public String getFturealPort() {
		return fturealPort;
	}
	
	public String getHiseventAlarm() {
		return hiseventAlarm;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public static void setInstance(QueryConfig instance) {
		QueryConfig.instance = instance;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public void setRealIp(String realIp) {
		this.realIp = realIp;
	}

	public void setRealPort(String realPort) {
		this.realPort = realPort;
	}

	public void setWebproxyIp(String webproxyIp) {
		this.webproxyIp = webproxyIp;
	}

	public void setWebproxyPort(String webproxyPort) {
		this.webproxyPort = webproxyPort;
	}

	public void setSecserverFlag(String secserverFlag) {
		this.secserverFlag = secserverFlag;
	}

	public void setRtEventFlag(String rtEventFlag) {
		this.rtEventFlag = rtEventFlag;
	}

	public void setCacheNum(String cacheNum) {
		this.cacheNum = cacheNum;
	}

	public void setFlexNum(String flexNum) {
		this.flexNum = flexNum;
	}

	public void setFtuFlag(String ftuFlag) {
		this.ftuFlag = ftuFlag;
	}

	public void setFturealIp(String fturealIp) {
		this.fturealIp = fturealIp;
	}

	public void setFturealPort(String fturealPort) {
		this.fturealPort = fturealPort;
	}

	public void setHiseventAlarm(String hiseventAlarm) {
		this.hiseventAlarm = hiseventAlarm;
	}

	public int getCsczEventSize() {
		return csczEventSize;
	}

	public void setCsczEventSize(int csczEventSize) {
		this.csczEventSize = csczEventSize;
	}

	public int getXtEventSize() {
		return xtEventSize;
	}

	public void setXtEventSize(int xtEventSize) {
		this.xtEventSize = xtEventSize;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getDuotuyuan() {
		return duotuyuan;
	}

	public void setDuotuyuan(String duotuyuan) {
		this.duotuyuan = duotuyuan;
	}

	public String getShebei() {
		return shebei;
	}

	public void setShebei(String shebei) {
		this.shebei = shebei;
	}
	
	public long getPasinvalidtime() {
		return pasinvalidtime;
	}

	public void setPasinvalidtime(long pasinvalidtime) {
		this.pasinvalidtime = pasinvalidtime;
	}

	public String getYanzhengma() {
		return yanzhengma;
	}

	public void setYanzhengma(String yanzhengma) {
		this.yanzhengma = yanzhengma;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	public String getYdddFlag() {
		return ydddFlag;
	}

	public void setYdddFlag(String ydddFlag) {
		this.ydddFlag = ydddFlag;
	}

	public String getYdddrealIp() {
		return ydddrealIp;
	}

	public void setYdddrealIp(String ydddrealIp) {
		this.ydddrealIp = ydddrealIp;
	}

	public String getYdddrealPort() {
		return ydddrealPort;
	}

	public void setYdddrealPort(String ydddrealPort) {
		this.ydddrealPort = ydddrealPort;
	}

	public String getUserlimit() {
		return userlimit;
	}

	public void setUserlimit(String userlimit) {
		this.userlimit = userlimit;
	}

	public String getUserlimitLog() {
		return userlimitLog;
	}

	public void setUserlimitLog(String userlimitLog) {
		this.userlimitLog = userlimitLog;
	}

	public String getIndexFlag() {
		return indexFlag;
	}

	public void setIndexFlag(String indexFlag) {
		this.indexFlag = indexFlag;
	}

	public String getUsertimeoutflag() {
		return usertimeoutflag;
	}

	public void setUsertimeoutflag(String usertimeoutflag) {
		this.usertimeoutflag = usertimeoutflag;
	}

	public String getUsertimeout() {
		return usertimeout;
	}

	public void setUsertimeout(String usertimeout) {
		this.usertimeout = usertimeout;
	}

	public String getFtumessageip() {
		return ftumessageip;
	}

	public void setFtumessageip(String ftumessageip) {
		this.ftumessageip = ftumessageip;
	}

	public String getFtumessageport() {
		return ftumessageport;
	}

	public void setFtumessageport(String ftumessageport) {
		this.ftumessageport = ftumessageport;
	}

	public String getReceivetime() {
		return receivetime;
	}

	public void setReceivetime(String receivetime) {
		this.receivetime = receivetime;
	}

	public String getFlexnum() {
		return flexnum;
	}

	public void setFlexnum(String flexnum) {
		this.flexnum = flexnum;
	}

	public String getFtumessageflag() {
		return ftumessageflag;
	}

	public void setFtumessageflag(String ftumessageflag) {
		this.ftumessageflag = ftumessageflag;
	}
	
	
	
}
