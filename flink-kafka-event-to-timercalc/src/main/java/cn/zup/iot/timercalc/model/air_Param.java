package cn.zup.iot.timercalc.model;

@lombok.Data
public class air_Param {
    private String i_data_label;//数据标签
    private String i_data_scada;//数据对应的scada三段式
    private double fCoef;//请求连接url
    private double maxPositiveValue;
    private double minPositiveValue;
    private double maxNegativeValue;
    private double minNegativeValue;
    private String mingzi;
    private int i_data_type;
}
