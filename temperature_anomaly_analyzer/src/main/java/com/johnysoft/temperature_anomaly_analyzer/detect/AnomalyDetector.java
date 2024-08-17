package com.johnysoft.temperature_anomaly_analyzer.detect;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.OptionalDouble;

import static lombok.AccessLevel.PACKAGE;

/*Getter is needed to store aggregate state */
@Getter
@NoArgsConstructor(access = PACKAGE)
@Slf4j
class AnomalyDetector {

    private int lastRecentMeasurements;

    private int anomalyThreshold;

    private InternalTemperatureMeasurement[] measurements;

    private int currentMeasurement = 0;

    private InternalTemperatureMeasurement anomaly;


    private AnomalyDetector(int lastRecentMeasurements, int anomalyThreshold) {
        this.anomalyThreshold = anomalyThreshold;
        this.lastRecentMeasurements = lastRecentMeasurements;
        measurements = new InternalTemperatureMeasurement[lastRecentMeasurements];
    }

    AnomalyDetector process(InternalTemperatureMeasurement measurement) {
        log.debug("process measurement: {}", measurement);
        Instant beforeProcessing = Instant.now();
        getPotentialAverageTemperature()
                .ifPresent(averageTemperature -> checkAnomaly(averageTemperature, measurement));
        addToMeasurements(measurement);
        MetricsRecorder.recordAnomalyDetectingTime(beforeProcessing);
        return this;
    }

    private OptionalDouble getPotentialAverageTemperature() {
        return Arrays.stream(measurements).filter(Objects::nonNull).mapToDouble(InternalTemperatureMeasurement::getTemperature).average();
    }

    private void checkAnomaly(double averageTemperature, InternalTemperatureMeasurement measurement) {
        anomaly = Math.abs(averageTemperature - measurement.getTemperature()) > anomalyThreshold ? measurement : null;
    }

    private void addToMeasurements(InternalTemperatureMeasurement measurement) {
        measurements[currentMeasurement] = measurement;
        currentMeasurement = (currentMeasurement + 1) % lastRecentMeasurements;
    }

    public boolean anomalyDetected() {
        return this.anomaly != null;
    }

    static AnomalyDetector forMeasurementsWithThreshold(int lastRecentMeasurements, int anomalyThreshold) {
        return new AnomalyDetector(lastRecentMeasurements, anomalyThreshold);
    }
}
