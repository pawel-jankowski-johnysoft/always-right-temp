package com.johnysoft.anomalies_delivery.live_statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/anomalies")
@RequiredArgsConstructor
class AnomaliesController {

    private final AnomaliesProvider anomaliesProvider;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<Measurement>> measurements() {
        return ResponseEntity.status(OK)
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(anomaliesProvider.eventsStream());
    }

}
