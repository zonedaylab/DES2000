package cn.zup.iot.timerdecision.util;

import cn.zup.iot.timerdecision.util.farmat.MathUtil;
import org.apache.log4j.Logger;

import java.util.List;


/**
 * 标杆电量工具类
 * mxf
 * 2017年10月6日08:22:58
 */
public class BenchmarkElectricityUtil {
	
	private static Logger log = Logger.getLogger(BenchmarkElectricityUtil.class);
	
	private static double v = 0.000;
	
	/**
	 * 获取四次增强标杆电量
	 * mxf
	 * maxData 为组内最大电量
	 * hisDataList 为组内电站发电量集合
	 * 
	 * 2017年10月6日08:25:05
	 * @return
	 * @throws Exception 
	 */
	public static double getFourEnhancementResult(Double maxData, List<Double> hisDataList, Integer capacity) throws Exception {
		double numerator = 0;//分子
		double denominator = 0;//分母
		if(hisDataList != null) {
			for(Double data : hisDataList) {
				numerator += Math.pow(data, 5) / Math.pow(maxData, 4);
			}
			
			for(Double data : hisDataList) {
				denominator += Math.pow(data, 4) / Math.pow(maxData, 4);
			}
		}
		
		if(MathUtil.equals(denominator, v)) {
			log.error("【获取标杆电量四次增强】 分母为0");
			throw new Exception();
		}
		
		return numerator / denominator  * capacity;
	}

}
