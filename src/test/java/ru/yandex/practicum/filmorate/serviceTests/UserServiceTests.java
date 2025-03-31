package ru.yandex.practicum.filmorate.serviceTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exceptions.EntityNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.userStorage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
class UserServiceTests {
    @Autowired
    private JdbcTemplate jdbc;
    private UserDbStorage userStorage;
    private UserService userService;

    private User user1 = createTestUser(1L);
    private User user2 = createTestUser(2L);
    private User user3 = createTestUser(3L);
    private User nonExistingUser = createTestUser(999L);
    private User commonFriend = createTestUser(4L);

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM friendships");
        jdbc.update("DELETE FROM users");
        jdbc.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");

        userStorage = new UserDbStorage(jdbc, new UserRowMapper());
        userService = new UserService(userStorage);
        userStorage.save(user1);
        userStorage.save(user2);
        userStorage.save(user3);
        userStorage.save(commonFriend);
    }

    private User createTestUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail(id + "@ya.ru");
        user.setLogin("user" + id);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void getAllUsersShouldReturnAllUsers() {
        List<User> users = userService.getAllUsers();

        assertThat(users)
                .extracting(User::getLogin)
                .containsExactly("user1", "user2", "user3", "user4");
    }

    @Test
    void updateUserWhenUserExistsShouldUpdateUserData() {
        User updatedUser = createTestUser(1L);
        updatedUser.setEmail("new@mail.com");
        updatedUser.setLogin("new_login");

        User result = userService.updateUser(updatedUser);

        assertEquals("new@mail.com", result.getEmail());
        assertEquals("new_login", result.getLogin());
    }

    @Test
    void updateNonExistingUserShouldThrow() {
        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(nonExistingUser));
    }

    @Test
    void addAndRemoveFriendShouldWork() {
        userService.addFriend(user1.getId(), user2.getId());
        assertThat(userService.showAllFriends(user1.getId()))
                .extracting(User::getId)
                .containsExactly(user2.getId());

        userService.deleteFriend(user1.getId(), user2.getId());
        assertThat(userService.showAllFriends(user1.getId())).isEmpty();
    }

    @Test
    void showAllFriendsShouldReturnUserFriends() {
        userService.addFriend(user1.getId(), user2.getId());
        userService.addFriend(user1.getId(), user3.getId());

        List<User> friends = userService.showAllFriends(user1.getId());

        assertEquals(2, friends.size());
        assertTrue(friends.contains(user2));
        assertTrue(friends.contains(user3));
    }

    @Test
    void showCommonFriendsShouldReturnCommonFriends() {
        userService.addFriend(user1.getId(), commonFriend.getId());
        userService.addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends = userService.showAllCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertTrue(commonFriends.contains(commonFriend));
    }
}