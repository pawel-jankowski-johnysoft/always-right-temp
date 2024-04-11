package com.johnysoft.temperature_anomaly_analyzer.detect;

import com.johnysoft.measurement_generator.TemperatureMeasurement;
import com.johnysoft.temperature_anomaly_analyzer.kafka.SerdesFactories;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serde;
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

import java.util.Collections;

@Configuration
class AnomalyDetectionConfiguration {
    static final String TEMPERATURE_MEASUREMENTS = "temperature-measurements";
    static final String DETECTED_ANOMALIES = "detected-anomalies";

    @Bean
    NewTopic TEMPERATURE_MEASUREMENTS() {
        return TopicBuilder.name(TEMPERATURE_MEASUREMENTS)
                .replicas(1)
                .partitions(1)
                .build();
    }

    @Bean
    Serde<TemperatureMeasurement> anomalyDetectedValueSerde(@Value("${spring.kafka.properties.schema.registry.url:http://localhost:8081}") String schemaRegistry) {
        Serde<TemperatureMeasurement> serde = new SpecificAvroSerde<>();
        serde.configure(Collections.singletonMap(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,schemaRegistry), false);
        return serde;
    }

    @Bean
    Topology detectAnomalyTopology(StreamsBuilder streamsBuilder, Serde<TemperatureMeasurement> anomalyDetectedValueSerde, @Value("${anomaly.last_recent_measurements}") int lastRecentMeasurements, @Value("${anomaly.anomaly_threshold}") int anomalyThreshold) {

        var stream = streamsBuilder.stream(TEMPERATURE_MEASUREMENTS, Consumed.with(Serdes.Long(), anomalyDetectedValueSerde));
        stream.mapValues(InternalTemperatureMeasurement::from)
                .groupByKey()
                .aggregate(() -> AnomalyDetector.forMeasurementsWithThreshold(lastRecentMeasurements, anomalyThreshold), (key, value, aggregate) -> aggregate.process(value), Materialized.with(Serdes.Long(), SerdesFactories.fromJSONSerdes(AnomalyDetector.class)))
                .filter((key, value) -> value.anomalyDetected())
                .mapValues((unused, value) -> value.getAnomaly().toTemperatureMeasurement())
                .toStream()
                .to(DETECTED_ANOMALIES, Produced.with(Serdes.Long(), anomalyDetectedValueSerde));

        return streamsBuilder.build();
    }
}
