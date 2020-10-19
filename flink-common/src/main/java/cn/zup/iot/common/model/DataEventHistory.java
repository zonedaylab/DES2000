package cn.zup.iot.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @author zhangpeng
 * @date 2020/09/11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataEventHistory {

    /**
     * @param value
     */
    public DataEventHistory(DataEvent value) {
//        this.id = value.getId();
//        this.eventDate = value.getEventTime().toLocalDate();
//        this.hn = String.format("h%s", value.getEventTime().getHour());
//        this.lastValue = value.getDataValue();
    }

    private String id;

    private LocalDate eventDate;

    private String hn;

    private Integer lastValue;

}
