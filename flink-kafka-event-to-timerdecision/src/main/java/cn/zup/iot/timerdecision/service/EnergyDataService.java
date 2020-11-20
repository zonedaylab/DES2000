package cn.zup.iot.timerdecision.service;

import cn.zup.iot.timerdecision.util.BenchmarkElectricityUtil;
import cn.zup.iot.timerdecision.util.DateUtil;
import cn.zup.iot.timerdecision.util.DispersionRatioUtil;
import cn.zup.iot.timerdecision.dao.DeviceDao;
import cn.zup.iot.timerdecision.dao.DiagnosisDao;
import cn.zup.iot.timerdecision.dao.HisDataDao;
import cn.zup.iot.timerdecision.model.*;
import cn.zup.iot.timerdecision.service.settings.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 存储标杆电量与离散率定时器
 * mxf
 * 2017年10月8日08:07:36
 *
 */
public class EnergyDataService {
	
	@Autowired
	private DiagnosisDao diagnosisDao;
	
	@Autowired
	private HisDataDao hisDataDao;
	
	@Autowired
	private DeviceDao deviceDao;
	
	private static final int v1 = BJLX.changzhan.getValue();
	
	private static final int v2 = ChangZhanParam.daypowers.getValue();
	
	private static final Integer batchType = 0;
	
	private static final Integer regionType = 5;
	
	//private static final Integer timeInterval = 5;//计算组串发电量时间间隔为5分钟
	
	private static  List<Integer> arr = new LinkedList<Integer>();
	
	
	
	private static final int[] currentArr60 = {33,34,35,36,37,38, 39,40,41,42,43};//阳光逆变器60的11路的电流参数
	
	private static final int[] currentArr33 = {33,34,35,36,37,38};//阳光逆变器33的6路电流参数
	
	private static final int[] voltageArr28 = {7, 9};//阳光逆变器电压参数
	
	private static Map<Integer, Integer> bjcsMap26 = new HashMap<Integer, Integer>();
	
	private static Map<Integer, Integer> bjcsMap28 = new HashMap<Integer, Integer>();
	
	static {
		arr = Arrays.asList(4,6,8,10,12,14);
		
		bjcsMap26.put(4, 215);
		bjcsMap26.put(6, 216);
		bjcsMap26.put(8, 217);
		bjcsMap26.put(10, 218);
		bjcsMap26.put(12, 219);
		bjcsMap26.put(14, 220);
		
		bjcsMap28.put(33, 160);
		bjcsMap28.put(34, 161);
		bjcsMap28.put(35, 162);
		bjcsMap28.put(36, 163);
		bjcsMap28.put(37, 164);
		bjcsMap28.put(38, 165);
		bjcsMap28.put(39, 166);
		bjcsMap28.put(40, 167);
		bjcsMap28.put(41, 168);
		bjcsMap28.put(42, 169);
		bjcsMap28.put(43, 170);
	}
	
	
	/**
	 * 计算标杆电量并存库
	 * mxf
	 * 2017年10月6日09:10:25
	 * @throws Exception 
	 */
	public void benchmarkElectricityService (Date date) throws Exception {
		//1.计算标杆电量
		List<ForecastPower> hisDataList = CalBenchmarkElectricity(date);
		//2.存库,调用monitor接口存入 ls.forecastPower
		hisDataDao.insertBenchmarkElectricity(hisDataList, date);
	}
	
