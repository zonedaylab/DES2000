package cn.zup.iot.timerdecision.util;
/**
 * 公共类
 * @author samson
 *
 */
public class CommonUtil {
	
	/**
	 * 进行分页显示  对sql语句进行加工 
	 * @param pageSize 每页数据量
	 * @param PageIndex 页码
	 * @param strSql sql语句
	 * @return
	 */
	public static String getPageConvert(int pageSize,int PageIndex,String strSql)
	{
		//分页头部
		strSql=	" select * from (" +
					" Select (@rowNum:=@rowNum+1) as rowNo,DataPage.* From " +
						" ( "+strSql;
		//分页尾部
		strSql=strSql+"	) as DataPage ,(Select (@rowNum :=0) " +
					" ) b  " +
				" ) com where 1=1 ";
		//查询页数和页码
    	if(pageSize != 0 && PageIndex !=0)
		{
    		strSql += " and rowNo >"+(PageIndex-1)*pageSize;
    		strSql += " and rowNo <="+pageSize*PageIndex;
		}
		return strSql;
	}
	
	/**
	 * 数据总条数  对sql语句进行加工
	 * @param strSql sql语句
	 * @return
	 */
	public static String getPageConvertCount(String strSql)
	{
		//分页头部
		strSql=	" select count(*) as counts from (" +
					" Select (@rowNum:=@rowNum+1) as rowNo,DataPage.* From " +
						" ( "+strSql;
		//分页尾部
		strSql=strSql+"	) as DataPage ,(Select (@rowNum :=0) " +
					" ) b  " +
				" ) com where 1=1 ";
		return strSql;
	}
	
}
