package cn.zup.iot.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/***
 * kafka 传输数据实体结构
 */
public class DataEvent {

    private int componentType;//部件类型
    private int componentId;//部件
    private int componentParamId;//部件参数
    private int stationId;//场站
    private long eventTime;//事件时间
    private String dataValue;//值

}
