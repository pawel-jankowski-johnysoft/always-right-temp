package com.johnysoft.temperature_anomaly_analyzer.detect;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * found anomalies Array vs Collection  ?
 * method static vs instance call from detector ?
 */
@RequiredArgsConstructor
class AnomaliesFinder {
    private final int anomalyThreshold;

    @Deprecated
    private static InternalTemperatureMeasurement isAnomalyForOthers(InternalTemperatureMeasurement current, InternalTemperatureMeasurement[] measurements, int anomalyThreshold) {
        return Arrays.stream(measurements).filter(Objects::nonNull).filter(internalTemperatureMeasurement -> current != internalTemperatureMeasurement).map(InternalTemperatureMeasurement::getTemperature).mapToDouble(Double::valueOf).average().stream().filter(it -> Math.abs(it - current.getTemperature()) > anomalyThreshold).boxed().findAny().map(unused -> current).orElse(null);
    }

    static Collection<InternalTemperatureMeasurement> find(Collection<InternalTemperatureMeasurement> measurements, int anomalyThreshold) {
        return measurements.stream().map(it -> isAnomalyForOthers(it, measurements, anomalyThreshold)).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private static InternalTemperatureMeasurement isAnomalyForOthers(InternalTemperatureMeasurement current, Collection<InternalTemperatureMeasurement> measurements, int anomalyThreshold) {
        return measurements.stream().filter(internalTemperatureMeasurement -> current != internalTemperatureMeasurement).map(InternalTemperatureMeasurement::getTemperature).mapToDouble(Double::valueOf).average().stream().filter(it -> Math.abs(it - current.getTemperature()) > anomalyThreshold).boxed().findAny().map(unused -> current).orElse(null);
    }

    /**
     * @deprecated should use returns collection one
     */
    @Deprecated()
    public InternalTemperatureMeasurement[] find(@NonNull InternalTemperatureMeasurement[] measurements) {
        return Arrays.stream(measurements).filter(Objects::nonNull).map(it -> isAnomalyForOthers(it, measurements, anomalyThreshold)).filter(Objects::nonNull).collect(Collectors.toSet()).toArray(InternalTemperatureMeasurement[]::new);
    }

    Collection<InternalTemperatureMeasurement> find(Collection<InternalTemperatureMeasurement> measurements) {
        return find(measurements, anomalyThreshold);
    }
}
