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

class UserServiceTests {
    private static UserStorage userStorage;
    private static UserService userService;
    private User user1 = createTestUser(null);
    private User user2 = createTestUser(null);
    private User user3 = createTestUser(null);
    private User nonExistingUser = createTestUser(999L);
    private User commonFriend = createTestUser(null);

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
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

        assertEquals(4, users.size());
        assertEquals(user1, users.get(0));
        assertEquals(user2, users.get(1));
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
    void updateUserWhenUserNotExistsShouldThrowException() {
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(nonExistingUser));
    }

    @Test
    void addFriendShouldAddFriendsToBothUsers() {
        userService.addFriend(user1.getId(), user2.getId());

        Optional<User> updatedUser1 = userStorage.getById(user1.getId());
        Optional<User> updatedUser2 = userStorage.getById(user2.getId());

        assertTrue(updatedUser1.get().getFriendSet().contains(user2.getId()));
        assertTrue(updatedUser2.get().getFriendSet().contains(user1.getId()));
    }

    @Test
    void deleteFriendShouldRemoveFriendsForBothUsers() {
        userService.addFriend(user1.getId(), user2.getId());

        userService.deleteFriend(user1.getId(), user2.getId());

        Optional<User> updatedUser1 = userStorage.getById(user1.getId());
        Optional<User> updatedUser2 = userStorage.getById(user2.getId());

        assertFalse(updatedUser1.get().getFriendSet().contains(user2.getId()));
        assertFalse(updatedUser2.get().getFriendSet().contains(user1.getId()));
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