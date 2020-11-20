package cn.zup.iot.timerdecision.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	
	 
	 public static Date getDate(String source) throws ParseException {  
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(source);
     } 
	 
	 public static Date getDate2(String source) throws ParseException {
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	     return sdf.parse(source);
	 }
	 
	 //获取某个月多少天
	 public static int getDaysOfMonth(Date date){
        Calendar calendar = Calendar.getInstance();  
        calendar.setTime(date);  
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);  
	 }
	 
	 /**
	  * 获取季度的开始日期
	  * mxf
	  * 2017年8月15日16:49:543
	  * @param quarter
	  * @return
	  */
	 public static String quarterStart(int quarter){
		 return (quarter-1)*3+1+"-1";
	 }
	 
	 public static String quarterEnd(int quarter){
		 String quarterEnd = "";
		 switch (quarter){
			 case 1:
				 quarterEnd = "3-31";
				 break;
			 case 2:
				 quarterEnd = "6-30";
				 break;
			 case 3 :
				 quarterEnd = "9-30";
				 break;
			 case 4:
				 quarterEnd = "12-31";
				 break;
		 }
		 return quarterEnd;
	 }
	 
	 public static int getQuarter(int month){
		 int quarter = 0;
		 if(month >= 1 && month <= 3){
			 quarter = 1;
		 }else if(month >=4 && month <= 6){
			 quarter = 2;
		 }else if(month >=7 && month <= 9){
			 quarter = 3;
		 }else{
			 quarter = 4;
		 }
		 return quarter;
	 }
	 /**
	  * 获取指定日期的前一天
	  * mxf
	  * 
	  * 2017年9月5日21:56:50
	  * @param date
	  * @return
	  */
	 public static Date getLastDay(Date date) {  
        Calendar calendar = Calendar.getInstance();  
        calendar.setTime(date);  
        calendar.add(Calendar.DAY_OF_MONTH, -1);  
        date = calendar.getTime();  
        return date;  
    }  
	 
	 /**
	  * 获取日期字符串
	  * mxf
	  * 2017年11月7日09:46:30
	  * @param date
	  * @return
	  */
	 public static String getDateStr(Date date) {
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	     return sdf.format(date);
	 }
	 
	 /**
	  * 判断某天是否为当天
	  * mxf
	  * 2017年12月7日16:52:55
	  * @param date
	  * @return
	  */
	 public static boolean isToday(Date date) {
		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String todayStr = sdf.format(today);
		String dateStr = sdf.format(date);
		return todayStr.equals(dateStr);
	 }
	 
	 /**
	  * 根据日期年月获取当前月的最后一天
	  * 若是本月，则返回今天
	  * 若不是本月，返回该月份的最后一天
	  * @param date
	  * @return
	 * @throws ParseException 
	  */
	 public static Date findLastDay(String date) throws ParseException {
		 Calendar cal = Calendar.getInstance();
		 int year = cal.get(Calendar.YEAR);//当前年
		 int month = cal.get(Calendar.MONTH )+1;//当前月
		 String[] arr = date.split("-");
		 
		 int year2 = Integer.parseInt(arr[0]);
		 int month2 = Integer.parseInt(arr[1]);
		 
		 if(year == year2 && month == month2) {//当前月
			 //返回当天的日期
			 return new Date();
		 } else {
			 //返回该月的最后一天
			 //设置年份
		        cal.set(Calendar.YEAR,year2);
		        //设置月份
		        cal.set(Calendar.MONTH, month2-1);
		        //获取某月最大天数
		        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		        return getDate(year2 + "-" + month2 + "-" + lastDay);
		 }
	 }
	 
	 /**
	  * 获取月的第一天
	  * mxf
	  * 2017年12月8日10:30:18
	  * @param date
	  * @return
	 * @throws ParseException 
	  */
	 public static Date findFirstDay (String date) throws ParseException {
		String[] arr = date.split("-");
		return getDate(arr[0] + "-" + arr[1] + "-1");
	 }
	 
	 /**
	  * 比较两个日期是否相等
	  * @param d1
	  * @param d2
	  * @return
	  */
	 public static int compareDate(Date d1,Date d2){
         if (d1.getTime() > d2.getTime()) {
             return 1;
         } else if (d1.getTime() < d2.getTime()) {
             return -1;
         } else {//相等
             return 0;
         }
	 }	

}
