package cn.zup.iot.timerdecision.model;

/**
 * 决策树的返回结果集
 * @author samson
 *
 */
public class DecisionResult {
	String actrualValue; //返回标识
	String message; //返回信息
	public String getActrualValue() {
		return actrualValue;
	}
	public void setActrualValue(String actrualValue) {
		this.actrualValue = actrualValue;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
