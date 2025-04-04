package ru.yandex.practicum.filmorate.storageTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.userStorage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTest {
    private final JdbcTemplate jdbc;
    private final UserDbStorage userStorage;

    private User user1 = createTestUser(1L);
    private User user2 = createTestUser(2L);
    private User user3 = createTestUser(3L);
    private User userToUpdate = createTestUser(50L);

    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM friendships");
        jdbc.update("DELETE FROM users");
        jdbc.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
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
    void testSaveUser() {
        User savedUser = userStorage.save(user1);

        assertThat(savedUser)
                .hasFieldOrPropertyWithValue("email", "1@ya.ru")
                .hasFieldOrPropertyWithValue("login", "user1");

        //assertThat(userStorage.getOrCheckById(savedUser.getId())).contains(savedUser);
    }

    @Test
    void testGetOrCheckById() {
        User saved = userStorage.save(user1);
        User result = userStorage.getOrCheckById(saved.getId());

        assertEquals(user1.getLogin(), result.getLogin());
        assertEquals(user1.getEmail(), result.getEmail());
    }

    @Test
    void testGetAllUsers() {
        user2.setLogin("login2");
        userStorage.save(user1);
        userStorage.save(user2);

        List<User> result = userStorage.getAll();

        assertThat(result)
                .extracting("login")
                .containsExactly("user1", "login2");
    }

    @Test
    void testDeleteUser() {
        User saved = userStorage.save(user1);
        userStorage.delete(saved.getId());

        //assertThat(userStorage.getOrCheckById(saved.getId())).isEmpty();
    }

    @Test
    void testUpdateUser() {
        User saved = userStorage.save(user1);
        userToUpdate.setId(saved.getId());
        userStorage.updateById(saved.getId(), userToUpdate);

        //assertThat(userStorage.getOrCheckById(saved.getId())).contains(userToUpdate);
    }

    @Test
    void testAddAndDeleteFriend() {
        User u1 = userStorage.save(user1);
        User u2 = userStorage.save(user2);

        userStorage.addFriend(u1.getId(), u2.getId());
        assertThat(userStorage.getFriendsSet(u1.getId())).containsExactly(u2.getId());

        userStorage.deleteFriend(u1.getId(), u2.getId());
        assertThat(userStorage.getFriendsSet(u1.getId())).isEmpty();
    }

    @Test
    void testGetCommonFriends() {
        User u1 = userStorage.save(user1);
        User u2 = userStorage.save(user2);
        User u3 = userStorage.save(user3);

        userStorage.addFriend(u1.getId(), u3.getId());
        userStorage.addFriend(u2.getId(), u3.getId());

        Set<Long> commonFriends = userStorage.getCommonFriends(u1.getId(), u2.getId());
        assertThat(commonFriends).containsExactly(u3.getId());
    }

    @Test
    void testFriendshipStatuses() {
        User u1 = userStorage.save(user1);
        User u2 = userStorage.save(user2);

        userStorage.addFriend(u1.getId(), u2.getId());
        assertThat(userStorage.statusCount(u1.getId(), u2.getId())).isEqualTo(1);

        userStorage.applyStatusApproved(u1.getId(), u2.getId());
        assertThat(userStorage.statusCount(u1.getId(), u2.getId())).isEqualTo(1);

        userStorage.applyStatusPending(u2.getId(), u1.getId());
        assertThat(userStorage.statusCount(u2.getId(), u1.getId())).isEqualTo(0);
        //последний тест должен не найти связь в обратную сторону, поэтому 0
    }
}