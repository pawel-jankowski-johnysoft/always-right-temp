package com.johnysoft.temperature_anomaly_analyzer.detect;

import lombok.With;

import java.util.Date;

@With
record TemperatureMeasurement(long roomId, long thermometerId, float temperature, Date timestamp) {
}
