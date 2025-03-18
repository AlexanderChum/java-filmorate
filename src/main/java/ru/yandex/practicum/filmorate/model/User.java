package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Long id;

    @NotBlank(message = "email не может быть пустым")
    @Email(message = "неверный формат email")
    private String email;

    @NotNull(message = "логин не может пустым")
    @Pattern(regexp = "\\S+", message = "логин не может содержать пробелы")
    private String login;

    private String name;

    @PastOrPresent(message = "День рождения не может быть в будущем")
    private LocalDate birthday;

    private Set<Long> friendSet = new HashSet<>();
}