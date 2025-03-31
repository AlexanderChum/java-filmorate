package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.userStorage.UserDbStorage;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDbStorage userDbStorage;

    public List<User> getAllUsers() {
        log.info("Поступил запрос на получение списка пользователей");
        return userDbStorage.getAll();
    }

    public User createUser(User user) {
        log.info("Поступил запрос на создание нового пользователя");
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Пользователь с пустым именем");
        }
        return userDbStorage.save(user);
    }

    public User updateUser(User newUser) {
        log.info("Поступил запрос на обновление пользователя");
        if (newUser.getId() == null) {
            throw new ValidationException("Введен неверный id");
        }

        userExistence(newUser.getId());

        userDbStorage.updateById(newUser.getId(), newUser);
        log.info("Пользователь с нужным id найден и обновлен");
        return userDbStorage.getById(newUser.getId()).get();
    }

    public User addFriend(Long userId, Long friendId) {
        log.info("Проверка первого пользователя на null");
        userExistence(userId);

        log.info("Проверка второго пользователя на null");
        userExistence(friendId);

        userDbStorage.addFriend(userId, friendId);
        friendshipStatusCheck(userId, friendId);

        log.info("Друг был добавлен");

        User user = userDbStorage.getById(userId).get();
        user.setFriendSet(userDbStorage.getFriendsSet(userId));
        return user;
    }

    public User deleteFriend(Long userId, Long friendId) {
        log.info("Проверка первого пользователя на null");
        userExistence(userId);

        log.info("Проверка второго пользователя на null");
        userExistence(friendId);

        User user = userDbStorage.getById(userId).get();

        if (!userDbStorage.getFriendsSet(userId).contains(friendId)) { //проверка на случай если некого удалять
            user.setFriendSet(userDbStorage.getFriendsSet(userId));
            return user;
        }

        userDbStorage.deleteFriend(userId, friendId);
        if (userDbStorage.statusCount(friendId, userId) > 0) { //проверка на случай если наш "друг" нас так и не добавил
            userDbStorage.applyStatusPending(userId, friendId);
        }

        log.info("Друг был удален");
        user.setFriendSet(userDbStorage.getFriendsSet(userId));
        return user;
    }

    private void friendshipStatusCheck(Long userId, Long friendId) {
        log.info("Проверка дружбы между пользователями для установления статуса");
        boolean isMutual = userDbStorage.statusCount(userId, friendId) > 0
                && userDbStorage.statusCount(friendId, userId) > 0;

        if (isMutual) {
            userDbStorage.applyStatusApproved(userId, friendId);
            userDbStorage.applyStatusApproved(friendId, userId);
        }
    }

    public List<User> showAllFriends(Long userId) {
        log.info("Поступил запрос на получение списка всех друзей пользователя");
        userExistence(userId);
        return userDbStorage.getFriendsSet(userId).stream()
                .map(id -> userDbStorage.getById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<User> showAllCommonFriends(Long userId, Long friendId) {
        log.info("Проверка первого пользователя на null");
        userExistence(userId);

        log.info("Проверка второго пользователя на null");
        userExistence(friendId);

        List<User> resultList = new ArrayList<>();
        for (Long id : userDbStorage.getCommonFriends(userId, friendId)) {
            resultList.add(userDbStorage.getById(id).get());
        }
        log.info("Список общих друзей 2 пользователей получен");
        return resultList;
    }

    private User userExistence(Long id) {
        return userDbStorage.getById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь с id = " + id + " не найден"));
    }
}
