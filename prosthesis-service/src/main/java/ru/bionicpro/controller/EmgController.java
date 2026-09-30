package ru.bionicpro.controller;

import org.springframework.web.bind.annotation.*;
import ru.bionicpro.model.EmgSample;
import ru.bionicpro.repository.EmgSampleRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/emg")
public class EmgController {
    private final EmgSampleRepository emgSampleRepository;

    public EmgController(EmgSampleRepository emgSampleRepository) {
        this.emgSampleRepository = emgSampleRepository;
    }

    @PostMapping("/batch")
    public void insert(@RequestBody List<EmgSample> x) {
        emgSampleRepository.insertBatch(x);
    }

    @GetMapping("/prostheses/{prosthesisId}")
    public List<EmgSample> get(@PathVariable UUID prosthesisId, @RequestParam(required = false) String sensorId, @RequestParam(defaultValue = "1000") int limit) {
        return emgSampleRepository.findByProsthesis(prosthesisId, sensorId, limit);
    }
}
