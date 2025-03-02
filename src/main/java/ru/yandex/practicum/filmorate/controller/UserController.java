package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public List<User> getAll() {
        log.info("Список пользователей возвращен");
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Поступил запрос на создание нового пользователя");
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Был пользователь с пустым именем");
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создание нового пользователя завершено");
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("Поступил запрос на обновление пользователя");
        if (user.getId() == null) {
            throw new ValidationException("Введен неверный id");
        }
        if (!users.containsKey(user.getId())) {
            throw new ValidationException("Пользователь с указанным id не существует");
        }
        users.put(user.getId(), user);
        log.info("Пользователь с нужным id найден и обновлен");
        return user;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
