package cn.zup.iot.timerdecision.model;

public class DeviceParm {
    private Integer treeId;
    private Integer deviceId;
    private Integer deviceType;
    private Integer bujianType;

    public Integer getTreeId() {
        return treeId;
    }

    public void setTreeId(Integer treeId) {
        this.treeId = treeId;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public Integer getBujianType() {
        return bujianType;
    }

    public void setBujianType(Integer bujianType) {
        this.bujianType = bujianType;
    }

    @Override
    public String toString() {
        return "DeviceParm{" +
                "treeId=" + treeId +
                ", deviceId=" + deviceId +
                ", deviceType=" + deviceType +
                ", bujianType=" + bujianType +
                '}';
    }
}
