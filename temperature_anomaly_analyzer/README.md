
### connect to kafka connect container
docker exec -it alwaysrighttemp-kafka-connect-1 bash


### read detected anomalies using avro schema
kafka-avro-console-consumer --bootstrap-server kafka:9092 \
--property schema.registry.url=http://schema-registry:8081 \
--topic detected-anomalies --from-beginning


### This page describe multiple ways to process data using kafka / kafka streams. Choose one which you want to use
#### https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/#spring_cloud_function

### Page below redirects to configuration kafka streams using spring cloud stream
#### https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream-binder-kafka.html#_kafka_streams_binder

## CONFIGURATIONS AVAILABLE FOR `SPRING CLOUD STREAM KAFKA STREAMS`
### https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream-binder-kafka.html#_configuration_options_2
