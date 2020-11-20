package cn.zup.iot.timerdecision.model;

import java.util.Date;

public class cal_param {
    @lombok.Data
    public static class calc_data {
         Integer calcId;
         Integer targetTable;//存库（1yaoce  2kwh   3byte   4energy）
         Integer deviceType;//部件类型ID
         Integer deviceID;//部件ID
         Integer deviceParam;//部件参数ID
         Integer changZhanID;//厂站ID
         long value;//结果值
         Date date;//计算出来的时间

    }

}
