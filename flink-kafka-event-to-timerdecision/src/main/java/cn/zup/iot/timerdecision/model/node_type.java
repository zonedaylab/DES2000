package cn.zup.iot.timerdecision.model;
/** 
 活动节点类型
 
*/
public enum node_type
{
	SLEEP(0), 		//0 休眠	
	FINISHED(1), 	//1 完成
	DIED(2); 		//2 僵死状态
	//必须增加一个构造函数,变量,得到该变量的值
	private int  mNodeType=0;
	private node_type(int value)
	{
		mNodeType=value;
	}
	/**
	* @return 枚举变量实际返回值
	*/
	  public int getNodeType()
	  {
		  return mNodeType;
	  } 
}