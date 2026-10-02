package ru.bionicpro.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.bionicpro.model.TelemetryClientDaily;
import ru.bionicpro.repository.TelemetryRepository;
import ru.bionicpro.service.TelemetryPdfService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reports")
public class TelemetryClientDailyController {
    private final TelemetryRepository telemetryRepository;
    private final TelemetryPdfService telemetryPdfService;

    public TelemetryClientDailyController(TelemetryRepository telemetryRepository, TelemetryPdfService telemetryPdfService) {
        this.telemetryRepository = telemetryRepository;
        this.telemetryPdfService = telemetryPdfService;
    }

    @GetMapping(produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> get(@AuthenticationPrincipal Jwt jwt) {
        List<TelemetryClientDaily> list = telemetryRepository.getByUserId(UUID.fromString(jwt.getSubject()));
        byte[] bytes = telemetryPdfService.generatePdf(list);
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=telemetry-report.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
