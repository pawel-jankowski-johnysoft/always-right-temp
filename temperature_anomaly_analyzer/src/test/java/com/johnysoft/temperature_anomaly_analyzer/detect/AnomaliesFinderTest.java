package com.johnysoft.temperature_anomaly_analyzer.detect;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

class AnomaliesFinderTest {

    public static final int ANOMALY_THRESHOLD = 5;
    AnomaliesFinder anomaliesFinder = new AnomaliesFinder(ANOMALY_THRESHOLD);
    @Test
    public void noAnomaliesWithEmptyArray() {
        // when
        InternalTemperatureMeasurement[] found = anomaliesFinder.find(new InternalTemperatureMeasurement[0]);

        //expect
        Assertions.assertThat(found).isEmpty();
    }

    @Test
    public void noAnomaliesForSingleMeasurement() {
        // when
        InternalTemperatureMeasurement[] found = anomaliesFinder.find(new InternalTemperatureMeasurement[]{new InternalTemperatureMeasurement(1,1, 36.6f, Instant.now().toEpochMilli())});

        //expect
        Assertions.assertThat(found).isEmpty();
    }

    @Test
    public void noAnomaliesForMeasurementsWithAcceptedThreshold() {
        // when
        InternalTemperatureMeasurement[] found = anomaliesFinder.find(new InternalTemperatureMeasurement[]{
                new InternalTemperatureMeasurement(1,1, 36.6f, Instant.now().toEpochMilli()),
                new InternalTemperatureMeasurement(1,1, 36.6f + ANOMALY_THRESHOLD - 2, Instant.now().toEpochMilli()),
        });

        //expect
        Assertions.assertThat(found).isEmpty();
    }

    @Test
    public void anomaliesReturnedWhenMeasurementsOutOfThreshold() {
        // when
        InternalTemperatureMeasurement firstAnomaly = new InternalTemperatureMeasurement(1, 1, 36.6f, Instant.now().toEpochMilli());
        InternalTemperatureMeasurement secondAnomaly = new InternalTemperatureMeasurement(1, 1, 36.6f + ANOMALY_THRESHOLD + 1, Instant.now().toEpochMilli());
        InternalTemperatureMeasurement[] found = anomaliesFinder.find(new InternalTemperatureMeasurement[]{
                firstAnomaly,
                secondAnomaly,
        });

        //expect
        Assertions.assertThat(found).containsOnly(firstAnomaly, secondAnomaly);
    }

    @Test
    public void noAnomaliesWhenMeasurementsWithExactlyThreshold() {
        // when
        InternalTemperatureMeasurement firstAnomaly = new InternalTemperatureMeasurement(1, 1, 36.6f, Instant.now().toEpochMilli());
        InternalTemperatureMeasurement secondAnomaly = new InternalTemperatureMeasurement(1, 1, 36.6f + ANOMALY_THRESHOLD, Instant.now().toEpochMilli());
        InternalTemperatureMeasurement[] found = anomaliesFinder.find(new InternalTemperatureMeasurement[]{
                firstAnomaly,
                secondAnomaly,
        });

        //expect
        Assertions.assertThat(found).isEmpty();
    }








    @Test
    public void noAnomaliesForSingleMeasurement_collection() {
        // when
        Collection<InternalTemperatureMeasurement> found = anomaliesFinder.find(Collections.singleton(new InternalTemperatureMeasurement(1,1, 36.6f, Instant.now().toEpochMilli())));

        //expect
        Assertions.assertThat(found).isEmpty();
    }

    @Test
    public void noAnomaliesForMeasurementsWithAcceptedThreshold_collection() {
        // when
        Collection<InternalTemperatureMeasurement> found = anomaliesFinder.find(List.of(
                new InternalTemperatureMeasurement(1,1, 36.6f, Instant.now().toEpochMilli()),
                new InternalTemperatureMeasurement(1,1, 36.6f + ANOMALY_THRESHOLD - 2, Instant.now().toEpochMilli())
                ));

        //expect
        Assertions.assertThat(found).isEmpty();
    }

    @Test
    public void anomaliesReturnedWhenMeasurementsOutOfThreshold_collection() {
        // when
        InternalTemperatureMeasurement firstAnomaly = new InternalTemperatureMeasurement(1, 1, 36.6f, Instant.now().toEpochMilli());
        InternalTemperatureMeasurement secondAnomaly = new InternalTemperatureMeasurement(1, 1, 36.6f + ANOMALY_THRESHOLD + 1, Instant.now().toEpochMilli());
        Collection<InternalTemperatureMeasurement> found = anomaliesFinder.find(List.of(firstAnomaly,secondAnomaly));

        //expect
        Assertions.assertThat(found).containsOnly(firstAnomaly, secondAnomaly);
    }

    @Test
    public void noAnomaliesWhenMeasurementsWithExactlyThreshold_collection() {
        // when
        InternalTemperatureMeasurement firstAnomaly = new InternalTemperatureMeasurement(1, 1, 36.6f, Instant.now().toEpochMilli());
        InternalTemperatureMeasurement secondAnomaly = new InternalTemperatureMeasurement(1, 1, 36.6f + ANOMALY_THRESHOLD, Instant.now().toEpochMilli());
        Collection<InternalTemperatureMeasurement> found = anomaliesFinder.find(List.of(firstAnomaly, secondAnomaly));

        //expect
        Assertions.assertThat(found).isEmpty();
    }
}
