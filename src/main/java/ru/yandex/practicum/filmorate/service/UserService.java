package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public List<User> getAllUsers() {
        return userStorage.getAll();
    }

    public User createUser(User user) {
        log.info("Поступил запрос на создание нового пользователя");
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Был пользователь с пустым именем");
        }
        userStorage.save(user);
        log.info("Создание нового пользователя завершено");
        return user;
    }

    public User updateUser(User newUser) {
        log.info("Поступил запрос на обновление пользователя");
        if (newUser.getId() == null) {
            throw new ValidationException("Введен неверный id");
        }
        if (userStorage.getById(newUser.getId()).isEmpty()) {
            throw new UserNotFoundException("Пользователь с указанным id не существует");
        }

        userStorage.save(newUser);
        log.info("Пользователь с нужным id найден и обновлен");
        return newUser;
    }

    public List<User> addFriend(Long userId, Long friendId) {
        Optional<User> user1 = userStorage.getById(userId);
        log.trace("Проверка первого пользователя на null");
        userExistValidation(user1);

        Optional<User> user2 = userStorage.getById(friendId);
        log.trace("Проверка второго пользователя на null");
        userExistValidation(user2);

        Set<Long> set1 = user1.get().getFriendSet();
        set1.add(friendId);

        Set<Long> set2 = user2.get().getFriendSet();
        set2.add(userId);

        log.info("Друг был добавлен");
        return List.of(user1.get(), user2.get());
    }

    public List<User> deleteFriend(Long userId, Long friendId) {
        Optional<User> user1 = userStorage.getById(userId);
        log.trace("Проверка первого пользователя на null");
        userExistValidation(user1);

        Optional<User> user2 = userStorage.getById(friendId);
        log.trace("Проверка второго пользователя на null");
        userExistValidation(user2);

        Set<Long> set1 = user1.get().getFriendSet();
        set1.remove(friendId);

        Set<Long> set2 = user2.get().getFriendSet();
        set2.remove(userId);

        log.info("Друг был удален");
        return List.of(user1.get(), user2.get());
    }

    public List<User> showAllFriends(Long userId) {
        return userStorage.getById(userId)
                .map(user ->
                        user.getFriendSet().stream()
                                .map(userStorage::getById)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList())
                )
                .orElseThrow(() -> new UserNotFoundException("Такого пользователя не существует"));
    }

    public List<User> showAllCommonFriends(Long userId, Long friendId) {
        Optional<User> user1 = userStorage.getById(userId);
        log.trace("Проверка первого пользователя на null");
        userExistValidation(user1);

        Optional<User> user2 = userStorage.getById(friendId);
        log.trace("Проверка второго пользователя на null");
        userExistValidation(user2);

        Set<Long> commonFriendIds = new HashSet<>(user1
                .map(User::getFriendSet)
                .orElse(Collections.emptySet())
        );

        commonFriendIds.retainAll(user2
                .map(User::getFriendSet)
                .orElse(Collections.emptySet())
        );
        log.trace("Список id общих друзей подготовлен");

        return commonFriendIds.stream()
                .map(userStorage::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private void userExistValidation(Optional<User> user) {
        if (user.isEmpty()) {
            log.error("Ошибка при поиске пользователя");
            throw new UserNotFoundException("Такого пользователя не существует");
        }
    }
}
