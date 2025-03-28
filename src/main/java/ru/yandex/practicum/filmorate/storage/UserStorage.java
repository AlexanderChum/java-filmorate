package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User save(User user);

    Optional<User> getById(Long id);

    List<User> getAll();

    void delete(Long id);
}
