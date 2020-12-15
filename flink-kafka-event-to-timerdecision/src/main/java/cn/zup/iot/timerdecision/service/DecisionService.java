package cn.zup.iot.timerdecision.service;

import cn.zup.iot.timerdecision.dao.DecisionDao;
import cn.zup.iot.timerdecision.model.*;

import java.util.Date;
import java.util.List;

public class DecisionService {

	
	private DecisionDao decisionDao;
	
	public DecisionDao getDecisionDao() {
		return decisionDao;
	}



	public void setDecisionDao(DecisionDao decisionDao) {
		this.decisionDao = decisionDao;
	}



	/**
	 * 获取电站状态 主要是告警信息 
	 * @param stationId
	 * @param dateParam
	 * @return
	 */
	public List<PmWarnRecord> getStationStatus(Integer stationId,Date dateParam)
	{
		return decisionDao.getStationStatus(stationId, dateParam);
	}
	

	/**
	 * 获取电站 逆变器异常信息 
	 * @param stationId
	 * @param bujianType
	 * @return
	 */
	public List<DeviceInfo> getStationDeviceInfo(Integer stationId, Integer bujianType)
	{
		return decisionDao.getStationDeviceInfo(stationId,0);
	}

	/**
	 * 获取直流侧电流和电压的数据 
	 * @param buJianId
	 * @param buJianType
	 * @param buJianCanshu
	 * @param type
	 * @param dateParam
	 * @return
	 */
	public List<YCInfoData> getPVData(int buJianId, int buJianType,String buJianCanshu,int type,Date dateParam)
	{
		return decisionDao.getPVData(buJianId,buJianType,buJianCanshu,type,dateParam);
	}

	/**
	 * 根据传递的电站获取电站电量
	 * @param stationIds
	 * @param dateParam
	 * @return
	 */
	public List<PowerDayStat> getGroupStationList(String stationIds,Date dateParam,int orderType)
	{
		return decisionDao.getGroupStationList(stationIds,dateParam,orderType);
	}
	public List<PowerStatData> getGroupStationPagingList(String stationIds, Date dateParam, int orderType, int page, int rows)
	{
		return decisionDao.getGroupStationPagingList(stationIds,dateParam,orderType,page,rows);
	}
	/**
	 * 获取分组的平均值
	 * @param stationIds
	 * @param dateParam
	 * @return
	 */
	public HisDataTimeAndValue getGroupAVG(String stationIds,Date dateParam)
	{
		return decisionDao.getGroupAVG(stationIds,dateParam);
	}
	
}
