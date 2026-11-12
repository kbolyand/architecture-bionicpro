package ru.bionicpro.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TelemetryClientDaily(
        LocalDate reportDate,
        UUID userId,
        UUID prosthesisId,
        int channel,
        long samplesCount,
        double avgAmplitude,
        double minAmplitude,
        double maxAmplitude,
        double avgFrequency,
        double minFrequency,
        double maxFrequency,
        LocalDateTime firstEventTime,
        LocalDateTime lastEventTime
) {
}