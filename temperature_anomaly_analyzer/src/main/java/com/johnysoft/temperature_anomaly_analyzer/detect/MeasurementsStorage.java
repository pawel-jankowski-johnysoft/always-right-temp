package com.johnysoft.temperature_anomaly_analyzer.detect;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static lombok.AccessLevel.PACKAGE;

/*
 * Storage based on Arrav vs List
 *
 * Deserialization gives unexpected sizes
 *
 * Remember to remove unused version (one returning array now)
 * */

@NoArgsConstructor(access = PACKAGE)
@Getter
class MeasurementsStorage {
    private int storageSize;
    private int currentIndex = 0;
    private List<InternalTemperatureMeasurement> measurements;

    MeasurementsStorage(int storageSize) {
        this.storageSize = storageSize;
        this.measurements = new ArrayList<>(storageSize);
    }

    void add(InternalTemperatureMeasurement measurement) {
        int position = currentIndex++ % storageSize;
        if (measurements.size() > position) measurements.set(position, measurement);
        else measurements.add(position, measurement);
    }

    /**
     * @deprecated use returns collection one
     */
    @Deprecated
    InternalTemperatureMeasurement[] getMeasurementsArray() {
        return measurements.toArray(InternalTemperatureMeasurement[]::new);
    }

    Collection<InternalTemperatureMeasurement> getMeasurementsView() {
        return Collections.unmodifiableCollection(measurements);
    }
}
