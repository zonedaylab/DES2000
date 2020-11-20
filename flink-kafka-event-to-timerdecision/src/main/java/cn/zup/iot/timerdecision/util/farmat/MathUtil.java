package cn.zup.iot.timerdecision.util.farmat;


import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 产生随机数，暂时替代通过Rid取过来的值。
 * @param 
 * @author Lucifer 2012-12-07
 * */
public class MathUtil {
	
	private static DecimalFormat df = new DecimalFormat("#.00");
	private static double IESLAB_FLT_EPSILON = 1.0e-05;
	private static final double VALUE = 0.001;
	/**
	 * 百分比
	 * */
	public static String producePercentValue(){
		String val = null;
		
		double s = 0;
		s = Math.random()*100;
		val = df.format(s)+"%";
		return val;
	}
	/**
	 * “是否”函数
	 * */
	public static String produceWhetherValue(){
		String val = null;
		double s = 0;
		s = Math.random()*100;
		if(s>50){
			val = "是";
		}else{
			val = "否";
		}
		return val;
	}
	/**
	 * 较大整数（流量，使用大小等）
	 * */
	public static String produceBigNumber(){
		String val = null;
		double s = 0;
		s = Math.random()*100+Math.random()*10;
		s = s + 0.817;
		val = df.format(s);
		return val;
	}
	/**
	 * 运行时间
	 **/
	public static String produceHours(){
		String val = null;
		double s =0;
		s = Math.random()*1000+Math.random()*5;
		s = s+5.7;
		val = df.format(s);
		return val;
	}
	/**
	 *将double型数据按给定位有效数字存储，以百分比形式返回
	 *@param s 进行处理的double数据
	 *@return 格式化后的字符串
	 **/
	public static String formatDouble(double s){
		String st = null; 
		if(isEqual(s, 0)){
			 st = String.valueOf(s)+"%";
			 return st;
		 }
		 
		 BigDecimal bg = new BigDecimal(s); 
		 double j = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		 st = String.valueOf(j);
		 st = st+"%";
		 return st;
	}
	
	/**
	 *将double型数据按给定位有效数字存储，以百分比形式返回
	 *@param s 进行处理的double数据
	 *@return 格式化后的字符串
	 *function： 处理s不直接是百分比的情况（小数/大数）
	 **/
	public static String formatDoubleSmall(double s){
		String st = null; 
		if(isEqual(s, 0)){
			 st = String.valueOf(s)+"%";
			 return st;
		 }
		 
		 BigDecimal bg = new BigDecimal(s*100); 
		 double j = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		 st = String.valueOf(j);
		 st = st+"%";
		 return st;
	}
	
	/**
	 *将double型数据按给定位有效数字存储，以百分比形式返回
	 *@param s 进行处理的double数据
	 *@return 格式化后的字符串
	 **/
	public static String formatDouble(double s,int deg){
		 String st = null;
		 BigDecimal bg = new BigDecimal(s*deg);        
		 double j = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		 st = String.valueOf(j);
		 st = st+"%";
		 return st;
	}
	/**
	 *将double型数据按给定位有效数字存储，以原始字符串形式返回
	 *@param s 进行处理的double数据
	 *@return 格式化后的字符串
	 **/
	public static String formatDoubleNon(double s){
		String val = null;
		if(isEqual(s, 0)){
			val = "0.0";
			return val;
		}
		 double st = s;
		 val = df.format(st);
		 return val;
	}
	
	public static double formatDoubleInDeg(double s,int deg){
		double j = 0.0;
		try{
			BigDecimal bg = new BigDecimal(s);        
			j = bg.setScale(deg, BigDecimal.ROUND_HALF_UP).doubleValue();
		}catch(Exception e){
			 System.out.println(e.toString());
		}
		return j;
	}
	
	/**
	 *  == 判断
	 *@param
	 *@return
	 **/
	public static boolean isEqual(double left,double right){
		return (left-IESLAB_FLT_EPSILON < right && right < left+IESLAB_FLT_EPSILON);
	}
	
	
	/**
	 *  < 判断
	 *@param
	 *@return
	 **/
	public static boolean isLessThan(double left,double right){
		return (!isEqual(left, right) && left < right);
	}
	
	
	/**
	 *  > 判断
	 *@param
	 *@return
	 **/
	public static boolean isGreatThan(double left,double right){
		return (!isEqual(left, right) && left > right);
	}
	
	
	/**
	 *  == 判断
	 *@param
	 *@return
	 **/
	public static boolean isEqual(float left,float right){
		return (left-IESLAB_FLT_EPSILON < right && right < left+IESLAB_FLT_EPSILON);
	}
	
	
	/**
	 *  < 判断
	 *@param
	 *@return
	 **/
	public static boolean isLessThan(float left,float right){
		return (!isEqual(left, right) && left < right);
	}
	
	
	/**
	 *  > 判断
	 *@param
	 *@return
	 **/
	public static boolean isGreatThan(float left,float right){
		return (!isEqual(left, right) && left > right);
	}
	
	
	/**
	 *以字符串格式返回保留三位小数并进行四舍五入的字符串
	 *@author zhangxiumei 2014-05-07 
	 **/
	public static String formatDoubleNon3(double s){
		double d = s;
		String result = String.format("%.3f", d);
		return result;
	}
	
	/**
	 * 保留有效数字
	 * */
	public static String formatDoubleNonExt(double s,int num){
		double d = s;
		String result = String.format("%." + num +"f", d);
		return result;
	}

	/**
	 * 比较两个数是否相等
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static boolean equals (Double v1, Double v2) {
		Double result = Math.abs(v1-v2);
		if(result < VALUE) {
			return true;
		}else {
			return false;
		}
	}
	
}
