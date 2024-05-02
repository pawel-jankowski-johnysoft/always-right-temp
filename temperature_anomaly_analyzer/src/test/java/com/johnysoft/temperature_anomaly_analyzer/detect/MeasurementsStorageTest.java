package com.johnysoft.temperature_anomaly_analyzer.detect;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class MeasurementsStorageTest {

    public static final int STORAGE_SIZE = 3;

    MeasurementsStorage storage = new MeasurementsStorage(STORAGE_SIZE);

    @Test
    public void anomalyExistsAfterAddToStorage() {
        //given
        InternalTemperatureMeasurement measurement = new InternalTemperatureMeasurement(1, 1, 36.6f, Instant.now().toEpochMilli());

        //when
        storage.add(measurement);

        //then
        InternalTemperatureMeasurement[] measurements = storage.getMeasurementsArray();
        Assertions.assertArrayEquals(new InternalTemperatureMeasurement[] {measurement}, measurements);
    }


    @Test
    public void overrideOldestAnomalyAfter() {
        //given
        InternalTemperatureMeasurement measurement = new InternalTemperatureMeasurement(1, 1, 36.6f, Instant.now().toEpochMilli());
        InternalTemperatureMeasurement measurement2 = new InternalTemperatureMeasurement(1, 1, 37.6f, Instant.now().toEpochMilli());
        InternalTemperatureMeasurement measurement3 = new InternalTemperatureMeasurement(1, 1, 35.6f, Instant.now().toEpochMilli());
        InternalTemperatureMeasurement measurement4 = new InternalTemperatureMeasurement(1, 1, 35.6f, Instant.now().toEpochMilli());

        //when
        storage.add(measurement);
        storage.add(measurement2);
        storage.add(measurement3);
        storage.add(measurement4);

        //then
        InternalTemperatureMeasurement[] measurements = storage.getMeasurementsArray();
        Assertions.assertArrayEquals(new InternalTemperatureMeasurement[] {measurement4,measurement2,measurement3}, measurements);
    }
}
