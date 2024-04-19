package com.johnysoft.anomalies_delivery.live_statistics;

import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

import static lombok.AccessLevel.PRIVATE;

@Document(Measurement.DETECTED_ANOMALIES)
@Data
@FieldDefaults(level = PRIVATE)
public class Measurement {
    static final String DETECTED_ANOMALIES = "detected-anomalies";

    long roomId;
    long thermometerId;
    float temperature;
    long timestamp;
}