	/**
	 * 计算标杆电量
	 * mxf
	 * 2017年10月6日09:22:16
	 * @return
	 * @throws Exception 
	 */
	private synchronized List<ForecastPower> CalBenchmarkElectricity (Date date) throws Exception {
		List<ForecastPower> hisDataList = new LinkedList<ForecastPower>();
		//1.获取所有分组
		List<PmStationGroup> parkList = diagnosisDao.getSubGroup(0);
		//2.计算每个组内的标杆电量
		for(PmStationGroup group : parkList) {
			//获取组内的电站
			List<PmStationGroup> stationList = diagnosisDao.getStationListByGroup(group.getPark_Group_Id());
			double maxValue = 0;
			List<Double> dataList = new LinkedList<Double>();
			for(PmStationGroup station : stationList) {
				//获取组内的电站该天的发电量
				List<HisDataTimeAndValue> list = hisDataDao.getRegionHistPowerData(regionType, Integer.parseInt(station.getStation_Ids()), v1, v2,date,date, batchType);
				//System.err.println("list--------------->" + list);
				if(list != null && list.size() != 0) {
					//该电站该天的发电量
					if(list.get(0) != null) {
						//System.out.println("--------data--------" + list.get(0).getValue());
						dataList.add((double)list.get(0).getValue() / station.getCapacity());
						maxValue = maxValue >= list.get(0).getValue() / station.getCapacity() ? maxValue : list.get(0).getValue() / station.getCapacity();
					}
					
				}
			}
			//计算组内标杆电量
			//3.将组内电站的标杆电量赋值
			for(PmStationGroup station : stationList) {
				Double result = BenchmarkElectricityUtil.getFourEnhancementResult(maxValue, dataList, station.getCapacity());
				ForecastPower forecastPower = new ForecastPower();
				forecastPower.setBuJianCanShuID(ChangZhanParam.benchmarkElectricity.getValue());
				forecastPower.setBuJianID(Integer.parseInt(station.getStation_Ids()));
				forecastPower.setBuJianLeiXingID(RegionType.commdev.getValue());
				forecastPower.setRiqi(date); 
				if(result != null){
					try {
						BigDecimal   b   =   new   BigDecimal(result);
						double   f1   =   b.setScale(1,   BigDecimal.ROUND_HALF_UP).doubleValue();
						forecastPower.setValue(f1);
					}catch(Exception e) {
						forecastPower.setValue(0.0);
					}
				}
				forecastPower.setChangZhanID(Integer.parseInt(station.getStation_Ids()));
			
				hisDataList.add(forecastPower);
			}
		}
		
		return hisDataList;
	}
	
	/**
	 * 计算电站离散率并存库
	 * mxf
	 * 2017年10月9日10:05:14
	 * @throws Exception 
	 */
	public void dispersionRatioService(Date date) throws Exception {
		//计算离散率
		List<ForecastPower> list = calDispersionRatio(date);
		//存库,调用monitor
		hisDataDao.insertBenchmarkElectricity(list, date);
	}
	
