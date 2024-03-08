package com.johnysoft.temperature_anomaly_analyzer.detect;

import com.johnysoft.temperature_anomaly_analyzer.kafka.SerdesFactories;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class AnomalyDetectionStream {

    static final String TEMPERATURE_MEASUREMENTS = "temperature-measurements";


    @Autowired
    public Topology detectAnomaly(StreamsBuilder streamsBuilder,
                                   @Value("${anomaly.last_recent_measurements}") int lastRecentMeasurements,
                                  @Value("${anomaly.anomaly_threshold}")int anomalyThreshold) {

        var stream = streamsBuilder.stream(TEMPERATURE_MEASUREMENTS, Consumed.with(Serdes.Long(), SerdesFactories.fromJSONSerdes(TemperatureMeasurement.class)));
        stream.groupByKey()
                .aggregate(() -> AnomalyDetector.forMeasurementsWithThreshold(lastRecentMeasurements, anomalyThreshold),
                        (key, value, aggregate) -> aggregate.process(value),
                        Materialized.with(Serdes.Long(), SerdesFactories.fromJSONSerdes(AnomalyDetector.class)))
                .filter((key, value) -> value.anomalyDetected())
                .toStream()
                .foreach((key, value) -> System.out.printf("termometr %d ma anomalię: %s \n", key, value.getAnomaly()));

        return streamsBuilder.build();
    }
}
