package cn.zup.iot.timerdecision.util.file;

import java.util.Map;

public class SystemUtil {
	private static SystemUtil instance = null;
	private static String iesBaseHome;
	public static String webHome;

	public static SystemUtil instance() {
		if (instance == null) {
			instance = new SystemUtil();
		}
		return instance;
	}

	private SystemUtil() {
		Map sys_propertyMap = System.getenv();
		iesBaseHome = (String) sys_propertyMap.get("IESBASE"); // sys_property.getProperty("ICCSWEBHOME");
		iesBaseHome += "/";

		if (webHome == null) {
			webHome = getWebConfigPrefix();
		}
	}

	public String getWebConfigPrefix() {
//		String classesPath = Thread.currentThread().getContextClassLoader()
//				.getResource("/").getPath();
		String classesPath = this.getClass().getResource("/").getPath();
		String webProjectName = "";
		if (null != classesPath && !"".equals(classesPath)) {
			int pos = classesPath.indexOf("WEB-INF");
			if (pos > 0) {
				classesPath = classesPath.substring(0, pos);
				String[] str = classesPath.replace("\\", "/").split("/");
				if (str.length > 0) {
					webProjectName = str[str.length - 1];
				}
			}
		}
		return webProjectName;
	}

	public String getIesBaseHome() {
		return iesBaseHome;
	}

	public String getIccsWebHome() {
		return webHome;
	}

	public static void setIccsWebHome(String name) {
		webHome = name;
	}

}
