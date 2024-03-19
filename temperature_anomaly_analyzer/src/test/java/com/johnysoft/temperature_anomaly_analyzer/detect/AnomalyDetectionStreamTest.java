package com.johnysoft.temperature_anomaly_analyzer.detect;

import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Date;
import java.util.Objects;
import java.util.Properties;

import static com.johnysoft.temperature_anomaly_analyzer.detect.AnomalyDetectionConfiguration.ANOMALY_DETECTED;
import static com.johnysoft.temperature_anomaly_analyzer.detect.AnomalyDetectionConfiguration.TEMPERATURE_MEASUREMENTS;

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
        Topology topology = CONFIGURATION.detectAnomalyTopology(streamsBuilder, LAST_RECENT_MEASUREMENTS, ANOMALY_THRESHOLD);

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
            inputTopic.pipeInput(1L, new TemperatureMeasurement(1L, 1L, 30.0f, new Date()));

            //then
            Assertions.assertTrue(outputTopic.isEmpty());
    }

    @Test
    void noAnomaliesDetectedUntilTheThresholdHasBeenExceeded() {
            // when
            inputTopic.pipeInput(1L, new TemperatureMeasurement(1L, 1L, 30.0f, new Date()));
            inputTopic.pipeInput(1L, new TemperatureMeasurement(1L, 1L, 32.0f, new Date()));
            inputTopic.pipeInput(1L, new TemperatureMeasurement(1L, 1L, 34.0f, new Date()));
            inputTopic.pipeInput(1L, new TemperatureMeasurement(1L, 1L, 36.0f, new Date()));
            inputTopic.pipeInput(1L, new TemperatureMeasurement(1L, 1L, 38.0f, new Date()));

            //then
            Assertions.assertTrue(outputTopic.isEmpty());
    }


    @Test
    void emitAnomalyWhenHasBeenDetected() {
        // given
        TemperatureMeasurement initialMeasurement = new TemperatureMeasurement(1L, 1L, 30.0f, new Date());
        TemperatureMeasurement anomaly = initialMeasurement.withTemperature(initialMeasurement.temperature() - (ANOMALY_THRESHOLD + 0.1f));

        // when
        inputTopic.pipeInput(initialMeasurement.thermometerId(), initialMeasurement);
        inputTopic.pipeInput(anomaly.thermometerId(), anomaly);

        //then
        Assertions.assertEquals(outputTopic.readValue(), anomaly);
    }

    @Test
    void onlyOneValidAnomalyDetectedInGivenMeasurements() {
        // given
        TemperatureMeasurement initialMeasurement = new TemperatureMeasurement(1L, 1L, 30.0f, new Date());
        float anomalyTempearture = initialMeasurement.temperature() - (ANOMALY_THRESHOLD + 0.1f);

        //and
        TemperatureMeasurement anomaly = initialMeasurement.withTemperature(anomalyTempearture);

        TemperatureMeasurement nonAnomalyMeasurement = initialMeasurement.withTemperature(anomalyTempearture + ((float) ANOMALY_THRESHOLD / 2));

        // when
        inputTopic.pipeInput(initialMeasurement.thermometerId(), initialMeasurement);
        inputTopic.pipeInput(anomaly.thermometerId(), anomaly);
        inputTopic.pipeInput(initialMeasurement.thermometerId(), nonAnomalyMeasurement);

        //then
        Assertions.assertEquals(outputNonNullMeasurements(), 1);

    }

    private long outputNonNullMeasurements() {
        return outputTopic.readValuesToList()
                .stream().filter(Objects::nonNull)
                .count();
    }
}