	/**
	 * 计算离散率
	 * mxf
	 * 
	 * 2017年10月9日10:06:49
	 * @return
	 * @throws Exception
	 */
	private synchronized List<ForecastPower> calDispersionRatio(Date date) throws Exception {
		
		List<ForecastPower> hisDataList = new LinkedList<ForecastPower>();
		//1.查询出全部电站,暂时只存济南
		List<StationInfo> stationList = deviceDao.getStationInfolist(RegionType.city.getValue(), TopRegion.jinan.getValue());
		//2.依次查询电站各组串当天的23小时各个点的电流值
		for(StationInfo stationInfo : stationList) {
			//获取电站下的逆变器
			List<Commdev> commdevList=deviceDao.getCommdevInfo(String.valueOf(stationInfo.getChangZhanID()),String.valueOf(BJLX.huaweinibianqi.getValue()),null);
			//计算每个组串当日电流的平均值，8点~16点
			List<Double> listValue = new ArrayList<Double>();//每个逆变器的各组串电流
			for(Commdev commdev : commdevList) {
				//传递参数			
				//查询该逆变器有哪个组串工作
				List<Integer> dianliuList = deviceDao.getWorkedZuChuan(commdev.getID(), arr);
				List<BuJianParam> listParam = genBuJianParamList(commdev.getID(), dianliuList);
				List<HisDataTimeAndValue> list = hisDataDao.GetStationHisDataMulti(listParam, date);
				
				for(int m=0;m<list.size();m++) {
					double value = 0;//每个组串的平均值
					for(int i=8;i<=16;i++) {
						value += list.get(m).getListValue().get(i);
					}
					listValue.add(value / 9);
				}
				
			}
			//5.计算所有组串的平均值
			double total = 0;
			for (int i=0;i<listValue.size();i++) {
				total += listValue.get(i);
			}
			
			double avg = total / listValue.size();
//			System.out.println("---------listValue---------" + listValue);
//			System.out.println("---------avg---------" + avg);
			//6.计算离散率
			Double despersionRatio = DispersionRatioUtil.calDispersionRatio(listValue, avg);
			//封装dto
			ForecastPower forecastPower = new ForecastPower();
			forecastPower.setBuJianCanShuID(ChangZhanParam.dispersionRatio.getValue());
			forecastPower.setBuJianID(stationInfo.getChangZhanID());
			forecastPower.setBuJianLeiXingID(RegionType.commdev.getValue());
			forecastPower.setRiqi(date); 
			if(despersionRatio != null){
				try{
					//System.out.println("------------->"+despersionRatio);
					BigDecimal   b   =   new   BigDecimal(despersionRatio);  
					double   f1   =   b.setScale(3,   BigDecimal.ROUND_HALF_UP).doubleValue();
					forecastPower.setValue(f1);
				}catch (Exception e) {
					forecastPower.setValue(0.0);
					System.out.println("----------------->" + stationInfo.getChangZhanID());
					for(double l : listValue) {
						System.out.println("---------->" +l);
					}
					e.printStackTrace();
				}
			}
			forecastPower.setChangZhanID(stationInfo.getChangZhanID());
			hisDataList.add(forecastPower);
		}
		
		return hisDataList;
		
		
		
	}
	
	/**
	 * 计算每个逆变器每个时间点各组串离散率
	 * mxf
	 * 
	 * 2017年10月9日10:06:49
	 * @return
	 * @throws Exception
	 */
	private synchronized List<ForecastPower> calDispersionRatio_Hour(Date date) throws Exception {
//		Date mydate = new Date();
//		int hours = mydate.getHours();//获取当前时间小时数
//		
//		Calendar c = Calendar.getInstance();//可以对每个时间域单独修改
//		c.setTime(new Date());
//		c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY) - 1);
		
		int hours = date.getHours() - 1;
		
		List<ForecastPower> hisDataList = new LinkedList<ForecastPower>();
		//1.查询出全部电站,暂时只存济南
		List<StationInfo> stationList = deviceDao.getStationInfolist(RegionType.city.getValue(), TopRegion.jinan.getValue());
		//2.依次查询电站各组串当天的23小时各个点的电流值
		for(StationInfo stationInfo : stationList) {
			//获取电站下的逆变器
			List<Commdev> commdevList=deviceDao.getCommdevInfo(String.valueOf(stationInfo.getChangZhanID()),String.valueOf(BJLX.huaweinibianqi.getValue()),null);
			//计算每个组串当日电流的平均值，7点~18点
			List<Double> listValue = new ArrayList<Double>();//每个逆变器的各组串电流
			for(Commdev commdev : commdevList) {
				//传递参数			
				//查询该逆变器有哪个组串工作
				List<Integer> dianliuList = deviceDao.getWorkedZuChuan(commdev.getID(), arr);
				List<BuJianParam> listParam = genBuJianParamList(commdev.getID(), dianliuList);
				List<HisDataTimeAndValue> list = hisDataDao.GetStationHisDataMulti(listParam, date);
				
				for(int m=0;m<list.size();m++) {
					//获取当前时间点
					//double value = list.get(m).getListValue().get(hours-1);
					double value = list.get(m).getListValue().get(hours);
					listValue.add(value);
				}
				
			}
			//5.计算该逆变器所有组串的和
			double total = 0;
			for (int i=0;i<listValue.size();i++) {
				total += listValue.get(i);
			}
			double avg = total / listValue.size();
			
			//6.计算离散率
			Double despersionRatio = DispersionRatioUtil.calDispersionRatio(listValue, avg);
			//封装dto
			ForecastPower forecastPower = new ForecastPower();
			forecastPower.setBuJianCanShuID(ChangZhanParam.dispersionRatio_hour.getValue());
			forecastPower.setBuJianID(stationInfo.getChangZhanID());
			forecastPower.setBuJianLeiXingID(RegionType.commdev.getValue());
			forecastPower.setRiqi(date); 
			if(despersionRatio != null){
				try{
					//System.out.println("------------->"+despersionRatio);
					BigDecimal   b   =   new   BigDecimal(despersionRatio);  
					double   f1   =   b.setScale(3,   BigDecimal.ROUND_HALF_UP).doubleValue();
					forecastPower.setValue(f1);
				}catch (Exception e) {
					//System.out.println("----------------->" + stationInfo.getChangZhanID());
//					for(double l : listValue) {
//						System.out.println("---------->" +l);
//					}
					forecastPower.setValue(0.00);
					e.printStackTrace();
				}
			}
			forecastPower.setChangZhanID(stationInfo.getChangZhanID());
			hisDataList.add(forecastPower);
			
		}
		
		return hisDataList;
	}
	
