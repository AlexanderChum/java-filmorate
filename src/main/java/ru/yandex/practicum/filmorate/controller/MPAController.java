package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.MPAService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mpa")
public class MPAController {
    private final MPAService mpaService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MPA> getAllMpa() {
        log.info("Запрос на получение всех возрастных рейтингов");
        return mpaService.getAllMpa();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public MPA getMpaById(@PathVariable @Positive Long id) {
        log.info("Запрос на получение возрастного рейтинга по id");
        return mpaService.getMpaById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public MPA addMpa(@Valid @RequestBody MPA mpa) {
        log.info("Запрос на добавление возрастного рейтинга");
        return mpaService.addMpa(mpa);
    }
}