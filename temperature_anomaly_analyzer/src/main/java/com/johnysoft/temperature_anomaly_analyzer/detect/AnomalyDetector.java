package com.johnysoft.temperature_anomaly_analyzer.detect;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

import static lombok.AccessLevel.PACKAGE;

/*Getter is needed to store aggregate state */
@Data
@NoArgsConstructor(access = PACKAGE)
class AnomalyDetector {
    private MeasurementsStorage storage;
    private int anomalyThreshold;
    private Collection<InternalTemperatureMeasurement> anomalies;

    private AnomalyDetector(MeasurementsStorage storage, int anomalyThreshold) {
        this.storage = storage;
        this.anomalyThreshold = anomalyThreshold;
    }

    public AnomalyDetector process(InternalTemperatureMeasurement measurement) {
        storage.add(measurement);
        this.anomalies = AnomaliesFinder.find(storage.getMeasurementsView(), anomalyThreshold);
        return this;
    }

    public boolean anomaliesDetected() {
        return !anomalies.isEmpty();
    }

    static AnomalyDetector create(int lastRecentMeasurements, int anomalyThreshold) {
        MeasurementsStorage storage = new MeasurementsStorage(lastRecentMeasurements);
        return new AnomalyDetector(storage, anomalyThreshold);
    }
}
