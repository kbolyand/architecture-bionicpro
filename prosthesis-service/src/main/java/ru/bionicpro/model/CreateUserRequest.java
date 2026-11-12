package ru.bionicpro.model;

import java.time.LocalDate;

public record CreateUserRequest(String firstName, String lastName, LocalDate dateOfBirth) {
}
