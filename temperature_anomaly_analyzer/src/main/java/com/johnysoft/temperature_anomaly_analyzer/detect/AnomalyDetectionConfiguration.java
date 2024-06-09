package com.johnysoft.temperature_anomaly_analyzer.detect;

import com.johnysoft.measurement_generator.TemperatureMeasurement;
import com.johnysoft.temperature_anomaly_analyzer.kafka.SerdesFactories;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
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
import java.util.function.Consumer;
import java.util.function.Function;

@Configuration
class AnomalyDetectionConfiguration {
    static final String TEMPERATURE_MEASUREMENTS = "temperature-measurements";
    static final String ANOMALY_DETECTOR_STATE_STORE = "anomaly-detector-state-store";
    static final String SPRING_KAFKA_STREAMS_APPLICATION_ID_VALUE_QUERY = "${spring.kafka.streams.application-id}";

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(@Value(SPRING_KAFKA_STREAMS_APPLICATION_ID_VALUE_QUERY) String applicationId) {
        return registry -> registry.config().commonTags(MetricsRecorder.MetricsNames.APPLICATION_COMMON_TAG, applicationId);
    }

    @Bean
    NewTopic TEMPERATURE_MEASUREMENTS() {
        return TopicBuilder.name(TEMPERATURE_MEASUREMENTS)
                .replicas(1)
                .partitions(3)
                .build();
    }

    @Bean
    Function<KStream<Long, TemperatureMeasurement>, KStream<Long, TemperatureMeasurement>> anomalyDetection(@Value("${anomaly.last_recent_measurements}") int lastRecentMeasurements, @Value("${anomaly.anomaly_threshold}") int anomalyThreshold) {
        return s -> s.processValues(timeMeasurementConsumingSupplier())
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
                .processValues(timeMeasurementAnomalyDetectedSupplier());
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
