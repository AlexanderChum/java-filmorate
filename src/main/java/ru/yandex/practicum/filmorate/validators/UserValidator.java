package ru.yandex.practicum.filmorate.validators;

import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class UserValidator {

    public static User userValidation(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Введен пустой email");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Введен не email");
        }
        if (user.getLogin() == null || user.getLogin().contains(" ")) {
            throw new ValidationException("Неверный формат логина");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Неверная дата рождения");
        }
        return user;
    }
}
