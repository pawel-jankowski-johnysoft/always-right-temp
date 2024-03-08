package com.johnysoft.temperature_anomaly_analyzer.detect;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
class AnomalyDetectionConfiguration {

    @Bean
    NewTopic TEMPERATURE_MEASUREMENTS() {
        return TopicBuilder.name(AnomalyDetectionStream.TEMPERATURE_MEASUREMENTS)
                .replicas(1)
                .partitions(1)
                .build();
    }
}
