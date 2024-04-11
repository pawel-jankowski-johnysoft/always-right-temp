
### connect to kafka connect container
docker exec -it alwaysrighttemp-kafka-connect-1 bash


### read detected anomalies using avro schema
kafka-avro-console-consumer --bootstrap-server kafka:9092 \
--property schema.registry.url=http://schema-registry:8081 \
--topic detected-anomalies --from-beginning
