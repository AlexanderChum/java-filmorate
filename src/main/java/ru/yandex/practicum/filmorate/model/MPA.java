package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MPA {
    private Long id;

    @NotBlank(message = "Название возрастного рейтинга не может быть пустым")
    private String name;
}
