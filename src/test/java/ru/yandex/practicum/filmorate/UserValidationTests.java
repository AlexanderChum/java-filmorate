package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validators.UserValidator;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserValidationTests {
    User user;

    @BeforeEach
    public void start() {
        user = new User();
        user.setLogin("Dolores");
        user.setName("Nick Name");
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.of(1946, 8, 20));
    }

    @Test
    void shouldNotCreateUserWithEmptySpacesInLogin() {
        user.setLogin("Dolo res");
        assertThrows(RuntimeException.class, () -> UserValidator.userValidation(user));
    }

    @Test
    void shouldNotCreateUserWithBlankOrNullLogin() {
        user.setLogin(null);
        assertThrows(RuntimeException.class, () -> UserValidator.userValidation(user));
    }

    @Test
    void shouldUseLoginIfNameIsEmpty() {
        user.setName(null);
        UserValidator.userValidation(user);
        assertEquals(user.getName(), user.getLogin());
    }

    @Test
    void bdayDateCantBeInFuture() {
        user.setBirthday(LocalDate.of(2100, 1, 1));
        assertThrows(RuntimeException.class, () -> UserValidator.userValidation(user));
    }

    @Test
    void shouldNotCreateUserWithBlankOrNullEmail() {
        user.setEmail(" ");
        assertThrows(RuntimeException.class, () -> UserValidator.userValidation(user));
    }

    @Test
    void shouldNotCreateUserWithWrongEmailFormat() {
        user.setEmail("mail#mail.ru");
        assertThrows(RuntimeException.class, () -> UserValidator.userValidation(user));
    }
}
