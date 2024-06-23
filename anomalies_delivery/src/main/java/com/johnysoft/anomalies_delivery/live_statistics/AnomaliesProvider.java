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

import java.time.Duration;

import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;
import static lombok.AccessLevel.PRIVATE;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
class AnomaliesProvider {
    private static final int HISTORY_SIZE = 1000;
    private static final Duration MAX_AGE = Duration.ofMinutes(1);
    ReactiveMongoTemplate reactiveMongoTemplate;

    Sinks.Many<Measurement> measurements = Sinks.many().replay().limit(HISTORY_SIZE, MAX_AGE);
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
