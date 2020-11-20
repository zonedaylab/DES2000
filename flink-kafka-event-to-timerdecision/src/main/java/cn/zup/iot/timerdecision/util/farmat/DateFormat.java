/**
 * 
 */
package cn.zup.iot.timerdecision.util.farmat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * @author jiangfutao
 *
 */
public class DateFormat{
	
	public static Date getDate(String dateStr){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	public static Date getDate2(String dateStr){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	public static String getDateStr(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = null;
		dateStr = sdf.format(date);
		return dateStr;
	}
	
	public static String getDateStr2(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = null;
		dateStr = sdf.format(date);
		return dateStr;
	}
	
	 public static String formatGregorianCalendar(GregorianCalendar retTime){
	    	int year = retTime.get(GregorianCalendar.YEAR);
	        int month = retTime.get(GregorianCalendar.MONTH) + 1;
	        int day = retTime.get(GregorianCalendar.DAY_OF_MONTH);
	        int hour = retTime.get(GregorianCalendar.HOUR_OF_DAY);
	        int min = retTime.get(GregorianCalendar.MINUTE);
	        int sec = retTime.get(GregorianCalendar.SECOND);
	        int msec = retTime.get(GregorianCalendar.MILLISECOND);

	        String strVal = formatStr(year, 4) + "-" + formatStr(month, 2) + "-" + formatStr(day, 2) + " " + formatStr(hour, 2) + ":" + formatStr(min, 2);
	        return strVal;
	    }
	 
	 public static String getTimeBySecond(long second){
		 	GregorianCalendar gc = new GregorianCalendar();
		 	gc.setTimeInMillis(second*1000);
		 
	    	int year = gc.get(GregorianCalendar.YEAR);
	        int month = gc.get(GregorianCalendar.MONTH) + 1;
	        int day = gc.get(GregorianCalendar.DAY_OF_MONTH);
	        int hour = gc.get(GregorianCalendar.HOUR_OF_DAY);
	        int min = gc.get(GregorianCalendar.MINUTE);
	        int sec = gc.get(GregorianCalendar.SECOND);

	        String strVal = formatStr(year, 4) + "-" + formatStr(month, 2) + "-" + formatStr(day, 2) 
	        				+ " " + formatStr(hour, 2) + ":" + formatStr(min, 2)+ ":" + formatStr(sec, 2);
	        return strVal;
	    }

	    public static String formatStr(int v, int len) {
	        String s = String.valueOf(v);
	        String r = s;

	        for (int i = s.length(); i < len; i++) {
	            r = "0" + r;
	        }

	        return r;
	    }
	    
	    public static String formatGregorianCalendar1(GregorianCalendar retTime){
	        int hour = retTime.get(GregorianCalendar.HOUR_OF_DAY);
	        int min = retTime.get(GregorianCalendar.MINUTE);
	        int sec = retTime.get(GregorianCalendar.SECOND);
	        int msec = retTime.get(GregorianCalendar.MILLISECOND);

	        String strVal = formatStr1(hour, 2) + ":" + formatStr1(min, 2);
	        return strVal;
	    }

	    public static String formatStr1(int v, int len) {
	        String s = String.valueOf(v);
	        String r = s;

	        for (int i = s.length(); i < len; i++) {
	            r = "0" + r;
	        }

	        return r;
	    }

}
