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
import java.util.Optional;
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

    private User user1 = createTestUser(null);
    private User user2 = createTestUser(null);
    private User user3 = createTestUser(null);
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
                .hasFieldOrPropertyWithValue("email", "null@ya.ru")
                .hasFieldOrPropertyWithValue("login", "usernull");

        assertThat(userStorage.getById(savedUser.getId())).contains(savedUser);
    }

    @Test
    void testGetById() {
        User saved = userStorage.save(user1);
        Optional<User> result = userStorage.getById(saved.getId());

        assertEquals(user1.getLogin(), result.get().getLogin());
        assertEquals(user1.getEmail(), result.get().getEmail());
    }

    @Test
    void testGetAllUsers() {
        user2.setLogin("login2");
        userStorage.save(user1);
        userStorage.save(user2);

        List<User> result = userStorage.getAll();

        assertThat(result)
                .extracting("login")
                .containsExactly("usernull", "login2");
    }

    @Test
    void testDeleteUser() {
        User saved = userStorage.save(user1);
        userStorage.delete(saved.getId());

        assertThat(userStorage.getById(saved.getId())).isEmpty();
    }

    @Test
    void testUpdateUser() {
        User saved = userStorage.save(user1);
        userToUpdate.setId(saved.getId());
        userStorage.updateById(saved.getId(), userToUpdate);

        assertThat(userStorage.getById(saved.getId())).contains(userToUpdate);
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