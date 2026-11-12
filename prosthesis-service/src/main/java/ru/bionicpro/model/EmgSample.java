package ru.bionicpro.model;

import java.time.Instant;
import java.util.UUID;

public record EmgSample(Instant eventTime, UUID prosthesisId, String sensorId, int channel,
                        float amplitude, float frequency, long sequenceId) {
}
