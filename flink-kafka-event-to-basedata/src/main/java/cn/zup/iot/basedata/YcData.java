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
 * 遥测表存盘
 */
public class YcData {

    private int componentType;
    private int componentId;
    private int componentParamId;
    private int stationId;

    private long eventTime;

    private String dataValue;

}
