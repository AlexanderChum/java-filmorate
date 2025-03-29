package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.mpaStorage.MPADbStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MPAService {
    private final MPADbStorage mpaDbStorage;

    public List<MPA> getAllMpa() {
        log.info("Поступил запрос на получение списка всех возрастных рейтнгов");
        return mpaDbStorage.getAllMpa();
    }

    public MPA getMpaById(Long id) {
        log.info("Поступил запрос на получение возрастного рейтинга по id");
        return mpaDbStorage.getMpaById(id)
                .orElseThrow(() -> new MpaNotFoundException("MPA не найден"));
    }

    public void deleteMpaById(Long id) {
        log.info("Поступил запрос на удаление возрастного рейтинга по id");
        mpaDbStorage.getMpaById(id);
        mpaDbStorage.deleteMpaById(id);
        log.info("Запрос обработан");
    }

    public MPA addMpa(MPA mpa) {
        log.info("Поступил запрос на добавление возрастного рейтинга");
        return mpaDbStorage.addMpa(mpa);
    }
}
