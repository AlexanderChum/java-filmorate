package ru.yandex.practicum.filmorate.storage.userStorage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User save(User user);

    User getOrCheckById(Long id);

    List<User> getAll();

    void delete(Long id);

    User updateById(Long id, User user);
}
