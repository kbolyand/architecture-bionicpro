package ru.bionicpro.controller;

import org.springframework.web.bind.annotation.*;
import ru.bionicpro.model.CreateProsthesisRequest;
import ru.bionicpro.model.Prosthesis;
import ru.bionicpro.repository.ProsthesisRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ProsthesisController {
    private final ProsthesisRepository prosthesisRepository;

    public ProsthesisController(ProsthesisRepository prosthesisRepository) {
        this.prosthesisRepository = prosthesisRepository;
    }

    @PostMapping("/users/{userId}/prostheses")
    public Prosthesis create(@PathVariable UUID userId, @RequestBody CreateProsthesisRequest createProsthesisRequest) {
        return prosthesisRepository.create(userId, createProsthesisRequest);
    }

    @GetMapping("/prostheses/{id}")
    public Prosthesis get(@PathVariable UUID id) {
        return prosthesisRepository.findById(id);
    }

    @GetMapping("/users/{userId}/prostheses")
    public List<Prosthesis> byUser(@PathVariable UUID userId) {
        return prosthesisRepository.findByUserId(userId);
    }
}
