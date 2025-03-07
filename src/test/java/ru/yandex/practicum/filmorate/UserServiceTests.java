package ru.yandex.practicum.filmorate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

class UserServiceTest {
    private static UserStorage userStorage;
    private static UserService userService;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
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
        User user1 = createTestUser(1L);
        User user2 = createTestUser(2L);
        userStorage.save(user1);
        userStorage.save(user2);

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
        assertEquals(user1, users.get(0));
        assertEquals(user2, users.get(1));
    }

    @Test
    void updateUserWhenUserExistsShouldUpdateUserData() {
        User existingUser = createTestUser(1L);
        userStorage.save(existingUser);

        User updatedUser = createTestUser(1L);
        updatedUser.setEmail("new@mail.com");
        updatedUser.setLogin("new_login");

        User result = userService.updateUser(updatedUser);

        assertEquals("new@mail.com", result.getEmail());
        assertEquals("new_login", result.getLogin());
    }

    @Test
    void updateUserWhenUserNotExistsShouldThrowException() {
        User nonExistingUser = createTestUser(999L);

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(nonExistingUser));
    }

    @Test
    void addFriendShouldAddFriendsToBothUsers() {
        User user1 = createTestUser(1L);
        User user2 = createTestUser(2L);
        userStorage.save(user1);
        userStorage.save(user2);

        userService.addFriend(user1.getId(), user2.getId());

        Optional<User> updatedUser1 = userStorage.getById(user1.getId());
        Optional<User> updatedUser2 = userStorage.getById(user2.getId());

        assertTrue(updatedUser1.get().getFriendSet().contains(user2.getId()));
        assertTrue(updatedUser2.get().getFriendSet().contains(user1.getId()));
    }

    @Test
    void deleteFriendShouldRemoveFriendsForBothUsers() {
        User user1 = createTestUser(1L);
        User user2 = createTestUser(2L);
        userStorage.save(user1);
        userStorage.save(user2);

        userService.addFriend(user1.getId(), user2.getId());

        userService.deleteFriend(user1.getId(), user2.getId());

        Optional<User> updatedUser1 = userStorage.getById(user1.getId());
        Optional<User> updatedUser2 = userStorage.getById(user2.getId());

        assertFalse(updatedUser1.get().getFriendSet().contains(user2.getId()));
        assertFalse(updatedUser2.get().getFriendSet().contains(user1.getId()));
    }

    @Test
    void showAllFriendsShouldReturnUserFriends() {
        User user = createTestUser(1L);
        User friend1 = createTestUser(2L);
        User friend2 = createTestUser(3L);
        userStorage.save(user);
        userStorage.save(friend1);
        userStorage.save(friend2);

        userService.addFriend(user.getId(), friend1.getId());
        userService.addFriend(user.getId(), friend2.getId());

        List<User> friends = userService.showAllFriends(1L);

        assertEquals(2, friends.size());
        assertTrue(friends.contains(friend1));
        assertTrue(friends.contains(friend2));
    }

    @Test
    void showCommonFriendsShouldReturnCommonFriends() {
        User user1 = createTestUser(1L);
        User user2 = createTestUser(2L);
        User commonFriend = createTestUser(3L);
        userStorage.save(user1);
        userStorage.save(user2);
        userStorage.save(commonFriend);

        userService.addFriend(user1.getId(), commonFriend.getId());
        userService.addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends = userService.showAllCommonFriends(1L, 2L);

        assertEquals(1, commonFriends.size());
        assertTrue(commonFriends.contains(commonFriend));
    }
}