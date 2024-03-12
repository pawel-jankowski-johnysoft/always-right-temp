package com.johnysoft.temperature_anomaly_analyzer.detect;

import lombok.*;
import lombok.experimental.FieldDefaults;

// Avro Serializer had a problem with serializing record
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
class TemperatureMeasurement {
    long roomId;
    long thermometerId;
    @With float temperature;
    long timestamp;
}
