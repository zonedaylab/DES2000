package cn.zup.iot.timerdecision.util;

import cn.zup.iot.timerdecision.util.farmat.MathUtil;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * 电站离散率工具类
 * @author mxf
 * 2017年10月9日08:30:12
 *
 */
public class DispersionRatioUtil {
	
	private static Logger log = Logger.getLogger(DispersionRatioUtil.class);
	
	private static final double v = 0.000;
	/**
	 * 计算电站离散率
	 * mxf
	 * 2017年10月9日08:36:13
	 * @param values
	 * @param avg
	 * @return
	 * @throws Exception 
	 */
	public static double calDispersionRatio(List<Double> values, double avg) throws Exception {
		
		double numerator = 0;//分子
		
		if(MathUtil.equals(avg, v)) {
			log.error(avg);
			return 0.0;
		}
		//计算电站离散率
		for(int i=0;i<values.size();i++) {
			numerator += Math.pow(values.get(i) - avg, 2);
		}
		
		numerator = Math.pow(numerator / values.size(), 0.5 );
		
		return numerator / avg;
		
	}

}