	/**
	 * 计算电站离散率并存库
	 * mxf
	 * 2017年10月9日10:05:14
	 * @throws Exception 
	 */
	public synchronized void dispersionRatioService_Hour(Date date) throws Exception {
		//计算离散率
		List<ForecastPower> list = calDispersionRatio_Hour(date);
		//存库,调用monitor
		hisDataDao.insertBenchmarkElectricity(list, date);
	}
	
	/**
	 * 计算小时标杆电量并存库
	 * mxf
	 * 2017年10月6日09:10:25
	 * @throws Exception 
	 */
	public synchronized void benchmarkElectricityService_Hour (Date date) throws Exception {
		//1.计算标杆电量
		List<ForecastPower> hisDataList = CalBenchmarkElectricity_Hour(date);
		//2.存库,调用monitor接口存入 ls.forecastPower
		hisDataDao.insertBenchmarkElectricity(hisDataList, date);
	}
	
	/**
	 * 计算小时标杆电量
	 * mxf
	 * 2017年12月9日14:36:22
	 * @return
	 * @throws Exception 
	 */
	private synchronized List<ForecastPower> CalBenchmarkElectricity_Hour (Date date) throws Exception {
		//获取上一个小时的时间
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);//date 换成已经已知的Date对象
        cal.add(Calendar.HOUR_OF_DAY, -1);// before 1 hour
        Date lastHour = cal.getTime();
		List<ForecastPower> hisDataList = new LinkedList<ForecastPower>();
		//1.获取所有分组
		List<PmStationGroup> parkList = diagnosisDao.getSubGroup(0);
		Map<String, Double> map = new HashMap<String, Double>();
		//2.计算每个组内的标杆电量
		for(PmStationGroup group : parkList) {
			//获取组内的电站
			List<PmStationGroup> stationList = diagnosisDao.getStationListByGroup(group.getPark_Group_Id());
			double maxValue = 0;
			List<Double> dataList = new LinkedList<Double>();
			for(PmStationGroup station : stationList) {
				//获取组内的电站该天的发电量
				//获取这个时间点与上个小时的发电量，做差值
				String value = hisDataDao.getRegionPowerData(regionType, Integer.parseInt(station.getStation_Ids()),BJLX.changzhan.getValue(),
						ChangZhanParam.daypowers.getValue(), InfoType.diandu.getValue(), batchType, date);
				//上一个小时的发电量
				String lastHourValue = hisDataDao.getRegionPowerData(regionType, Integer.parseInt(station.getStation_Ids()),BJLX.changzhan.getValue(),
						ChangZhanParam.daypowers.getValue(), InfoType.diandu.getValue(), batchType, lastHour);
				double v1 = 0;
				double v2 = 0;
				if(value != null && value != "" ) {
					v1 = Double.parseDouble(value);
				}
				if(lastHourValue != null && lastHourValue != "") {
					v2 = Double.parseDouble(lastHourValue);
				}
				//小时的发电量
				double v = v1 - v2; 
				//System.err.println("list--------------->" + list);
				
				//System.out.println("--------data--------" + list.get(0).getValue());
				dataList.add(v / station.getCapacity());
				maxValue = maxValue >= v / station.getCapacity() ? maxValue : v / station.getCapacity();
				//小时发电量
				map.put(station.getStation_Ids(), v);
			}
			//计算组内标杆电量
			//3.将组内电站的标杆电量赋值
			for(PmStationGroup station : stationList) {
				Double result = BenchmarkElectricityUtil.getFourEnhancementResult(maxValue, dataList, station.getCapacity());
				ForecastPower forecastPower = new ForecastPower();
				forecastPower.setBuJianCanShuID(ChangZhanParam.benchmarkElectricity_hour.getValue());
				forecastPower.setBuJianID(Integer.parseInt(station.getStation_Ids()));
				forecastPower.setBuJianLeiXingID(RegionType.commdev.getValue());
				forecastPower.setRiqi(date);
				forecastPower.setHourPower(map.get(station.getStation_Ids()));
				System.out.println("result-------->" + result);
				if(result != null){
					try {
						BigDecimal   b   =   new   BigDecimal(result);  
						double   f1   =   b.setScale(1,   BigDecimal.ROUND_HALF_UP).doubleValue();
						forecastPower.setValue(f1);
					}catch (Exception e){
						forecastPower.setValue(0.0);
					}
				}
				forecastPower.setChangZhanID(Integer.parseInt(station.getStation_Ids()));
			
				hisDataList.add(forecastPower);
			}
		}
		
