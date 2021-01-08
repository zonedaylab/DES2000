package cn.zup.iot.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * kafka中的定时消息，根据定时消息判断是几分钟时间信号
 * @author shishanli
 * @date 2021年1月5日00:41:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/***
 * kafka 传输数据实体结构
 */
public class Timer {
    private String timerName;
    private LocalDateTime timerTime;
    private int timerType;

    public String getTimerName() {
        return timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }

    public LocalDateTime getTimerTime() {
        return timerTime;
    }

    public void setTimerTime(LocalDateTime timerTime) {
        this.timerTime = timerTime;
    }

    public int getTimerType() {
        return timerType;
    }

    public void setTimerType(int timerType) {
        this.timerType = timerType;
    }
}
