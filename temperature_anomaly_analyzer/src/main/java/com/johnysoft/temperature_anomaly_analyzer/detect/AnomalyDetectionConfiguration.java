package com.johnysoft.temperature_anomaly_analyzer.detect;

import com.johnysoft.measurement_generator.TemperatureMeasurement;
import com.johnysoft.temperature_anomaly_analyzer.kafka.SerdesFactories;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorSupplier;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.function.Consumer;

@Configuration
class AnomalyDetectionConfiguration {
    static final String TEMPERATURE_MEASUREMENTS = "temperature-measurements";
    static final String DETECTED_ANOMALIES = "detected-anomalies";
    private static final String ANOMALY_DETECTOR_STATE_STORE = "anomaly-detector-state-store";
    static final String SPRING_KAFKA_STREAMS_APPLICATION_ID_VALUE_QUERY = "${spring.kafka.streams.application-id}";

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(@Value(SPRING_KAFKA_STREAMS_APPLICATION_ID_VALUE_QUERY) String applicationId) {
        return registry -> registry.config().commonTags("application", applicationId);
    }

    @Bean
    NewTopic TEMPERATURE_MEASUREMENTS() {
        return TopicBuilder.name(TEMPERATURE_MEASUREMENTS)
                .replicas(1)
                .partitions(3)
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
        stream.processValues(timeMeasurementConsumingSupplier())
                .mapValues(InternalTemperatureMeasurement::from)
                .groupByKey()
                .aggregate(
                        () -> AnomalyDetector.forMeasurementsWithThreshold(lastRecentMeasurements, anomalyThreshold),
                        (key, value, aggregate) -> aggregate.process(value),
                        Materialized.<Long, AnomalyDetector, KeyValueStore<Bytes, byte[]>>as(ANOMALY_DETECTOR_STATE_STORE)
                                .withKeySerde(Serdes.Long())
                                .withValueSerde(SerdesFactories.JSONSerdes(AnomalyDetector.class))
                                .withRetention(Duration.ofMinutes(5))
                                )
                .filter((key, value) -> value.anomalyDetected())
                .mapValues((unused, value) -> value.getAnomaly().toTemperatureMeasurement())
                .toStream()
                .filter((key, value) -> value != null)
                .processValues(timeMeasurementAnomalyDetectedSupplier())
                .to(DETECTED_ANOMALIES, Produced.with(Serdes.Long(), anomalyDetectedValueSerde));

        return streamsBuilder.build();
    }

    private FixedKeyProcessorSupplier<Long, TemperatureMeasurement, TemperatureMeasurement> timeMeasurementConsumingSupplier() {
        return fixKeyProcessorSuplier(record -> MetricsRecorder.recordTemperatureMeasurementConsuming(Instant.ofEpochMilli(record.timestamp())));
    }

    private FixedKeyProcessorSupplier<Long, TemperatureMeasurement, TemperatureMeasurement> timeMeasurementAnomalyDetectedSupplier() {
        return fixKeyProcessorSuplier(__ -> MetricsRecorder.recordDetectedAnomaly());
    }

    private FixedKeyProcessorSupplier<Long, TemperatureMeasurement, TemperatureMeasurement> fixKeyProcessorSuplier(Consumer<FixedKeyRecord<Long, TemperatureMeasurement>> action) {
        return () -> new FixedKeyProcessor<>() {
            private FixedKeyProcessorContext<Long, TemperatureMeasurement> context;

            @Override
            public void init(FixedKeyProcessorContext<Long, TemperatureMeasurement> context) {
                this.context = context;
            }

            @Override
            public void process(FixedKeyRecord<Long, TemperatureMeasurement> record) {
                action.accept(record);
                context.forward(record);
            }
        };
    }
}
