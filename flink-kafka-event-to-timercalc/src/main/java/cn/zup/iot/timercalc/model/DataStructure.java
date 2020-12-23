package cn.zup.iot.timercalc.model;
import java.io.Serializable;

public class DataStructure  implements Serializable {
    private static final long serialVersionUID = -8289770787953160443L;

    long time; //数据库查询出的时间转换为long类型
    double value;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
