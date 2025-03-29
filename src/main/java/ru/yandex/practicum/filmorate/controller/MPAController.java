package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
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
    public List<MPA> getAllMpa() {
        log.info("Запрос на получение всех возрастных рейтингов");
        return mpaService.getAllMpa();
    }

    @GetMapping("/{id}")
    public MPA getMpaById(@PathVariable @Positive Long id) {
        log.info("Запрос на получение возрастного рейтинга по id");
        return mpaService.getMpaById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteMpaById(@PathVariable @Positive Long id) {
        log.info("Запрос на удаление возрастного рейтинга по id");
        mpaService.deleteMpaById(id);
    }

    @PostMapping
    public MPA addMpa(@Valid @RequestBody MPA mpa) {
        log.info("Запрос на добавление возрастного рейтинга");
        return mpaService.addMpa(mpa);
    }
}