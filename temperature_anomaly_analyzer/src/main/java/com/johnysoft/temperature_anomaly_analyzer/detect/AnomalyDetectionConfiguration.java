package com.johnysoft.temperature_anomaly_analyzer.detect;

import com.johnysoft.temperature_anomaly_analyzer.kafka.SerdesFactories;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
class AnomalyDetectionConfiguration {
    static final String TEMPERATURE_MEASUREMENTS = "temperature-measurements";
    static final String ANOMALY_DETECTED = "anomaly-detected";

    @Bean
    NewTopic TEMPERATURE_MEASUREMENTS() {
        return TopicBuilder.name(TEMPERATURE_MEASUREMENTS)
                .replicas(1)
                .partitions(1)
                .build();
    }

    @Bean
    public Topology detectAnomalyTopology(StreamsBuilder streamsBuilder,
                                          @Value("${anomaly.last_recent_measurements}") int lastRecentMeasurements,
                                          @Value("${anomaly.anomaly_threshold}")int anomalyThreshold) {
        var stream = streamsBuilder.stream(TEMPERATURE_MEASUREMENTS, Consumed.with(Serdes.Long(), SerdesFactories.fromJSONSerdes(TemperatureMeasurement.class)));
        stream.groupByKey()
                .aggregate(() -> AnomalyDetector.forMeasurementsWithThreshold(lastRecentMeasurements, anomalyThreshold),
                        (key, value, aggregate) -> aggregate.process(value),
                        Materialized.with(Serdes.Long(), SerdesFactories.fromJSONSerdes(AnomalyDetector.class)))
                .filter((key, value) -> value.anomalyDetected())
                .mapValues(AnomalyDetector::getAnomaly)
                .toStream()
                .to(ANOMALY_DETECTED, Produced.with(Serdes.Long(), SerdesFactories.fromJSONSerdes(TemperatureMeasurement.class)));

        return streamsBuilder.build();
    }
}
