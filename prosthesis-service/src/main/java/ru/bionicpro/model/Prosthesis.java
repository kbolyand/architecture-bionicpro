package ru.bionicpro.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Prosthesis(UUID id, UUID userId, String serialNumber, String model, String manufacturer,
                         LocalDate installedAt, String status, OffsetDateTime createdAt) {
}
