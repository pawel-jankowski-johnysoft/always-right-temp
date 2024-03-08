package com.johnysoft.temperature_anomaly_analyzer.detect;

import java.util.Date;

record TemperatureMeasurement(long roomId, long thermometerId, float temperature, Date timestamp) {
}
