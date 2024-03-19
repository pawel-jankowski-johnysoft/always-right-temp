package com.johnysoft.temperature_anomaly_analyzer.detect;

import com.johnysoft.measurement_generator.TemperatureMeasurement;
import lombok.*;
import lombok.experimental.FieldDefaults;

// Avro Serializer had a problem with serializing record
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
class InternalTemperatureMeasurement {
    long roomId;
    long thermometerId;
    @With float temperature;
    long timestamp;

    TemperatureMeasurement toTemperatureMeasurement() {
        return TemperatureMeasurement.newBuilder()
                .setTemperature(getTemperature())
                .setRoomId(getRoomId())
                .setThermometerId(getThermometerId())
                .setTimestamp(getTimestamp())
                .build();
    }

    static InternalTemperatureMeasurement from(TemperatureMeasurement measurement) {
        return new InternalTemperatureMeasurement(measurement.getRoomId(), measurement.getThermometerId(),
                measurement.getTemperature(), measurement.getTimestamp());
    }
}
