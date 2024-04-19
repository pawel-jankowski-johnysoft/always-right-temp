package com.johnysoft.anomalies_delivery.live_statistics;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;
import static lombok.AccessLevel.PRIVATE;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
class AnomaliesProvider {
    ReactiveMongoTemplate reactiveMongoTemplate;

    Sinks.Many<Measurement> measurements = Sinks.many().replay().all();
    Scheduler measurementsScheduler = Schedulers.fromExecutor(newVirtualThreadPerTaskExecutor());

    @PostConstruct()
    public void init() {
        reactiveMongoTemplate.changeStream(Measurement.class)
                .listen()
                .mapNotNull(ChangeStreamEvent::getBody)
                .subscribe(measurements::tryEmitNext);
    }

    Flux<Measurement> eventsStream() {
        return measurements.asFlux().publishOn(measurementsScheduler);
    }
}
