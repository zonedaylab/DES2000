package cn.zup.iot.timerdbsave;

public class BatchUpdaeParm {
    private int hour;
    private String dataTime;
    private int componentType;
    private int componentId;
    private int componentParamId;
    private int stationId;
    private Double dataValue;

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public String getDataTime() {
        return dataTime;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }

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

    public Double getDataValue() {
        return dataValue;
    }

    public void setDataValue(Double dataValue) {
        this.dataValue = dataValue;
    }

    public BatchUpdaeParm(int hour, String dataTime, int componentType, int componentId, int componentParamId, int stationId, Double dataValue) {
        this.hour = hour;
        this.dataTime = dataTime;
        this.componentType = componentType;
        this.componentId = componentId;
        this.componentParamId = componentParamId;
        this.stationId = stationId;
        this.dataValue = dataValue;
    }
}
