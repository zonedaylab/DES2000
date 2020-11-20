package cn.zup.iot.timerdecision.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 获取月份的最后一天
 * @author hans
 *
 */
public class LastDayOfMonth 
{
    /**
     * 获取某月的最后一天
     * @Title:getLastDayOfMonth
     * @Description:
     * @param:@param year
     * @param:@param month
     * @param:@return
     * @return:String
     * @throws
     */
    public static String getLastDayOfMonth(int year,int month)
    {
        Calendar cal = Calendar.getInstance();
        //设置年份
        cal.set(Calendar.YEAR,year);
        //设置月份
        cal.set(Calendar.MONTH, month-1);
        //获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        //设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        //格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String lastDayOfMonth = sdf.format(cal.getTime());
         
        return lastDay+"";
    }
 
    /**
     * @Title:main
     * @Description:
     * @param:@param args
     * @return: void
     * @throws
     */
    public static void main(String[] args) 
    {
        String lastDay = getLastDayOfMonth(2016,11);
        System.out.println("获取当前月的最后一天：" + lastDay);
    }
}
