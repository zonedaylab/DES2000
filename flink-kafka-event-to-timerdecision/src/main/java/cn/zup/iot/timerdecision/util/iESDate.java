package cn.zup.iot.timerdecision.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class iESDate {	
	
	Calendar calendarDate = Calendar.getInstance();
	public iESDate(Date date)
	{		
		calendarDate.setTime(date);				

	}	
	public iESDate(Calendar date)
	{		
		calendarDate=date;
	}
	public iESDate(String strDate) throws ParseException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = sdf.parse(strDate);         //得到了Date
		calendarDate.setTime(date);  
	}
	public int getYear()
	{
		return  calendarDate.get(Calendar.YEAR);
	}

	public int getMonth()
	{
		return  calendarDate.get(Calendar.MONTH)+1;
	}
	public int getDayOfMonth()
	{
		return  calendarDate.get(Calendar.DAY_OF_MONTH);
	}
	//获取从公元元年开始的所有月份
	public int getMonths()
	{
		return  calendarDate.get(Calendar.YEAR)*12+calendarDate.get(Calendar.MONTH)+1;
	}
	
	public void AddDay(int days)
	{
		calendarDate.add(Calendar.DATE, days);
	}
	public void AddMonth(int months)
	{
		calendarDate.add(Calendar.MONTH, months);
	}
	
	//判断是否同一月	
	public boolean equasMonth(Date date)
	{
		Calendar tempDate= Calendar.getInstance();
		tempDate.setTime(date);
		return equasMonth(tempDate);
	}
	//判断是否同一月	
	public boolean equasMonth(iESDate date)
	{	
		if(date.getMonth()==this.getMonth()&&
			date.getMonth()==this.getYear())
			return true;
		return false;
	}		
	//判断是否同一月	
	public boolean equasMonth(Calendar date)
	{
		if(date.get(Calendar.MONTH)+1 ==this.getMonth()&&
			date.get(Calendar.YEAR) == this.getYear())
			return true;
		return false;
	}
	
	//判断是否同一天
	
	
	
	//设置日期
	public void setDay(int day)
	{		
		calendarDate.set(Calendar.DAY_OF_MONTH,day);
	}
	
	
	
}
