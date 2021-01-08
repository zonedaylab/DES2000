package cn.zup.iot.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 采集到的kafka中的消息
 * @author shishanli
 * @2021年1月3日19:41:12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class DataEvent {
    //部件类型
    private int componentType;
    //部件
    private int componentId;
    //部件参数
    private int componentParamId;
    //场站
    private int stationId;
    //事件时间
    private long eventTime;
    //值
    private String dataValue;

    public int getComponentType() {
        return componentType;
    }

    public void setComponentType(int componentType) {
        this.componentType = componentType;
    }

    public int getComponentId() {
        return componentId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    public int getComponentParamId() {
        return componentParamId;
    }

    public void setComponentParamId(int componentParamId) {
        this.componentParamId = componentParamId;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }
}
