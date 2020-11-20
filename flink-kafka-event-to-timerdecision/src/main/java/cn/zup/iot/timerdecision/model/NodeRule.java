package cn.zup.iot.timerdecision.model;

public class NodeRule {

	private Integer RULE_ID;
	private Integer ACTIVITY_ID;
	private Integer GOTO_ACTIVITY;
	private Integer GOTO_ACTIVITY_CODE;

	private Integer CONDITIONS; //判断条件 > < ==

	private String JUDGE_VALUE; //判据 true false ,data

	public void  setRULE_ID(Integer RULE_ID)
	{
		this.RULE_ID=RULE_ID;
	}
	public  Integer getRULE_ID()
	{
		 return RULE_ID;
	}

	public void  setACTIVITY_ID(Integer ACTIVITY_ID)
	{
		this.ACTIVITY_ID=ACTIVITY_ID;
	}
	public  Integer getACTIVITY_ID()
	{
		 return ACTIVITY_ID;
	}
	/// <summary>
	/// 
	/// </summary>
	public void  setGOTO_ACTIVITY(Integer GOTO_ACTIVITY)
	{
		this.GOTO_ACTIVITY=GOTO_ACTIVITY;
	}
	public  Integer getGOTO_ACTIVITY()
	{
		 return GOTO_ACTIVITY;
	}
	/// <summary>
	/// 
	/// </summary>
	public void  setGOTO_ACTIVITY_CODE(Integer GOTO_ACTIVITY_CODE)
	{
		this.GOTO_ACTIVITY_CODE=GOTO_ACTIVITY_CODE;
	}
	public  Integer getGOTO_ACTIVITY_CODE()
	{
		 return GOTO_ACTIVITY_CODE;
	}

	/// <summary>
	/// 
	/// </summary>
	public void  setCONDITIONS(Integer CONDITIONS)
	{
		this.CONDITIONS=CONDITIONS;
	}
	public  Integer getCONDITIONS()
	{
		 return CONDITIONS;
	}

	/// <summary>
	/// 
	/// </summary>
	public void  setJUDGE_VALUE(String JUDGE_VALUE)
	{
		this.JUDGE_VALUE=JUDGE_VALUE;
	}
	public  String getJUDGE_VALUE()
	{
		 return JUDGE_VALUE;
	}

}

