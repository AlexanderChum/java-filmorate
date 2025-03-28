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
        log.info("Поступил запрос на получение списка пользователей");
        return userStorage.getAll();
    }

    public User createUser(User user) {
        log.info("Поступил запрос на создание нового пользователя");
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Пользователь с пустым именем");
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

        userExistence(newUser.getId());

        userStorage.save(newUser);
        log.info("Пользователь с нужным id найден и обновлен");
        return newUser;
    }

    public List<User> addFriend(Long userId, Long friendId) {
        log.info("Проверка первого пользователя на null");
        User user = userExistence(userId);

        log.info("Проверка второго пользователя на null");
        User friend = userExistence(friendId);

        Set<Long> setUser = user.getFriendSet();
        setUser.add(friendId);

        Set<Long> setFriend = friend.getFriendSet();
        setFriend.add(userId);

        log.info("Друг был добавлен");
        return List.of(user, friend);
    }

    public List<User> deleteFriend(Long userId, Long friendId) {
        log.info("Проверка первого пользователя на null");
        User user = userExistence(userId);

        log.info("Проверка второго пользователя на null");
        User friend = userExistence(friendId);

        Set<Long> setUser = user.getFriendSet();
        setUser.remove(friendId);

        Set<Long> setFriend = friend.getFriendSet();
        setFriend.remove(userId);

        log.info("Друг был удален");
        return List.of(user, friend);
    }

    public List<User> showAllFriends(Long userId) {
        log.info("Поступил запрос на получение списка всех друзей");
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
        log.info("Проверка первого пользователя на null");
        User user = userExistence(userId);

        log.info("Проверка второго пользователя на null");
        User userToCheck = userExistence(friendId);

        Set<Long> commonFriendIds = new HashSet<>(user.getFriendSet());
        commonFriendIds.retainAll(userToCheck.getFriendSet());
        log.info("Список id общих друзей подготовлен");

        return commonFriendIds.stream()
                .map(userStorage::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private User userExistence(Long id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id = " + id + " не найден"));
    }
}
