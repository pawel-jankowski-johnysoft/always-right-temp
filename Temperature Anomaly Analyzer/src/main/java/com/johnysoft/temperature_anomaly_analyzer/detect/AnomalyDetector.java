package com.johnysoft.temperature_anomaly_analyzer.detect;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import static lombok.AccessLevel.PACKAGE;

/*Getter is needed to store aggregate state */
@Getter
@NoArgsConstructor(access = PACKAGE)
class AnomalyDetector {

    private int lastRecentMeasurements;

    private int anomalyThreshold;

    private List<TemperatureMeasurement> measurements = new ArrayList<>();

    private int currentMeasurement = 0;

    private TemperatureMeasurement anomaly;


    private AnomalyDetector(int lastRecentMeasurements, int anomalyThreshold){
        this.anomalyThreshold = anomalyThreshold;
        this.lastRecentMeasurements = lastRecentMeasurements;
    }

    public AnomalyDetector process(TemperatureMeasurement measurement) {
        getPotentialAverageTemperature()
                .ifPresent(averageTemperature -> checkAnomaly(averageTemperature, measurement));
        addToMeasurements(measurement);
        return this;
    }

    private OptionalDouble getPotentialAverageTemperature() {
        return measurements.stream().mapToDouble(TemperatureMeasurement::temperature).average();
    }

    private void checkAnomaly(double averageTemperature, TemperatureMeasurement measurement) {
        anomaly = Math.abs(averageTemperature - measurement.temperature()) > anomalyThreshold ? measurement : null;
    }

    private void addToMeasurements(TemperatureMeasurement measurement) {
        measurements.add(currentMeasurement, measurement);
        currentMeasurement = (currentMeasurement+1) % lastRecentMeasurements;
    }

    public boolean anomalyDetected() {
        return this.anomaly != null;
    }

    static AnomalyDetector forMeasurementsWithThreshold(int lastRecentMeasurements, int anomalyThreshold) {
        return new AnomalyDetector(lastRecentMeasurements, anomalyThreshold);
    }
}
