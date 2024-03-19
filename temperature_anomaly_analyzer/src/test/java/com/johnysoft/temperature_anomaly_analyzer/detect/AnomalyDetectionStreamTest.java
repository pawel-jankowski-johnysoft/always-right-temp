package com.johnysoft.temperature_anomaly_analyzer.detect;

import com.johnysoft.measurement_generator.TemperatureMeasurement;
import com.johnysoft.temperature_anomaly_analyzer.kafka.SerdesFactories;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Instant;
import java.util.Objects;
import java.util.Properties;

import static com.johnysoft.temperature_anomaly_analyzer.detect.AnomalyDetectionConfiguration.ANOMALY_DETECTED;
import static com.johnysoft.temperature_anomaly_analyzer.detect.AnomalyDetectionConfiguration.TEMPERATURE_MEASUREMENTS;

@Disabled("""
Tests needs to be rewritten in one of ways:
- to test only anomaly detector
- stay like now with avro deserializer (if will be possible)
""")
class AnomalyDetectionStreamTest {
    private static final AnomalyDetectionConfiguration CONFIGURATION = new AnomalyDetectionConfiguration();
    private static final int LAST_RECENT_MEASUREMENTS = 2;
    private static final int ANOMALY_THRESHOLD = 5;
    private TopologyTestDriver topologyTestDriver;
    private TestInputTopic<Long, TemperatureMeasurement> inputTopic;
    private TestOutputTopic<Long, TemperatureMeasurement> outputTopic;

    @BeforeEach
    public void init() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        Topology topology = CONFIGURATION.detectAnomalyTopology(streamsBuilder, SerdesFactories.fromJSONSerdes(TemperatureMeasurement.class), LAST_RECENT_MEASUREMENTS, ANOMALY_THRESHOLD);

        topologyTestDriver = new TopologyTestDriver(topology, new Properties());
        this.inputTopic = topologyTestDriver.createInputTopic(TEMPERATURE_MEASUREMENTS, new LongSerializer(), new JsonSerializer<>());

        JsonDeserializer<TemperatureMeasurement> valueDeserializer = new JsonDeserializer<>();
        valueDeserializer.addTrustedPackages("*");
        this.outputTopic = topologyTestDriver.createOutputTopic(ANOMALY_DETECTED, new LongDeserializer(), valueDeserializer);

    }

    @AfterEach
    public void close() {
        this.topologyTestDriver.close();
    }


    @Test
    void noAnomaliesForSingleMeasurement() {
        // when
        inputTopic.pipeInput(1L, new TemperatureMeasurement(1L, 30.0f, 1L, Instant.now().toEpochMilli()));

        //then
        Assertions.assertTrue(outputTopic.isEmpty());
    }

    @Test
    void noAnomaliesDetectedUntilTheThresholdHasBeenExceeded() {
        // when
        inputTopic.pipeInput(1L, new TemperatureMeasurement(1L, 30.0f, 1L, Instant.now().toEpochMilli()));
        inputTopic.pipeInput(1L, new TemperatureMeasurement(1L, 32.0f, 1L, Instant.now().toEpochMilli()));
        inputTopic.pipeInput(1L, new TemperatureMeasurement(1L, 34.0f, 1L, Instant.now().toEpochMilli()));
        inputTopic.pipeInput(1L, new TemperatureMeasurement(1L, 36.0f, 1L, Instant.now().toEpochMilli()));
        inputTopic.pipeInput(1L, new TemperatureMeasurement(1L, 38.0f, 1L, Instant.now().toEpochMilli()));

        //then
        Assertions.assertTrue(outputTopic.isEmpty());
    }


    @Test
    void emitAnomalyWhenHasBeenDetected() {
        // given
        TemperatureMeasurement initialMeasurement = new TemperatureMeasurement(1L, 30.0f, 1L, Instant.now().toEpochMilli());
        TemperatureMeasurement anomaly = TemperatureMeasurement.newBuilder(initialMeasurement).setTemperature(initialMeasurement.getTemperature() - (ANOMALY_THRESHOLD + 0.1f)).build();

        // when
        inputTopic.pipeInput(initialMeasurement.getThermometerId(), initialMeasurement);
        inputTopic.pipeInput(anomaly.getThermometerId(), anomaly);

        //then
        Assertions.assertEquals(outputTopic.readValue(), anomaly);
    }

    @Test
    void onlyOneValidAnomalyDetectedInGivenMeasurements() {
        // given
        TemperatureMeasurement initialMeasurement = new TemperatureMeasurement(1L, 30.0f, 1L, Instant.now().toEpochMilli());
        float anomalyTempearture = initialMeasurement.getTemperature() - (ANOMALY_THRESHOLD + 0.1f);

        //and
        TemperatureMeasurement anomaly = TemperatureMeasurement.newBuilder(initialMeasurement).setTemperature(anomalyTempearture).build();

        TemperatureMeasurement nonAnomalyMeasurement = TemperatureMeasurement.newBuilder(initialMeasurement).setTemperature(anomalyTempearture + ((float) ANOMALY_THRESHOLD / 2)).build();

        // when
        inputTopic.pipeInput(initialMeasurement.getThermometerId(), initialMeasurement);
        inputTopic.pipeInput(anomaly.getThermometerId(), anomaly);
        inputTopic.pipeInput(initialMeasurement.getThermometerId(), nonAnomalyMeasurement);

        //then
        Assertions.assertEquals(outputNonNullMeasurements(), 1);

    }

    private long outputNonNullMeasurements() {
        return outputTopic.readValuesToList().stream().filter(Objects::nonNull).count();
    }
}
