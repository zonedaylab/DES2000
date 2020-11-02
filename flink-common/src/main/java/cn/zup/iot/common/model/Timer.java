package cn.zup.iot.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
}
