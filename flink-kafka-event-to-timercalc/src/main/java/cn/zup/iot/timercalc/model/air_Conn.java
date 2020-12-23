package cn.zup.iot.timercalc.model;

@lombok.Data
public class air_Conn {

    private Integer conn_id;//webservice接口配置id
    private String conn_name;//post请求连接名称
    private String req_url;//请求连接url
    private String req_param;//请求参数
    private String result_name;//返回值对应的结果标签
    private int startType;//启动标志
}
