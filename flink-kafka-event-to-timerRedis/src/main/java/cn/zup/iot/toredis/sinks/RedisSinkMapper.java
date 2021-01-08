package cn.zup.iot.toredis.sinks;

import cn.zup.iot.common.model.DataEvent;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

/**
 * RedisSinkMapper类的功能就是将我们自己的输入数据映射到redis的对应的类型。
 * @Author 史善力
 * @date 2020年12月23日19:24:37
 */
public class RedisSinkMapper implements RedisMapper<DataEvent> {
    /**
     * getCommandDescription：主要来获取我们写入哪种类型的数据，比如list、hash等等。
     * @Author 史善力
     * @date 2020年12月23日19:33:15
     * @return RedisCommandDescription
     */
    @Override
    public RedisCommandDescription getCommandDescription() {
        return new RedisCommandDescription(RedisCommand.SET);
    }

    /**
     * 从我们传来的DataEvent对象中抽取key值
     * @Author  史善
     * @date 2020年12月23日19:35:25
     * @param dataEvent DataEvent对象
     * @return String
     */

    @Override
    public String getKeyFromData(DataEvent dataEvent) {
        System.out.println("data.getKeyFromData" + dataEvent);
        //将传来的DataEvent对象变成字符串
        return String.format("%s,%s,%s,%s", dataEvent.getComponentType(),dataEvent.getComponentId(),dataEvent.getComponentParamId(),dataEvent.getStationId());
    }

    /**
     * 从我们传来的DataEvent对象中抽取value值
     * @Author 史善力
     * @date 2020年12月23日19:37:22
     * @param dataEvent DataEvent对象
     * @return String
     */
    @Override
    public String getValueFromData(DataEvent dataEvent) {
        return String.valueOf(dataEvent.getDataValue());
    }
}
