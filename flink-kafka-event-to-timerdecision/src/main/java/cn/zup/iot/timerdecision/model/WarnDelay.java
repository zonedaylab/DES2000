package cn.zup.iot.timerdecision.model;

import java.util.HashMap;
import java.util.Map;

public class WarnDelay {
	//全局静态变量，存储告警时长。
	public static Map<Integer, Integer> warnDelayMapOfOnline = new HashMap<Integer,Integer>();

	public static Map<Integer, Integer> warnRecoverMapOfOnline = new HashMap<Integer,Integer>();
	}
