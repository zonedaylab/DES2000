package cn.zup.iot.toredis.sinks;

import cn.zup.iot.common.model.DataEvent;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

//RedisSinkMapper类的功能就是将我们自己的输入数据映射到redis的对应的类型。
public class RedisSinkMapper implements RedisMapper<DataEvent> {
    //getCommandDescription：主要来获取我们写入哪种类型的数据，比如list、hash等等。
    @Override
    public RedisCommandDescription getCommandDescription() {
        return new RedisCommandDescription(RedisCommand.SET);
    }

    //getKeyFromData：主要是从我们的输入数据中抽取key
    @Override
    public String getKeyFromData(DataEvent data) {
        System.out.println("data.getKeyFromData" + data);
        return String.format("%s,%s,%s,%s", data.getComponentType(),data.getComponentId(),data.getComponentParamId(),data.getStationId());
    }
    @Override
    public String getValueFromData(DataEvent data) {
        return String.valueOf(data.getDataValue());
    }
}
