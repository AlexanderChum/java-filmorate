package ru.yandex.practicum.filmorate.storage.userStorage;


import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(getNextId());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> getById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }

    @Override
    public void updateById(Long id, User user) {
        user.setId(id);
        save(user);
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
