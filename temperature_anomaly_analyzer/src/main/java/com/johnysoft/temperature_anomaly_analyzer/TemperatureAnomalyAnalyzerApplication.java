package com.johnysoft.temperature_anomaly_analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
public class TemperatureAnomalyAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TemperatureAnomalyAnalyzerApplication.class, args);
    }
}
