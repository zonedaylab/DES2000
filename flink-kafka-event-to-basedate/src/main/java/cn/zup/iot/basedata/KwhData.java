package cn.zup.iot.basedata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/***
 * 电度表
 */
public class KwhData {

    private int componentType;
    private int componentId;
    private int componentParamId;
    private int stationId;

    private long eventTime;

    private String dataValue;

}
