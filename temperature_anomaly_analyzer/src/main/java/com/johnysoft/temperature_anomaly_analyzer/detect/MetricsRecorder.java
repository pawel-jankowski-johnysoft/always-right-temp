package com.johnysoft.temperature_anomaly_analyzer.detect;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

import static com.johnysoft.temperature_anomaly_analyzer.detect.AnomalyDetectionConfiguration.SPRING_KAFKA_STREAMS_APPLICATION_ID_VALUE_QUERY;

@Component
class MetricsRecorder {
    private static String applicationName;

    @Autowired
    MetricsRecorder(@Value(SPRING_KAFKA_STREAMS_APPLICATION_ID_VALUE_QUERY) String appName) {
        applicationName = appName;
    }

    static void recordTemperatureMeasurementConsuming(Instant timestamp) {
        Metrics.timer(MetricsNames.TEMPERATURE_MEASUREMENT_CONSUMING, app())
                .record(Duration.between(timestamp, Instant.now()));
    }

    static void recordAnomalyDetectingTime(Instant beforeProcessing) {
        Metrics.timer(MetricsNames.ANOMALY_DETECTING_METRIC, app())
                .record(Duration.between(beforeProcessing, Instant.now()));
    }

    static void recordDetectedAnomaly() {
        Metrics.counter(MetricsNames.DETECTED_ANOMALIES, app()).increment();
    }

    private static Tags app() {
        return Tags.of(MetricsNames.APP_TAG, applicationName);
    }

    static class MetricsNames {
        private static final String TEMPERATURE_MEASUREMENT_CONSUMING = "temperature_measurement_consuming";
        private static final String ANOMALY_DETECTING_METRIC = "anomaly_detecting";
        private static final String DETECTED_ANOMALIES = "detected_anomalies";

        private static final String APP_TAG = "app";
        static final String APPLICATION_COMMON_TAG = "application";
    }
}
