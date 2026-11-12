package ru.bionicpro.model;

import java.time.LocalDate;

public record CreateProsthesisRequest(String serialNumber, String model, String manufacturer, LocalDate installedAt,
                                      String status) {
}
