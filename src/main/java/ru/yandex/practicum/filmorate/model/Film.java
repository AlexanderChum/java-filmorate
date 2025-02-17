package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.validators.ReleaseDate;

import java.time.LocalDate;


@Data
public class Film {
    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 200, message = "Слишком длинное описание")
    private String description;

    @NotNull(message = "Дата выпуска фильма не может быть пустой")
    @ReleaseDate
    private LocalDate releaseDate;

    @Positive(message = "Длина фильма не может быть меньше 0")
    private Long duration;
}