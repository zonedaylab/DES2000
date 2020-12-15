package cn.zup.iot.timerdecision.service;

import cn.zup.iot.timerdecision.dao.*;
import cn.zup.iot.timerdecision.model.*;
import cn.zup.iot.timerdecision.service.settings.*;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class DataAdressService {

    private DecisionDao decisionDao = new DecisionDao();
    private HisDataDao hisDataDao = new HisDataDao();
    private DiagnosisDao diagnosisDao = new DiagnosisDao();
    private DeviceDao deviceDao = new DeviceDao();
    private ScadaWarnPushDao scadaWarnPushDao = new ScadaWarnPushDao();
    /**
     * 获取电站 逆变器异常信息
     *
     * @param
     * @param dateParam
     * @return
     */
    public DecisionResult getDeviceOnlineState(Device device, Date dateParam) {
        DecisionResult result = new DecisionResult();
        String flag = "0";
        String msg = "";  //获取电站区域名称 乡镇名称 电站名称
        boolean niflag = false;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        int hour = Integer.parseInt(sdf.format(date));
        List<DeviceInfo> deviceList = decisionDao.getDeviceInfo(device.getId(), 6, BJLX.yuanshendianbiao.getValue());
        if (deviceList != null && deviceList.size() > 0) {
            msg += deviceList.get(0).getProvinceName() + deviceList.get(0).getCityName() + deviceList.get(0).getStationName() + " : ";
            for (DeviceInfo deviceInfo : deviceList) {
                //取出获取设备（电站下所有电表的）的小时电量
                List<HisDataTimeAndValue> hourList = hisDataDao.getRegionHoursMinuteData(6, deviceInfo.getDeviceId(), BJLX.yuanshendianbiao.getValue(), 11, dateParam, dateParam);
                if (hourList == null || hourList.size() == 0) {
                    niflag = true;
                    msg += deviceInfo.getDeviceName() + "电表数据未采集; ";
                }
                if (hourList != null && hourList.size() > 0) {
                    float value = hourList.get(hour - 4).getValue();
                    if (hourList.get(hour - 3).getValue() == value && hourList.get(hour - 2).getValue() == value && hourList.get(hour - 1).getValue() == value) {
                        niflag = true;
                        msg += deviceInfo.getDeviceName() + "电表数据连续4小时未变，现场跳闸或其他原因,请派运维人员及时查看; ";
                    }
                }
            }
        }
        if (niflag) {
            flag = "1";
        }
        result.setActrualValue(flag);
        if (flag.endsWith("1"))
            result.setMessage(msg);
        return result;
    }

    /**
     * 获取设备当前告警
     *
     * @param device
     * @param dateParam
     * @return
     */
    public DecisionResult getDeviceNowState(Device device, Date dateParam) {
        DecisionResult result = new DecisionResult();
        String flag = "0";
        String msg = "";  //获取电站区域名称 乡镇名称 电站名称
        boolean niflag = false;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        int hour = Integer.parseInt(sdf.format(date));
        List<DeviceInfo> deviceList = decisionDao.getDeviceInfo(device.getId(), 6, BJLX.yuanshendianbiao.getValue());
        if (deviceList != null && deviceList.size() > 0) {
            msg += deviceList.get(0).getProvinceName() + deviceList.get(0).getCityName() + deviceList.get(0).getStationName() + " : ";
            for (DeviceInfo deviceInfo : deviceList) {
                //取出获取设备（电站下所有电表的）的小时电量

                List<PmWarnRecord> warnList = scadaWarnPushDao.GetDeviceWarnRecordList(deviceInfo.getDeviceId(), 4, 1, 2);
                if (warnList != null && warnList.size() > 0) {
                    niflag = true;
                    msg += deviceInfo.getDeviceName() + "电站已有scada通讯告警; ";
                }
            }
        }
        if (niflag) {
            flag = "1";
        }
        result.setActrualValue(flag);
        if (flag.endsWith("1"))
            result.setMessage(msg);
        return result;
    }

    /**
     * 获取电表（设备）通讯状态
     *
     * @param
     * @param dateParam
     * @return
     */
    public DecisionResult getDeviceNetState(Device device, Date dateParam) {
        DecisionResult result = new DecisionResult();
        String flag = "0";
        String msg = "";  //获取电站区域名称 乡镇名称 电站名称
        boolean niflag = false;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        int hour = Integer.parseInt(sdf.format(date));
        List<DeviceInfo> deviceList = decisionDao.getDeviceInfo(device.getId(), 6, BJLX.yuanshendianbiao.getValue());
        if (deviceList != null && deviceList.size() > 0) {
            msg += deviceList.get(0).getProvinceName() + deviceList.get(0).getCityName() + deviceList.get(0).getStationName() + " : ";
            for (DeviceInfo deviceInfo : deviceList) {
                //取出获取设备（电表）的通信时间
                List<HisDataTimeAndValue> hourList = hisDataDao.getRegionHoursTime(6, deviceInfo.getDeviceId(), BJLX.yuanshendianbiao.getValue(), 15, dateParam, dateParam);
                if (hourList != null && hourList.size() > 0) {
                    long value = hourList.get(hour - 3).getTime();
                    Date endDate = new Date(value * 1000);
                    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if (hourList.get(hour - 2).getTime() == value && hourList.get(hour - 1).getTime() == value && hourList.get(hour).getTime() == value) {
                        niflag = true;
                        msg += deviceInfo.getDeviceName() + "通讯中断，最后通讯时间为:" + sd.format(endDate);
                    }
                }
            }
        }
        if (niflag) {
            flag = "1";
        }
        result.setActrualValue(flag);
        if (flag.endsWith("1"))
            result.setMessage(msg);
        return result;
    }


    /***
     * 获取电站状态信息
     * @param device
     * @param dateParam
     */
    public DecisionResult getStationStatus(Device device, Date dateParam) {
        DecisionResult result = new DecisionResult();
        result.setActrualValue("0");//设置0表示电站正常
        //List<PmWarnRecord> record = decisionDao.getStationStatus(device.getId(), dateParam);//原方法
        //获取告警记录
        List<PmWarnRecord> record = decisionDao.getStationWarnStatus(device.getId(), dateParam);//修改后方法
        if (record != null && record.size() > 0) {
            result.setActrualValue("1");  //如果记录不为空表示返回电站异常
            String msg = record.get(0).getProvinceName() + record.get(0).getCityName() + record.get(0).getStation_Name() + " : ";  //获取电站区域名称 乡镇名称 电站名称
            for (PmWarnRecord item : record) {
                msg += item.getEquipment_Name() + "_" + item.getWarn_Name() + "; ";//获取电站告警信息
            }
            result.setMessage(msg);
        }
        return result;
    }

    /***
     * 获取电站状态信息
     * @param device
     * @param dateParam
     */
    public DecisionResult getStationWarn(Device device, Date dateParam) {
        DecisionResult result = new DecisionResult();
        result.setActrualValue("0");//表示电站正常
        //List<PmWarnRecord> record = decisionDao.getStationStatus(device.getId(), dateParam);//原方法
        List<PmWarnRecord> record = decisionDao.getStationWarn(device.getId(), dateParam);//修改后方法
        if (record != null && record.size() > 0) {
            result.setActrualValue("1");  //表示返回电站异常
            String msg = record.get(0).getProvinceName() + record.get(0).getCityName() + record.get(0).getStation_Name() + " : ";  //获取电站区域名称 乡镇名称 电站名称
            for (PmWarnRecord item : record) {
                msg += item.getEquipment_Name() + "_" + item.getWarn_Name() + "; ";//获取电站告警信息
            }
            result.setMessage(msg);
        }
        return result;
    }

    /***
     * 获取电站状态信息
     * @param device
     * @param dateParam
     */
        public DecisionResult getStationNetStatus(Device device, Date dateParam) {
        DecisionResult result = new DecisionResult();
        result.setActrualValue("0");//表示电站正常
        //List<PmWarnRecord> record = decisionDao.getStationStatus(device.getId(), dateParam);//原方法
        List<PmWarnRecord> record = decisionDao.getStationNetStatus(device.getId(), dateParam);//修改后方法
        if (record != null && record.size() > 0) {
            result.setActrualValue("1");  //表示返回电站异常
            String msg = record.get(0).getProvinceName() + record.get(0).getCityName() + record.get(0).getStation_Name() + " : ";  //获取电站区域名称 乡镇名称 电站名称
            for (PmWarnRecord item : record) {
                msg += item.getEquipment_Name() + "_" + item.getWarn_Name() + "; ";//获取电站告警信息
            }
            result.setMessage(msg);
        }
        return result;
    }

    /**
     * 获取电站 逆变器异常信息
     *
     * @param device
     * @param dateParam
     * @return
     */
    public DecisionResult getStationDeviceStatus(Device device, Date dateParam) {
        DecisionResult result = new DecisionResult();
        String flag = "0";
        String msg = "";  //获取电站区域名称 乡镇名称 电站名称
        decisionDao = new DecisionDao();
        //上海伏凌部件类型为26
        List<DeviceInfo> deviceList = decisionDao.getStationDeviceInfo(device.getId(), 0);
        if (deviceList != null && deviceList.size() > 0) {
            msg += deviceList.get(0).getProvinceName() + deviceList.get(0).getCityName() + deviceList.get(0).getStationName() + " : ";
            for (DeviceInfo deviceInfo : deviceList) {

                boolean niflag = true;
                //获取逆变器的小时电量
                List<HisDataTimeAndValue> hourList = hisDataDao.getRegionCumulativeHours(6, deviceInfo.getDeviceId(), 26, 99, dateParam, dateParam);
                if (hourList != null && hourList.size() > 0) {
                    float value = hourList.get(0).getValue();
                    for (HisDataTimeAndValue item : hourList) {
                        if (item.getValue() > 0 && item.getValue() != value) {
                            niflag = false;
                            break;
                        }
                    }
                    if (niflag) {
                        flag = "1";
                        msg += deviceInfo.getDeviceName() + "通讯异常; ";
                    }

                }
            }
        }
        result.setActrualValue(flag);
        if (flag.endsWith("1"))
            result.setMessage(msg);
        return result;
    }

    /**
     * 获取电站 逆变器异常信息
     *
     * @param device
     * @param dateParam
     * @return
     */
    public DecisionResult getStationDeviceNetState(Device device, Date dateParam) {
        DecisionResult result = new DecisionResult();
        String flag = "0";
        String msg = "";  //获取电站区域名称 乡镇名称 电站名称
        boolean niflag = false;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        int hour = Integer.parseInt(sdf.format(date));
        decisionDao = new DecisionDao();
        List<DeviceInfo> deviceList = decisionDao.getStationDeviceInfo(device.getId(), BJLX.yuanshendianbiao.getValue());
        if (deviceList != null && deviceList.size() > 0) {
            msg += deviceList.get(0).getProvinceName() + deviceList.get(0).getCityName() + deviceList.get(0).getStationName() + " : ";
            for (DeviceInfo deviceInfo : deviceList) {
                //取出获取设备（电站下所有电表的）的小时电量
                hisDataDao = new HisDataDao();
                List<HisDataTimeAndValue> hourList = hisDataDao.getRegionHoursData(6, deviceInfo.getDeviceId(), BJLX.yuanshendianbiao.getValue(), 11, dateParam, dateParam);
                if (hourList == null || hourList.size() == 0) {
                    niflag = true;
                    msg += deviceInfo.getDeviceName() + "数据未采集; ";
                }
                if (hourList != null && hourList.size() > 0) {
                    float value = hourList.get(hour - 3).getValue();
                    if (hourList.get(hour - 2).getValue() == value && hourList.get(hour - 1).getValue() == value && hourList.get(hour).getValue() == value) {
                        niflag = true;
                        msg += deviceInfo.getDeviceName() + "通讯异常; ";
                    }
                }
            }
        }
        if (niflag) {
            flag = "1";
        }
        result.setActrualValue(flag);
        if (flag.endsWith("1"))
            result.setMessage(msg);
        return result;
    }

    /***
     * 获取电站直流侧电压和电流异常信息
     * @param device
     * @param dateParam
     */
    public DecisionResult getPVData(Device device, Date dateParam) {
        DecisionResult result = new DecisionResult();
        String msg = "";
        String flag = "0";
        //获取近七日天气信息
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat df = new DecimalFormat("######0.00");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(calendar.DATE, -7);//把日期往后增加一天.整数往后推,负数往前移动
        dateParam = calendar.getTime(); //这个时间就是日期往后推一天的结果
        String dateString = sdf.format(dateParam);
        //		System.out.println(dateString);
        //获取天气情况
        List<WeatherInfo> weatherList = diagnosisDao.GetWeatherDateList(dateString);
        //获取电站下逆变器设备
        //getDeviceInfo（）的参数和逆变器类型的参数一样，26
        List<DeviceInfo> deviceList = decisionDao.GetDeviceInfo(device.getId());
        if (deviceList.size() > 0) {
            msg = deviceList.get(0).getProvinceName() + deviceList.get(0).getCityName() + deviceList.get(0).getStationName() + " : ";  //获取电站区域名称 乡镇名称 电站名称

            //遍历设备判断电流为0
            for (DeviceInfo item : deviceList) {
                if (weatherList.size() == 0)
                    break;

                Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
                today.setTimeInMillis(Calendar.getInstance().getTimeInMillis()); //获取前minuteParam数据
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                Date weatherDate = today.getTime();
                List<YCInfoData> ycList = new ArrayList<YCInfoData>();

                List<YCInfoData> YCInfoDataList = new ArrayList<YCInfoData>();
                //获取weather日期的所有整点数据0 1 2 3...23
                for (int i = 0; i < 24; i++) {
                    //获取17 和 33逆变器的电流信息
                    if (!item.getMemo().equals(""))
                        ycList = decisionDao.getPVData(item.getDeviceId(), BJLX.huaweinibianqi.getValue(), item.getMemo(), 2, weatherDate);
                    else
                        ycList = decisionDao.getPVData(item.getDeviceId(), BJLX.huaweinibianqi.getValue(), item.getMemo(), 4, weatherDate);
                    //遍历获取的电流信息进行业务判断
                    for (YCInfoData yc : ycList) {
                        if (yc.getValue() == 0) //判断电流为0
                        {
                            YCInfoData ycdata = new YCInfoData();
                            boolean isFlag = true;
                            for (int j = 0; j < YCInfoDataList.size(); j++) {
                                if (YCInfoDataList.get(j).getBuJianCanShu() == yc.getBuJianCanShu() && YCInfoDataList.get(j).getBuJianLeiXing() == yc.getBuJianLeiXing() && YCInfoDataList.get(j).getBuJianId() == yc.getBuJianId()) {
                                    YCInfoDataList.get(j).setCalsStat(YCInfoDataList.get(j).getCalsStat() + 1);
                                    isFlag = false;
                                }
                            }
                            if (isFlag) {
                                ycdata.setBuJianCanShu(yc.getBuJianCanShu());
                                ycdata.setBuJianLeiXing(yc.getBuJianLeiXing());
                                ycdata.setBuJianId(yc.getBuJianId());
                                ycdata.setBujiancanshuname(yc.getBujiancanshuname());
                                ycdata.setValue(yc.getValue());
                                ycdata.setCalsStat(1);
                                YCInfoDataList.add(ycdata);
                            }
                        }
                    }
                    //小时增加1
                    Calendar calendarHour = new GregorianCalendar();
                    calendarHour.setTime(weatherDate);
                    calendarHour.add(calendarHour.HOUR_OF_DAY, 1);//把日期往后增加一天.整数往后推,负数往前移动
                    weatherDate = calendarHour.getTime(); //这个时间就是日期往后推一天的结果

                }
                //判断是否全部电流为0
                for (YCInfoData yc : YCInfoDataList) {
                    if (yc.getCalsStat() > 20) {
                        flag = "1"; //表示逆变器异常
                        msg += "逆变器电流异常" + item.getDeviceName() + "" + yc.getBujiancanshuname() + "" + yc.getValue() + "; ";
                    }
                }

            }

            //判断如果存在电流问题则返回
            if (flag.endsWith("1")) {
                result.setActrualValue(flag);
                result.setMessage(msg);
                return result;
            }

            //遍历设备 遮挡临时屏蔽
            for (DeviceInfo item : deviceList) {
                List<PVDispersion> PVDispersionList = new ArrayList<PVDispersion>();
                //遍历日期
                for (WeatherInfo weather : weatherList) {
                    //判断是否是有效天气
                    if (weather.getWeatherTypeFa() == WeatherType.qing.getValue() || weather.getWeatherTypeFb() == WeatherType.qing.getValue() || weather.getWeatherTypeFa() == WeatherType.duoyun.getValue() || weather.getWeatherTypeFb() == WeatherType.duoyun.getValue()) {
                        List<YCInfoData> ycList = new ArrayList<YCInfoData>();

                        PVDispersion PVDispersion = new PVDispersion();
                        //获取weather日期的所有整点数据0 1 2 3...23
                        for (int i = 0; i < 24; i++) {
                            //获取17 和 33逆变器的电流信息
                            if (item.getShuoMing().equals("17"))
                                ycList = decisionDao.getPVData(item.getDeviceId(), BJLX.huaweinibianqi.getValue(), item.getMemo(), 2, weather.getTime());
                            else
                                ycList = decisionDao.getPVData(item.getDeviceId(), BJLX.huaweinibianqi.getValue(), item.getMemo(), 4, weather.getTime());
                            //遍历获取的电流信息进行业务判断
                            List<Double> input = new ArrayList<Double>();
                            for (YCInfoData yc : ycList) {
                                if (yc.getValue() > 1)
                                    input.add(yc.getValue());//判断遮挡
                            }
                            if (input.size() > 0) {
                                double valueD = Collections.max(input) - Collections.min(input);
                                if (PVDispersion.getValue() == 0 || valueD > PVDispersion.getValue()) {
                                    PVDispersion.setValue(valueD);//获取当天日期的最大值
                                    PVDispersion.setDate(weather.getTime());//获取当天日期的最大值发生时间
                                    if (Collections.min(input) < 1)
                                        PVDispersion.setFlag(false);
                                    else
                                        PVDispersion.setFlag(true);
                                }
                            }

                            //小时增加1
                            Calendar calendarHour = new GregorianCalendar();
                            calendarHour.setTime(weather.getTime());
                            calendarHour.add(calendarHour.HOUR_OF_DAY, 1);//把日期往后增加一小时.整数往后推,负数往前移动
                            weather.setTime(calendarHour.getTime()); //这个时间就是日期往后推一天的结果
                        }
                        //判断最大差需要大于3
                        if (PVDispersion.getValue() > 6 && PVDispersion.isFlag())
                            PVDispersionList.add(PVDispersion);
                    }
                }
                //判断PVDispersionList中所有天气相差不超过两个小时
                List<Integer> inputDate = new ArrayList<Integer>();

                if (PVDispersionList.size() >= 2)  //至少两天以上的才参与计算
                {
                    String msgDetail = "";
                    for (PVDispersion it : PVDispersionList) {
                        inputDate.add(it.getDate().getHours());
                        msgDetail += sdf.format(it.getDate()) + "差值" + df.format(it.getValue()) + ";";
                    }
                    int hours = (Collections.max(inputDate) - Collections.min(inputDate));
                    if (hours <= 2) {
                        flag = "1"; //表示遮挡
                        msg += item.getDeviceName() + ":" + msgDetail;
                    }
                }
            }
            //遍历设备判断电压小于450V
            for (DeviceInfo item : deviceList) {
                if (weatherList.size() == 0)
                    break;
                Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
                today.setTimeInMillis(Calendar.getInstance().getTimeInMillis()); //获取前minuteParam数据
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                Date weatherDate = today.getTime();
                List<YCInfoData> ycList = new ArrayList<YCInfoData>();

                List<YCInfoData> YCInfoDataList = new ArrayList<YCInfoData>();
                //获取weather日期的所有整点数据0 1 2 3...23
                for (int i = 0; i < 24; i++) {
                    //获取17 和 33逆变器的电流信息
                    if (!item.getMemo().equals(""))
                        ycList = decisionDao.getPVData(item.getDeviceId(), BJLX.huaweinibianqi.getValue(), item.getMemo(), 1, weatherDate);
                    else
                        ycList = decisionDao.getPVData(item.getDeviceId(), BJLX.huaweinibianqi.getValue(), item.getMemo(), 3, weatherDate);
                    //遍历获取的电压信息进行业务判断
                    for (YCInfoData yc : ycList) {
                        if (yc.getValue() < 400) //判断电流为0
                        {
                            YCInfoData ycdata = new YCInfoData();
                            boolean isFlag = true;
                            for (int j = 0; j < YCInfoDataList.size(); j++) {
                                if (YCInfoDataList.get(j).getBuJianCanShu() == yc.getBuJianCanShu() && YCInfoDataList.get(j).getBuJianLeiXing() == yc.getBuJianLeiXing() && YCInfoDataList.get(j).getBuJianId() == yc.getBuJianId()) {
                                    YCInfoDataList.get(j).setCalsStat(YCInfoDataList.get(j).getCalsStat() + 1);
                                    isFlag = false;
                                }
                            }
                            if (isFlag) {
                                ycdata.setBuJianCanShu(yc.getBuJianCanShu());
                                ycdata.setBuJianLeiXing(yc.getBuJianLeiXing());
                                ycdata.setBuJianId(yc.getBuJianId());
                                ycdata.setBujiancanshuname(yc.getBujiancanshuname());
                                ycdata.setValue(yc.getValue());
                                ycdata.setCalsStat(1);
                                YCInfoDataList.add(ycdata);
                            }
                        }
                    }
                    //小时增加1
                    Calendar calendarHour = new GregorianCalendar();
                    calendarHour.setTime(weatherDate);
                    calendarHour.add(calendarHour.HOUR_OF_DAY, 1);//把日期往后增加一天.整数往后推,负数往前移动
                    weatherDate = calendarHour.getTime(); //这个时间就是日期往后推一天的结果

                }
                //判断是否全部电压低于450V
                for (YCInfoData yc : YCInfoDataList) {
                    //如果20个小时电流为0，就判断有错
                    if (yc.getCalsStat() > 20) {
                        flag = "1"; //表示逆变器异常
                        msg += "逆变器电压异常" + item.getDeviceName() + "" + yc.getBujiancanshuname() + "低于450; ";
                    }
                }
            }

        }
        result.setActrualValue(flag);
        if (flag.endsWith("1"))
            result.setMessage(msg);
        return result;
    }

    /***
     * 获取电站直流侧电压和电流异常信息
     * @param device
     * @param dateParam
     */
    public DecisionResult getPVDataByHour(Device device, Date dateParam) {
        DecisionResult result = new DecisionResult();
        String msg = "";
        String flag = "0";

        //获取电站下逆变器设备
        decisionDao = new DecisionDao();
        List<DeviceInfo> deviceList = decisionDao.GetDeviceInfo(device.getId());
        if (deviceList.size() > 0) {
            msg = deviceList.get(0).getProvinceName() + deviceList.get(0).getCityName() + deviceList.get(0).getStationName() + " : ";  //获取电站区域名称 乡镇名称 电站名称

            //遍历设备判断电流为0
            for (DeviceInfo item : deviceList) {
                List<YCInfoData> ycList = new ArrayList<YCInfoData>();

                List<YCInfoData> YCInfoDataList = new ArrayList<YCInfoData>();
                //获取weather日期的所有整点数据0 1 2 3...23
                for (int i = 0; i < 5; i++) {
                    Calendar date = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
                    date.setTimeInMillis(Calendar.getInstance().getTimeInMillis() - 300000 * (i + 1)); //获取前minuteParam数据
                    Date weatherDate = date.getTime();

                    //获取17 和 33逆变器的电流信息
                    if (!item.getMemo().equals(""))
                        ycList = decisionDao.getPVData(item.getDeviceId(), BJLX.huaweinibianqi.getValue(), item.getMemo(), 2, weatherDate);
                    else
                        ycList = decisionDao.getPVData(item.getDeviceId(), BJLX.huaweinibianqi.getValue(), item.getMemo(), 4, weatherDate);
                    //遍历获取的电流信息进行业务判断
                    for (YCInfoData yc : ycList) {
                        if (yc.getValue() == 0) //判断电流为0
                        {
                            YCInfoData ycdata = new YCInfoData();
                            boolean isFlag = true;
                            for (int j = 0; j < YCInfoDataList.size(); j++) {
                                if (YCInfoDataList.get(j).getBuJianCanShu() == yc.getBuJianCanShu() && YCInfoDataList.get(j).getBuJianLeiXing() == yc.getBuJianLeiXing() && YCInfoDataList.get(j).getBuJianId() == yc.getBuJianId()) {
                                    YCInfoDataList.get(j).setCalsStat(YCInfoDataList.get(j).getCalsStat() + 1);
                                    isFlag = false;
                                }
                            }
                            if (isFlag) {
                                ycdata.setBuJianCanShu(yc.getBuJianCanShu());
                                ycdata.setBuJianLeiXing(yc.getBuJianLeiXing());
                                ycdata.setBuJianId(yc.getBuJianId());
                                ycdata.setBujiancanshuname(yc.getBujiancanshuname());
                                ycdata.setValue(yc.getValue());
                                ycdata.setCalsStat(1);
                                YCInfoDataList.add(ycdata);
                            }
                        }
                    }
                }
                //判断是否全部电流为0
                for (YCInfoData yc : YCInfoDataList) {
                    if (yc.getCalsStat() > 5) {
                        flag = "1"; //表示逆变器异常
                        msg += "逆变器电流异常" + item.getDeviceName() + "" + yc.getBujiancanshuname() + "" + yc.getValue() + "; ";
                    }
                }
            }
            //遍历设备判断电压小于450V
            for (DeviceInfo item : deviceList) {
                List<YCInfoData> ycList = new ArrayList<YCInfoData>();

                List<YCInfoData> YCInfoDataList = new ArrayList<YCInfoData>();
                //获取weather日期的所有整点数据0 1 2 3...23
                for (int i = 0; i < 5; i++) {
                    Calendar date = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
                    date.setTimeInMillis(Calendar.getInstance().getTimeInMillis() - 300000 * (i + 1)); //获取前minuteParam数据
                    Date weatherDate = date.getTime();
                    //获取17 和 33逆变器的电流信息
                    if (!item.getMemo().equals(""))
                        ycList = decisionDao.getPVData(item.getDeviceId(), BJLX.huaweinibianqi.getValue(), item.getMemo(), 1, weatherDate);
                    else
                        ycList = decisionDao.getPVData(item.getDeviceId(), BJLX.huaweinibianqi.getValue(), item.getMemo(), 3, weatherDate);
                    //遍历获取的电压信息进行业务判断
                    for (YCInfoData yc : ycList) {
                        if (yc.getValue() < 450) //判断电流为0
                        {
                            YCInfoData ycdata = new YCInfoData();
                            boolean isFlag = true;
                            for (int j = 0; j < YCInfoDataList.size(); j++) {
                                if (YCInfoDataList.get(j).getBuJianCanShu() == yc.getBuJianCanShu() && YCInfoDataList.get(j).getBuJianLeiXing() == yc.getBuJianLeiXing() && YCInfoDataList.get(j).getBuJianId() == yc.getBuJianId()) {
                                    YCInfoDataList.get(j).setCalsStat(YCInfoDataList.get(j).getCalsStat() + 1);
                                    isFlag = false;
                                }
                            }
                            if (isFlag) {
                                ycdata.setBuJianCanShu(yc.getBuJianCanShu());
                                ycdata.setBuJianLeiXing(yc.getBuJianLeiXing());
                                ycdata.setBuJianId(yc.getBuJianId());
                                ycdata.setBujiancanshuname(yc.getBujiancanshuname());
                                ycdata.setValue(yc.getValue());
                                ycdata.setCalsStat(1);
                                YCInfoDataList.add(ycdata);
                            }
                        }
                    }
                }
                //判断是否全部电压低于450V
                for (YCInfoData yc : YCInfoDataList) {
                    if (yc.getCalsStat() > 5) {
                        flag = "1"; //表示逆变器异常
                        msg += "逆变器电压异常" + item.getDeviceName() + "" + yc.getBujiancanshuname() + "" + yc.getValue() + "; ";
                    }
                }
            }

        }
        result.setActrualValue(flag);
        if (flag.endsWith("1"))
            result.setMessage(msg);
        return result;
    }

    public boolean isNumeric(String str) {

        if (str == "")
            return false;
        Pattern pattern = Pattern.compile("[0-9]*");
        if (pattern.matcher(str).matches()) {
            return true;
        } else {
            return false;
        }
    }

    /***
     * 获取电站直流侧电压和电流异常信息
     * @param device
     * @param dateParam
     */
    public DecisionResult getPVCurrentData(Device device, Date dateParam) {
        DecisionResult result = new DecisionResult();
        String msg = "";
        String flag = "0";
        boolean niflag = false;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        int hour = Integer.parseInt(sdf.format(date));

        //获取电站下逆变器设备
        List<DeviceInfo> deviceList = decisionDao.getDeviceInfo(device.getId(), 5, BJLX.yuanshennibianqi.getValue());

        if (deviceList.size() > 0) {
            msg = device.getName() + ":";  //组message为电站名称：

            //遍历设备判断电流为0
            for (DeviceInfo item : deviceList) {
                //说明配置的是哪几个参数需要判断，判断组串为0
                if (item.getShuoMing() != null && item.getShuoMing().contains(",")) {
                    String param[] = item.getShuoMing().split(",");
                    for (int i = 0; i < param.length; i++) {
                        if (isNumeric(param[i])) {
                            //取出获取设备（电站下所有电表的）的小时电量
                            List<HisDataTimeAndValue> hourList = hisDataDao.getHoursYcData(6, item.getDeviceId(), BJLX.yuanshennibianqi.getValue(), Integer.valueOf(param[i]), dateParam, dateParam);
                            if (hourList != null && hourList.size() > 0) {
                                if (hourList.get(hour - 2).getValue() == 0f && hourList.get(hour - 2).getValue() == 0f && hourList.get(hour).getValue() == 0f) {
                                    niflag = true;
                                    msg += item.getDeviceName()+"组串" + param[i] + "电流为0,";
                                }
                            }
                        }
                    }
                }
            }
        }

        if (niflag) {
            flag = "1";
            msg += "请派运维人员前往查看上述组串信息;";
        }
        result.setActrualValue(flag);
        if (flag.endsWith("1"))
            result.setMessage(msg);
        return result;
    }
    /***
     * 获取电站直流侧电压和电流异常信息
     * @param device
     * @param dateParam
     */
    public DecisionResult getDbCvData(Device device, Date dateParam) {
        DecisionResult result = new DecisionResult();
        String msg = "";
        String flag = "0";
        boolean niflag = false;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        int hour = Integer.parseInt(sdf.format(date));


        //获取电站下电表设备
        //改为获取电站下逆变器设备
        List<DeviceInfo> deviceList = decisionDao.getDeviceInfo(device.getId(), 5, BJLX.yuanshendianbiao.getValue());

        double[] dayPower = new double[deviceList.size()];
        int num = 0;

        if (deviceList.size() > 0) {
            msg = device.getName() + ":日电量分别为：";  //组message为电站名称：

            //遍历设备判断电流为0
            for (DeviceInfo item : deviceList) {
                //取出获取设备（电站下所有电表的）的小时电量
                if(item.getShuoMing()!=null&&item.getShuoMing()!=""){
                    List<HisDataTimeAndValue> hourList = hisDataDao.getRegionHoursData(6, item.getDeviceId(), BJLX.yuanshendianbiao.getValue(), 11, dateParam, dateParam);
                    dayPower[num++] = (hourList.get(hour).getValue() - hourList.get(1).getValue())/Double.valueOf(item.getShuoMing());
                    msg+=""+(hourList.get(hour).getValue() - hourList.get(1).getValue())+",";
                }
            }
        }
        //计算各电表日电量平均差
        if(num>=deviceList.size()){
            //给你5个数，能得到他们的标准差
            double stanDiv = getStandardDiviation(dayPower);
            if(stanDiv > 0.3){
                niflag = true;
                msg+="单kW发电离散率(标准差)较大："+stanDiv;
            }
        }
        if (niflag) {
            flag = "1";
        }
        result.setActrualValue(flag);
        if (flag.endsWith("1"))
            result.setMessage(msg);
        return result;
    }

    /**
     * 标准差σ=sqrt(s^2)
     * @param x
     * @return
     */
    public static double getStandardDiviation(double[] x) {
        int m = x.length;
        double sum = 0;
        for (int i = 0; i < m; i++) {// 求和
            sum += x[i];
        }
        double dAve = sum / m;// 求平均值
        double dVar = 0;
        for (int i = 0; i < m; i++) {// 求方差
            dVar += (x[i] - dAve) * (x[i] - dAve);
        }
        return Math.sqrt(dVar / m);
    }

    /***
     * 获取电站分组信息
     * @param device
     * @param dateParam
     */
    public DecisionResult  getGroupData(Device device,Date dateParam)
    {

        DecisionResult result = new DecisionResult();
        String msg = "";
        String flag = "0";
        //获取近七日天气信息
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(calendar.DATE,-7);//把日期往后增加一天.整数往后推,负数往前移动
//		dateParam=calendar.getTime(); //这个时间就是日期往后推一天的结果
//		String dateString = sdf.format(dateParam);
//		System.out.println(dateString);
//		List<WeatherInfo> weatherList = diagnosisDao.GetWeatherDateList(dateString);
        //遍历日期
        DecimalFormat   df   = new DecimalFormat("######0.00");
        double value = 0;
        int valueNum = 0;
//		for(WeatherInfo weather :weatherList)
//		{
//			//判断是否是有效天气
//			if(weather.getWeatherTypeFa()==WeatherType.qing.getValue()||weather.getWeatherTypeFb()==WeatherType.qing.getValue()
//					||weather.getWeatherTypeFa()==WeatherType.duoyun.getValue()||weather.getWeatherTypeFb() == WeatherType.duoyun.getValue())
//			{
//				Calendar calendarHour = new GregorianCalendar();
//				calendarHour.setTime(weather.getTime());
//				calendarHour.add(calendarHour.HOUR_OF_DAY,22);//把日期往后增加一天.整数往后推,负数往前移动
//				weather.setTime(calendarHour.getTime()); //这个时间就是日期往后推一天的结果
//				//获取电站所在分组以及分组下电站情况
//				List<PmStationGroup>  groupList = diagnosisDao.getGroupStationList(device.getId());
//				for(PmStationGroup group :groupList)
//				{
//					//根据日期获取分组下电站的发电量
//					List<PowerDayStat> stationList = decisionDao.getGroupStationList(String.valueOf(device.getId()), weather.getTime(),1);
//					HisDataTimeAndValue avgStation =decisionDao.getGroupAVG(group.getStation_Ids(), weather.getTime());
//
//					if(stationList!=null&&stationList.size()>0&&stationList.get(0).getMemo()!=null&&stationList.get(0).getMemo().equals(""))
//					{
//						msg += sdf.format(weather.getTime())+stationList.get(0).getStationName()+"日发电量为："+df.format(stationList.get(0).getDayPower())+";组发电量为："+df.format(avgStation.getValue()*stationList.get(0).getCapacity());
//						valueNum++;
//						//组内发电量数据核算，当天发电量减去组内均值电量的0.9倍
//						value += stationList.get(0).getDayPower()-(0.9*avgStation.getValue()*stationList.get(0).getCapacity());
//					}
//				}
//			}
//		}


        for(int j = 0;j<7;j++)
        {
            Calendar calendarHour = new GregorianCalendar();
            calendarHour.setTime(calendar.getTime());
            calendarHour.set(calendarHour.HOUR_OF_DAY,22);//把日期往后增加一天.整数往后推,负数往前移动
            calendar.setTime(calendarHour.getTime()); //这个时间就是日期往后推一天的结果
            dateParam=calendar.getTime(); //这个时间就是日期往后推一天的结果
            String dateString = sdf.format(dateParam);
            //获取对比当天的电站状态
            List<PmWarnRecord> record = decisionDao.getStationWarnStatus(device.getId(), dateParam);//修改后方法
            //对比当天电站不存在告警时进行分组电量对比
            if(record.size()==0)
            {
                //获取电站所在分组以及分组下电站情况
                List<PmStationGroup>  groupList = diagnosisDao.getGroupStationList(device.getId());
                for(PmStationGroup group :groupList)
                {
                    //根据日期获取分组下电站的发电量
                    List<PowerDayStat> stationList = decisionDao.getGroupStationList(String.valueOf(device.getId()), calendar.getTime(),1);
                    HisDataTimeAndValue avgStation =decisionDao.getGroupAVG(group.getStation_Ids(), calendar.getTime());

                    if(stationList!=null&&stationList.size()>0)
                    {
                        //组内发电量数据核算，当天发电量减去组内均值电量的0.8倍
                        value = stationList.get(0).getDayPower()-(0.85*avgStation.getValue()*stationList.get(0).getCapacity());
                        //value<0 表示日发电量低于组发电量0.8倍
                        if(value < 0){
                            msg += sdf.format(calendar.getTime())+stationList.get(0).getStationName()+"日发电量为："+df.format(stationList.get(0).getDayPower())+";组发电量为："+df.format(avgStation.getValue()*stationList.get(0).getCapacity())+";日发电量差值低于平均值"+df.format(avgStation.getValue()*stationList.get(0).getCapacity())+"的15%；";
                            valueNum++;
                        }
                    }
                }
            }else{
                System.err.println("存在告警，不进行电站分组对比"+String.valueOf(device.getId()));
            }

            calendar.add(calendar.DATE,1);//把日期往后增加一天.整数往后推,负数往前移动
        }
        //valueNum>=2 代表 了有效计算次数超过2（包含2）才
        if(valueNum>=3)
            flag = "1";

        //判断是否需要清洗 现获取该电站上次清洗时间如果超过2个月则设置flag为2 msg返回为需要清洗
        if(flag.endsWith("1"))
        {

        }

        result.setActrualValue(flag);
        if(flag.endsWith("1"))
            result.setMessage(msg);
        return result;
    }


    /***
     * 获取遮挡  如 杂草
     * @param device
     * @param dateParam
     */
    public DecisionResult  getStationShelter(Device device,Date dateParam)
    {

        DecisionResult result = new DecisionResult();
        String msg = "";
//		String stationWeedFlag = "0";
//		Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
//		today.setTimeInMillis(Calendar.getInstance().getTimeInMillis()); //获取当前时间
//		if(dateParam != null){
//			today.setTimeInMillis(dateParam.getTime());
//		}
//
//		//获取电站的离散率
//		hisDataDao = new HisDataDao();
//		List<HisDataTimeAndValue> dispersionList = hisDataDao.getHisDataTimeAndValue(RegionType.village.getValue(),device.getId(),ChangZhanParam.dispersionRatio.getValue(),dateParam);
//		if(dispersionList != null && dispersionList.size() >0 &&dispersionList.get(0).getValue()>0.051 )// 判断如果大于阈值则进入判断
//		{
//			//获取weather日期的所有整点数据0 1 2 3...23
//			float dispersionWeek=0,dispersionLastWeek=0;
//			Date date = today.getTime();
//			for(int daysForDisper=0;daysForDisper<14;daysForDisper++)
//			{
//				List<HisDataTimeAndValue> dispersionHis = hisDataDao.getHisDataTimeAndValue(RegionType.village.getValue(),device.getId(),ChangZhanParam.dispersionRatio.getValue(),date);
//				if(dispersionList != null && dispersionList.size() >0 &&dispersionList.get(0)!=null && daysForDisper<7)
//					dispersionWeek += dispersionHis.get(0).getValue();  //获取本周离散率
//				else
//					dispersionLastWeek += dispersionHis.get(0).getValue(); //获取上周离散率
//				//天数减一
//				Calendar calendarDay = new GregorianCalendar();
//				calendarDay.setTime(date);
//				calendarDay.add(calendarDay.DAY_OF_MONTH,-1);//把日期往后增加一天.整数往后推,负数往前移动
//				date=calendarDay.getTime(); //这个时间就是日期往后推一天的结果
//			}
//			//判断如果本周离散率大于上周离散率则进入
//			date = today.getTime();
//			if(dispersionWeek > dispersionLastWeek)
//			{
//				//将过去15天电站电量与标杆电量的比值按日期分成5组
//				double[] arr=new double[5];
//				double powerValue =0,benchmarkValue=0;
//				for(int daysForEnergy=0;daysForEnergy<15;daysForEnergy++)
//				{
//					List<HisDataTimeAndValue> dispersionHis = hisDataDao.getHisDataTimeAndValue(BJLX.changzhan.getValue(),device.getId(),ChangZhanParam.benchmarkElectricity.getValue(),date); //获取电站组标杆电量
//					String dayValue = hisDataDao.getRegionPowerData(RegionType.village.getValue(),device.getId(),BJLX.changzhan.getValue(),ChangZhanParam.daypowers.getValue(),InfoType.diandu.getValue(),0,date);//获取电站日发电量
//					if(dispersionHis != null && dispersionHis.size() >0 &&dispersionHis.get(0)!=null)
//					{
//						powerValue = Double.parseDouble(dayValue);
//						benchmarkValue = dispersionHis.get(0).getValue();
//						if (benchmarkValue==0){
//							arr[daysForEnergy/3] +=1;
//						}else {
//							arr[daysForEnergy/3] +=powerValue / benchmarkValue;  //获取电站日发电量和组标杆电量比值
//						}
//					}
//					//天数减一
//					Calendar calendarHour = new GregorianCalendar();
//					calendarHour.setTime(date);
//					calendarHour.add(calendarHour.DAY_OF_MONTH,-1);//把日期往后增加一天.正数往后推,负数往前移动
//					date=calendarHour.getTime(); //这个时间就是日期往后推一天的结果
//				}
//
//				//比较arr数组中相邻两个值的大小，如果数组下标小的（日期近的)电量实标比更小，则统计一次
//				int comparisonCounts = 0;
//				for(int daysGroupNum=0;daysGroupNum<4;daysGroupNum++)
//				{
//					if(arr[daysGroupNum]<arr[daysGroupNum+1])
//						comparisonCounts += 1;
//				}
//
//
//				if(comparisonCounts>=3 || ( arr[0] < arr[1] && arr[1]<arr[2]))
//				{
//					deviceDao = new DeviceDao();
//					List<StationInfo> stationList = deviceDao.getStationInfolist(RegionType.village.getValue(), device.getId());
//					stationWeedFlag = "1"; //表示杂草遮挡
//					msg += stationList.get(0).getCountyName()+stationList.get(0).getTownName()+stationList.get(0).getStationName()+"疑似杂草遮挡，请现场处理";
//				}
//			}
//		}
//
//		//判断电站是否开始恢复
//		if(stationWeedFlag.endsWith("1")&&getStationShelterRecovery(device,dateParam))
//			stationWeedFlag = "0"; //表示杂草遮挡恢复
//
//		result.setActrualValue(stationWeedFlag);
//		if(stationWeedFlag.endsWith("1"))
//			result.setMessage(msg);
        result.setActrualValue("0");
        result.setMessage(msg);
        return result;
    }
    /***
     * 获取遮挡恢复 true 代表恢复 false 代表未恢复
     * @param device
     * @param dateParam
     */
    public boolean  getStationShelterRecovery(Device device,Date dateParam)
    {
        DecisionResult result = new DecisionResult();
        String msg = "";
        String stationWeedFlag = "0";
        Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
        today.setTimeInMillis(Calendar.getInstance().getTimeInMillis()); //获取当前时间
        if(dateParam != null){
            today.setTimeInMillis(dateParam.getTime());
        }

        //获取电站的离散率
        List<HisDataTimeAndValue> dispersionList = hisDataDao.getHisDataTimeAndValue(BJLX.changzhan.getValue(),device.getId(), ChangZhanParam.dispersionRatio.getValue(),dateParam);
        if(dispersionList != null && dispersionList.size() >0  )// 判断数据有效
        {
            //获取今日离散率
            float dispToday;
            Date date = today.getTime();
            List<HisDataTimeAndValue> dispersionHis = hisDataDao.getHisDataTimeAndValue(BJLX.changzhan.getValue(),device.getId(),ChangZhanParam.dispersionRatio.getValue(),date);
            dispToday=dispersionHis.get(0).getValue();//获取今日离散率
            //获取5日离散率最小值
            float dispFive=0,dispFiveMin=10;
            for(int i=0;i<4;i++)
            {

                //天数减一
                Calendar calendarDay = new GregorianCalendar();
                calendarDay.setTime(date);
                calendarDay.add(calendarDay.DAY_OF_MONTH,-1);//把日期往后增加一天.整数往后推,负数往前移动
                date=calendarDay.getTime(); //这个时间就是日期往后推一天的结果
                if(dispersionList != null && dispersionList.size() >0 &&dispersionList.get(0)!=null)
                {
                    List<HisDataTimeAndValue> dispersionHisR = hisDataDao.getHisDataTimeAndValue(BJLX.changzhan.getValue(),device.getId(),ChangZhanParam.dispersionRatio.getValue(),date);
                    dispFive=dispersionHisR.get(0).getValue();//获取5日离散率
                }
                if(dispFive<dispFiveMin)
                    dispFiveMin=dispFive;

            }
            //若今日离散率小于过去5天离散率，则继续判断。
            if (dispToday<dispFiveMin){
                Date dateR = today.getTime();
                double[] ratio=new double[40];
                double powerValue =0,benchmarkValue=0;
                for(int i=0;i<40;i++)
                {
                    List<HisDataTimeAndValue> benchmarkHis = hisDataDao.getHisDataTimeAndValue(BJLX.changzhan.getValue(),device.getId(),ChangZhanParam.benchmarkElectricity.getValue(),dateR); //获取电站组标杆电量
                    String dayValue = hisDataDao.getRegionPowerData(RegionType.village.getValue(),device.getId(),BJLX.changzhan.getValue(),ChangZhanParam.daypowers.getValue(), InfoType.diandu.getValue(),0,dateR);//获取电站日发电量
                    if(benchmarkHis != null && benchmarkHis.size() >0 &&benchmarkHis.get(0)!=null)
                    {
                        powerValue = Double.parseDouble(dayValue);
                        benchmarkValue = benchmarkHis.get(0).getValue();
                        if(benchmarkValue==0) {
                            ratio[i]=1;
                        }else {
                            ratio[i]=powerValue/benchmarkValue;     //计算过去40天的“实际电量/标杆电量”并存入ratio数组。
                        }

                    }
                    //天数减一
                    Calendar calendarHour = new GregorianCalendar();
                    calendarHour.setTime(dateR);
                    calendarHour.add(calendarHour.DAY_OF_MONTH,-1);//把日期往后增加一天.整数往后推,负数往前移动
                    dateR=calendarHour.getTime(); //这个时间就是日期往后推一天的结果
                }
                //下面记录今日实标比rToday，用于后续比较：
                double rToday=ratio[0];

                //下面计算近10天实标比平均值rmal：
                double rmal=0;
                for(int i=0;i<10;i++) {
                    rmal=rmal+ratio[i]/10;
                }

                //下面使用冒泡法对数组元素进行由大到小排序：
                double temp; // 记录临时中间值
                int size = ratio.length; // 数组大小
                for (int i = 0; i < size - 1; i++) {
                    for (int j = i + 1; j < size; j++) {
                        if (ratio[i] < ratio[j]) { // 交换两数的位置
                            temp = ratio[i];
                            ratio[i] = ratio[j];
                            ratio[j] = temp;
                        }
                    }
                }
                //下面计算过去40天排名前10的实标比平均值rnom：
                double rnom=0;
                for(int i=0;i<10;i++) {
                    rnom=rnom+ratio[i]/10;
                }
                //下面计算阈值r0:
                double r0=0.3*rnom+0.7*rmal;
                if(r0<1.03*rmal) {
                    r0=1.03*rmal;
                }
                if(rToday>r0)
                    return true;
            }

        }
        return false;
    }

    /***
     * 获取源深电站状态信息
     * @param device
     * @param dateParam
     */
    public DecisionResult getYsStationStatus(Device device, Date dateParam) {
        DecisionResult result = new DecisionResult();
        result.setActrualValue("0");//设置0表示电站正常
        //List<PmWarnRecord> record = decisionDao.getStationStatus(device.getId(), dateParam);//原方法
        //获取告警记录
        List<PmWarnRecord> record = decisionDao.getStationWarnStatus(device.getId(), dateParam);//修改后方法
        if (record != null && record.size() > 0) {
            result.setActrualValue("1");  //如果记录不为空表示返回电站异常
            String msg = record.get(0).getProvinceName() + record.get(0).getCityName() + record.get(0).getStation_Name() + " : ";  //获取电站区域名称 乡镇名称 电站名称
            for (PmWarnRecord item : record) {
                msg += item.getEquipment_Name() + "_" + item.getWarn_Name() + "; ";//获取电站告警信息
            }
            result.setMessage(msg);
        }
        return result;
    }


    /**
     * 获取源深电站 逆变器异常信息
     *
     * @param device
     * @param dateParam
     * @return
     */
    public DecisionResult getYsStationDeviceStatus(Device device, Date dateParam) {
//        GregorianCalendar gc = new GregorianCalendar();
//        gc.set(Calendar.YEAR,2020);//设置年
//        gc.set(Calendar.MONTH, 11);//这里0是1月..以此向后推
//        gc.set(Calendar.DAY_OF_MONTH, 10);//设置天
//        dateParam = gc.getTime();
        DecisionResult result = new DecisionResult();
        String flag = "0";
        String msg = "";  //获取电站区域名称 乡镇名称 电站名称
        //上海伏凌部件类型为26
        List<DeviceInfo> deviceList = decisionDao.getStationDeviceInfo(device.getId(), BJLX.huaweinibianqi.getValue());
        if (deviceList != null && deviceList.size() > 0) {
            msg += deviceList.get(0).getProvinceName() + deviceList.get(0).getCityName() + deviceList.get(0).getStationName() + " : ";
            for (DeviceInfo deviceInfo : deviceList) {

                boolean niflag = true;
                //获取逆变器的小时电量
                List<HisDataTimeAndValue> hourList = hisDataDao.getRegionCumulativeHours(6, deviceInfo.getDeviceId(), BJLX.huaweinibianqi.getValue(), 99, dateParam, dateParam);
                if (hourList != null && hourList.size() > 0) {
                    float value = hourList.get(0).getValue();
                    for (HisDataTimeAndValue item : hourList) {
                        if (item.getValue() > 0 && item.getValue() != value) {
                            niflag = false;
                            break;
                        }
                    }
                    if (niflag) {
                        flag = "1";
                        msg += deviceInfo.getDeviceName() + "通讯异常; ";
                    }

                }
            }
        }
        result.setActrualValue(flag);
        if (flag.endsWith("1")){
            result.setMessage(msg);
        }

        return result;
    }

    /***
     * 获取源深电站直流侧电流异常信息
     * @param device
     * @param dateParam
     */
    public DecisionResult getYsPVData(Device device, Date dateParam) {
        DecisionResult result = new DecisionResult();
        String msg = "";
        String flag = "0";
        //获取近七日天气信息
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat df = new DecimalFormat("######0.00");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(calendar.DATE, -7);//把日期往后增加一天.整数往后推,负数往前移动
        dateParam = calendar.getTime(); //这个时间就是日期往后推一天的结果
        String dateString = sdf.format(dateParam);
//        //		System.out.println(dateString);
//        //获取天气情况
//        List<WeatherInfo> weatherList = diagnosisDao.GetWeatherDateList(dateString);
        //获取电站下逆变器设备
        //getDeviceInfo（）的参数和逆变器类型的参数一样，26
        List<DeviceInfo> deviceList = decisionDao.getStationDeviceInfo(device.getId(), BJLX.huaweinibianqi.getValue());
//        List<DeviceInfo> deviceList = decisionDao.GetDeviceInfo(device.getId());
        if (deviceList.size() > 0) {
            msg = deviceList.get(0).getProvinceName() + deviceList.get(0).getCityName() + deviceList.get(0).getStationName() + " : ";  //获取电站区域名称 乡镇名称 电站名称

            //遍历设备判断电流为0
            for (DeviceInfo item : deviceList) {
//                if (weatherList.size() == 0)
//                    break;

                Calendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
                today.setTimeInMillis(Calendar.getInstance().getTimeInMillis()); //获取前minuteParam数据
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                Date weatherDate = today.getTime();
                List<YCInfoData> ycList = new ArrayList<YCInfoData>();

                List<YCInfoData> YCInfoDataList = new ArrayList<YCInfoData>();
                //获取weather日期的所有整点数据0 1 2 3...23
                for (int i = 0; i < 24; i++) {
                    //获取17 和 33逆变器的电流信息
                    if (item.getMemo()!=null)
                        ycList = decisionDao.getPVData(item.getDeviceId(), BJLX.huaweinibianqi.getValue(), item.getMemo(), 2, weatherDate);
                    else
                        ycList = decisionDao.getPVData(item.getDeviceId(), BJLX.huaweinibianqi.getValue(), item.getMemo(), 4, weatherDate);
                    //遍历获取的电流信息进行业务判断
                    for (YCInfoData yc : ycList) {
                        if (yc.getValue() == 0) //判断电流为0
                        {
                            YCInfoData ycdata = new YCInfoData();
                            boolean isFlag = true;
                            for (int j = 0; j < YCInfoDataList.size(); j++) {
                                if (YCInfoDataList.get(j).getBuJianCanShu() == yc.getBuJianCanShu() && YCInfoDataList.get(j).getBuJianLeiXing() == yc.getBuJianLeiXing() && YCInfoDataList.get(j).getBuJianId() == yc.getBuJianId()) {
                                    YCInfoDataList.get(j).setCalsStat(YCInfoDataList.get(j).getCalsStat() + 1);
                                    isFlag = false;
                                }
                            }
                            if (isFlag) {
                                ycdata.setBuJianCanShu(yc.getBuJianCanShu());
                                ycdata.setBuJianLeiXing(yc.getBuJianLeiXing());
                                ycdata.setBuJianId(yc.getBuJianId());
                                ycdata.setBujiancanshuname(yc.getBujiancanshuname());
                                ycdata.setValue(yc.getValue());
                                ycdata.setCalsStat(1);
                                YCInfoDataList.add(ycdata);
                            }
                        }
                    }
                    //小时增加1
                    Calendar calendarHour = new GregorianCalendar();
                    calendarHour.setTime(weatherDate);
                    calendarHour.add(calendarHour.HOUR_OF_DAY, 1);//把日期往后增加一天.整数往后推,负数往前移动
                    weatherDate = calendarHour.getTime(); //这个时间就是日期往后推一天的结果

                }
                //判断是否全部电流为0
                for (YCInfoData yc : YCInfoDataList) {
                    if (yc.getCalsStat() > 20) {
                        flag = "1"; //表示逆变器异常
                        msg += "逆变器电流异常" + item.getDeviceName() + "" + yc.getBujiancanshuname() + "" + yc.getValue() + "; ";
                    }
                }

            }

            //判断如果存在电流问题则返回
            if (flag.endsWith("1")) {
                result.setActrualValue(flag);
                result.setMessage(msg);
                return result;
            }

        }
        result.setActrualValue(flag);
        if (flag.endsWith("1"))
            result.setMessage(msg);
        return result;
    }

    /***
     * 获取源深电站内部逆变器的离散率
     * @param device
     * @param dateParam
     */
    public DecisionResult getYsDbCvData(Device device, Date dateParam) {
        DecisionResult result = new DecisionResult();
        String msg = "";
        String flag = "0";
        boolean niflag = false;

        GregorianCalendar gc = new GregorianCalendar();
        gc.set(Calendar.YEAR,2020);//设置年
        gc.set(Calendar.MONTH, 11);//这里0是1月..以此向后推
        gc.set(Calendar.DAY_OF_MONTH, 4);//设置天
        dateParam = gc.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        int hour = Integer.parseInt(sdf.format(dateParam));


        //获取电站下电表设备
        //改为获取电站下逆变器设备
        List<DeviceInfo> deviceList = decisionDao.getDeviceInfo(device.getId(), 5, BJLX.huaweinibianqi.getValue());

        double[] dayPower = new double[deviceList.size()];
        int num = 0;

        if (deviceList.size() > 0) {
            msg = device.getName() + ":日电量分别为：";  //组message为电站名称：

            //遍历设备判断电流为0
            for (DeviceInfo item : deviceList) {
                //取出获取设备（电站下所有电表的）的小时电量
                if(item.getShuoMing()!=null&&item.getShuoMing()!=""){
                    List<HisDataTimeAndValue> hourList = hisDataDao.getRegionHoursData(6, item.getDeviceId(), BJLX.huaweinibianqi.getValue(), 101, dateParam, dateParam);
                    dayPower[num++] = (hourList.get(hour).getValue() - hourList.get(1).getValue())/Double.valueOf(item.getShuoMing());
                    msg+=""+(hourList.get(hour).getValue() - hourList.get(1).getValue())+",";
                }
            }
        }
        //计算各电表日电量平均差
        if(num>=deviceList.size()){
            //给你5个数，能得到他们的标准差
            double stanDiv = getStandardDiviation(dayPower);
            if(stanDiv > 0.3){
                niflag = true;
                msg+="单kW发电离散率(标准差)较大："+stanDiv;
            }
        }
        if (niflag) {
            flag = "1";
        }
        result.setActrualValue(flag);
        if (flag.endsWith("1"))
            result.setMessage("逆变器离散率"+msg);
        return result;
    }

}
