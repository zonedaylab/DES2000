package cn.zup.iot.timerdecision.model;
/** 
规则类型

*/
public enum ConditionType
{
	/** 
	 1.大于	 
	*/
	MoreThan(1),

	/** 
	 2.大于或等于	 
	*/
	MoreThanOrEqual(2),

	/** 
	 3.等于	 
	*/
	Equal(3),

	/** 
	 4.小于	 
	*/
	LessThan(4),

	/** 
	 5.小于或等于	 
	*/
	LessThanOrEqual(5),

	/** 
	 6.包含	 
	*/
	Contain(6),

	/** 
	 7.不包含	 
	*/
	NoContain(7);
	
	//必须增加一个构造函数,变量,得到该变量的值
	private int  mConditionType=0;
	private ConditionType(int value)
	{
		mConditionType=value;
	}
	/**
	* @return 枚举变量实际返回值
	*/
	  public int valueOf()
	  {
		  return mConditionType;
	  } 
	public static ConditionType valueOf(int value) {    //    手写的从int到enum的转换函数
        switch (value) {
        case 1:
            return MoreThan;
        case 2:
            return MoreThanOrEqual;
        case 3:
            return Equal;
        case 4:
            return LessThan;
        case 5:
            return LessThanOrEqual;
        case 6:
            return Contain;
        case 7:
            return NoContain;
        default:
            return null;
        }
    }	
}