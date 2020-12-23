package cn.zup.iot.timercalc.model;

import java.util.List;
import java.util.Map;

@lombok.Data
public class jk_Conn {

    private Integer conn_id;//webservice接口配置id
    private String conn_name;//post请求连接名称
    private String req_url;//请求连接url
    private String req_param;//请求参数
    private int bjtype;//部件类型ID
    private String i_device_label;
    private String result_name;//
    private String req_result;//
    private Map<String,String> deviceMap;
    private List<conn_param>  paramList;


}
