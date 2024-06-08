package com.johnysoft.temperature_anomaly_analyzer.kafka;

import lombok.NoArgsConstructor;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class SerdesFactories {
    public static  <T> Serde<T> JSONSerdes(Class<T> tClass) {
        return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(tClass));
    }
}