		return hisDataList;
	}
	
	 /**
	  * 生成逆变器组串查询条件list
	  * mxf
	  * 2017年10月7日10:52:04
	  * @return
	  */
	private static List<BuJianParam> genBuJianParamList(int bujianId, List<Integer> list) {
		List<BuJianParam> listParam = new ArrayList<BuJianParam>();
		
		for(int i=0;i<list.size();i++) {
			BuJianParam bujianParam = new BuJianParam("26",String.valueOf(list.get(i)),String.valueOf(bujianId));
			listParam.add(bujianParam);
		}
		
		return listParam;
	}

	/**
	 * 计算组串发电量，早5点至晚8点
	 * mxf
	 * @throws ParseException 
	 */
	private synchronized List<ForecastPower> groupStringPowerService(Date date) throws ParseException {
		
		//查询伏凌和洪楼的电站的逆变器
		List<Commdev> hongjialou_commdevList = deviceDao.getCommdevList(RegionType.village.getValue(), TopRegion.hongjialou.getValue());
		List<Commdev> fuling_commdevList = deviceDao.getCommdevList(RegionType.city.getValue(), TopRegion.hangzhou.getValue());
		List<Commdev> commdevList = new ArrayList<Commdev>();
		commdevList.addAll(hongjialou_commdevList);
		commdevList.addAll(fuling_commdevList);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(date);
		Date d = DateUtil.getDate2(dateStr + " 00:00:00");
		List<ForecastPower> result = new ArrayList<ForecastPower>();
		
		for(Commdev item : commdevList) {
			//判断逆变器的类型
			//若为华为逆变器，pv1~pv6电压参数为3,5,7,9,11,13    pv1~pv6电流参数为4,6,8,10,12,14
			//若为阳光逆变器，若为33型号的逆变器，电流参数为33,34,35,36,37,38    电压参数为7,9；若为60型号的逆变器，电流参数为33,34,35,36,37,38，39,40,41,42,43    电压参数为7
			if(item.getChangZhanID() == 530) {
				System.out.println("================");
			}
			if(item.getBJLXID() == BJLX.huaweinibianqi.getValue()) {
				//华为逆变器
				for(int i : arr) {
					ForecastPower forecastPower = new ForecastPower();
					float p = 0;
					List<HisDataTimeAndValue> dianliuList = hisDataDao.getGroupStringData(BJLX.huaweinibianqi.getValue(), i, item.getID(), date);//电流
					List<HisDataTimeAndValue> dianyaList = hisDataDao.getGroupStringData(BJLX.huaweinibianqi.getValue(), i-1, item.getID(), date);//电压
					
					for(int m = 0; m< dianliuList.size(); m++) {
						List<Float> dianlius = dianliuList.get(m).getListValue();
						List<Float> dianyas = dianyaList.get(m).getListValue();
						
						for(int n = 7;n < 21;n++) {
							float dianliu = dianlius.get(n);
							float dianya = dianyas.get(n);
							p += dianliu * dianya;
						}
					}
					
					forecastPower.setBuJianCanShuID(bjcsMap26.get(i));
					forecastPower.setBuJianID(item.getID());
					forecastPower.setBuJianLeiXingID(item.getBJLXID());
					forecastPower.setChangZhanID(item.getChangZhanID());
					forecastPower.setRiqi(d);
					BigDecimal   b   =   new   BigDecimal(p/12/1000);  
					double   f1   =   b.setScale(3,   BigDecimal.ROUND_HALF_UP).doubleValue();
					forecastPower.setValue(f1);
					
					result.add(forecastPower);
				}
				
			} else {
				//阳光逆变器
				//判断型号
				if("33".equals(item.getShuoMing())) {
					
					for(int i : currentArr33) {
						ForecastPower forecastPower = new ForecastPower();
						float p = 0;
						List<HisDataTimeAndValue> dianliuList = null;
						List<HisDataTimeAndValue> dianyaList = null;
						
						dianliuList = hisDataDao.getGroupStringData(BJLX.yangguangnibianqi.getValue(), i, item.getID(), date);//电流
						if(i < 36) {
							dianyaList = hisDataDao.getGroupStringData(BJLX.yangguangnibianqi.getValue(), voltageArr28[0], item.getID(), date);//电压
						} else {
							dianyaList = hisDataDao.getGroupStringData(BJLX.yangguangnibianqi.getValue(), voltageArr28[1], item.getID(), date);//电压
						}
						
						for(int m = 0; m< dianliuList.size(); m++) {
							List<Float> dianlius = dianliuList.get(m).getListValue();
							List<Float> dianyas = dianyaList.get(m).getListValue();
							
							for(int n = 7;n < 21;n++) {
								float dianliu = dianlius.get(n);
								float dianya = dianyas.get(n);
								p += dianliu * dianya;
							}
						}
						
						forecastPower.setBuJianCanShuID(bjcsMap28.get(i));
						forecastPower.setBuJianID(item.getID());
						forecastPower.setBuJianLeiXingID(item.getBJLXID());
						forecastPower.setChangZhanID(item.getChangZhanID());
						forecastPower.setRiqi(d);
						BigDecimal   b   =   new   BigDecimal(p/12/1000);  
						double   f1   =   b.setScale(3,   BigDecimal.ROUND_HALF_UP).doubleValue();
						forecastPower.setValue(f1);
						
						result.add(forecastPower);
					}
				} else if ("60".equals(item.getShuoMing())) {
					for(int i : currentArr60) {
						ForecastPower forecastPower = new ForecastPower();
						float p = 0;
						List<HisDataTimeAndValue> dianliuList = hisDataDao.getGroupStringData(BJLX.yangguangnibianqi.getValue(), i, item.getID(), date);//电流
						List<HisDataTimeAndValue> dianyaList = hisDataDao.getGroupStringData(BJLX.yangguangnibianqi.getValue(), voltageArr28[0], item.getID(), date);//电压
						
						for(int m = 0; m< dianliuList.size(); m++) {
							List<Float> dianlius = dianliuList.get(m).getListValue();
							List<Float> dianyas = dianyaList.get(m).getListValue();
							
							for(int n = 7;n < 21;n++) {
								float dianliu = dianlius.get(n);
								float dianya = dianyas.get(n);
								p += dianliu * dianya;
							}
						}
						
						forecastPower.setBuJianCanShuID(bjcsMap28.get(i));
						forecastPower.setBuJianID(item.getID());
						forecastPower.setBuJianLeiXingID(item.getBJLXID());
						forecastPower.setChangZhanID(item.getChangZhanID());
						forecastPower.setRiqi(d);
						BigDecimal   b   =   new   BigDecimal(p/12/1000);  
						double   f1   =   b.setScale(3,   BigDecimal.ROUND_HALF_UP).doubleValue();
						forecastPower.setValue(f1);
						
						result.add(forecastPower);
					}
				}
			}
		}
		
		return result;
		
	}
	
	/**
	 * 计算组串发电量并存库
	 * mxf
	 * @throws ParseException
	 */
	public void calGroupStringPower() throws ParseException {
//		for(int i = 1; i<= 31; i++) {
//			String dateStr = "2018-03-" + i;
//			Date date = DateUtil.getDate(dateStr);
//			//Date date = new Date();
//			List<ForecastPower> result = groupStringPowerService(date);
////			for(ForecastPower item : result) {
////				System.out.println(item.getBuJianLeiXingID() + "==" + item.getBuJianID() + "==" + item.getBuJianCanShuID() + "==" + item.getRiqi());
////			}
//			//存库,调用monitor
//			hisDataDao.insertBenchmarkElectricity(result, date);
//		}
		
//		String dateStr = "2018-02-08";
//		Date date = DateUtil.getDate(dateStr);
		Date date = new Date();
		List<ForecastPower> result = groupStringPowerService(date);
//		for(ForecastPower item : result) {
//			System.out.println(item.getBuJianLeiXingID() + "==" + item.getBuJianID() + "==" + item.getBuJianCanShuID() + "==" + item.getRiqi());
//		}
		//存库,调用monitor
		hisDataDao.insertBenchmarkElectricity(result, date);
		
	}
	
	public void goTest() throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		for(int i=1;i<=31;i++) {
			for(int m = 7;m <= 18; m++) {
				String str ="2017-12-" + i +  " " + m + ":00:00";
				Date date = sdf.parse(str);
				dispersionRatioService_Hour(date);
				benchmarkElectricityService_Hour(date);
			}
//			String str ="2017-9-" + i +  " 21:00:00";
//			Date date = sdf.parse(str);
//			dispersionRatioService(date);
//			benchmarkElectricityService(date);
		}
	}
	
	/**
	 * 计算天数据,离散率、标杆电量
	 * mxf
	 * @throws Exception
	 */
	public void calDayData() throws Exception{
		Date date = new Date();
		dispersionRatioService(date);
		benchmarkElectricityService(date);
		
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		
//		for(int i=1;i<=31;i++) {
//			String str ="2018-03-" + i +  " 21:00:00";
//			Date date = sdf.parse(str);
//			dispersionRatioService(date);
//			benchmarkElectricityService(date);
//		}
	}
	
	/**
	 * 计算小时数据，小时离散率、小时标杆电量，小时发电量
	 * mxf
	 * @throws Exception
	 */
	public void calHourData() throws Exception {
		Date date = new Date();
		//获取上一个小时的时间
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//Date date = sdf.parse("2018-01-24 13:00:00");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);//date 换成已经已知的Date对象
        cal.add(Calendar.HOUR_OF_DAY, -1);// before 1 hour
        Date lastHour = cal.getTime();
		dispersionRatioService_Hour(lastHour);
		benchmarkElectricityService_Hour(lastHour);
		
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		
//		for(int i=1;i<=31;i++) {
//			for(int m = 7;m <= 18; m++) {
//				String str ="2018-03-" + i +  " " + m + ":00:00";
//				Date date = sdf.parse(str);
//				dispersionRatioService_Hour(date);
//				benchmarkElectricityService_Hour(date);
//			}
////			String str ="2017-9-" + i +  " 21:00:00";
////			Date date = sdf.parse(str);
////			dispersionRatioService(date);
////			benchmarkElectricityService(date);
//		}
	}

}
