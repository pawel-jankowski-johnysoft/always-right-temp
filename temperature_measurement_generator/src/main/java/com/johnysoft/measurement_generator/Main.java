package com.johnysoft.measurement_generator;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.record.CompressionType;
import org.apache.kafka.common.serialization.LongSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

public class Main {
    private static final Random RANDOM = new Random();
    private static final String TEMPERATURE_MEASUREMENTS_TOPIC = "temperature-measurements";
    private static final String DEFAULT_BROKER_DEFAULT_ADDRESS = "kafka:9092";
    private static final String SCHEMA_REGISTRY_DEFAULT_URL = "http://localhost:8081";
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws Exception {
        KafkaProducer<Long, TemperatureMeasurement> producer = new KafkaProducer<>(prepareConfiguration());

        while (true) {
            TemperatureMeasurement measurement = generate();
            LOGGER.info("generated measurement: {}", measurement);
            producer.send(new ProducerRecord<>(TEMPERATURE_MEASUREMENTS_TOPIC, measurement.getThermometerId(), measurement));
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    private static Properties prepareConfiguration() {
        Properties properties = new Properties();
        properties.put(BOOTSTRAP_SERVERS_CONFIG, envVariableOrDefault(BOOTSTRAP_SERVERS_CONFIG, DEFAULT_BROKER_DEFAULT_ADDRESS));
        properties.put(KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        properties.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        properties.put(SCHEMA_REGISTRY_URL_CONFIG, envVariableOrDefault(SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_DEFAULT_URL));
        properties.put(COMPRESSION_TYPE_CONFIG, CompressionType.SNAPPY.name);
        properties.put(ACKS_CONFIG, "0");
        return properties;
    }

    private static String envVariableOrDefault(String variableName, String defaultValue) {
        return Optional.ofNullable(System.getenv(variableName))
                .orElse(defaultValue);
    }

    private static TemperatureMeasurement generate() {
        return new TemperatureMeasurement(RANDOM.nextLong(0, 6), RANDOM.nextFloat(18.0f, 36.6f), RANDOM.nextLong(1, 11), System.currentTimeMillis());
    }
}
