package ru.yandex.practicum.filmorate.storage.userStorage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {
    private static final String GET_USERS = "SELECT * FROM users";
    private static final String GET_USER_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String DELETE_USER_BY_ID = "DELETE FROM users WHERE id = ?";
    private static final String INSERT_USER = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_BY_ID = "UPDATE users SET login = ?, name = ?, email = ?, birthday = ?" +
            " WHERE id = ?";

    private static final String ADD_FRIEND = "INSERT INTO friendships (user_id, friend_id, status)" +
            " VALUES(?, ?, 'pending')";
    private static final String DELETE_FRIEND = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
    private static final String GET_FRIENDS = "SELECT friend_id FROM friendships WHERE user_id = ?";
    private static final String GET_COMMON_FRIENDS = "SELECT DISTINCT f1.friend_id AS common_friend" +
            " FROM friendships f1 JOIN friendships f2 ON f1.friend_id = f2.friend_id" +
            " WHERE f1.user_id = ? and f2.user_id = ?";

    private static final String GET_STATUS = "SELECT COUNT(status) FROM friendships WHERE user_id = ? AND friend_id =?";
    private static final String CHANGE_STATUS_APPROVED = "UPDATE friendships SET status = 'approved'" +
            " WHERE user_id = ? AND friend_id = ?";
    private static final String CHANGE_STATUS_PENDING = "UPDATE friendships SET status = 'pending'" +
            " WHERE user_id = ? AND friend_id = ?";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User save(User user) {
        log.info("Попытка добавить пользователя в базу данных");
        return insertWithId(INSERT_USER, GET_USER_BY_ID,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()).get();
    }

    @Override
    public Optional<User> getById(Long id) {
        log.info("Попытка найти пользователя в базе данных");
        return findOne(GET_USER_BY_ID, id);
    }

    @Override
    public List<User> getAll() {
        log.info("Попытка найти нескольких пользователей в базе данных");
        return findMany(GET_USERS);
    }

    @Override
    public void delete(Long id) {
        log.info("Попытка удалить пользователя из базы данных");
        delete(DELETE_USER_BY_ID, id);
    }

    @Override
    public void updateById(Long id, User user) {
        log.info("Попытка обновить пользователя в базе данных");
        update(UPDATE_BY_ID,
                user.getLogin(),
                user.getName(),
                user.getEmail(),
                user.getBirthday(),
                id);
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Попытка добавить друга в базе данных");
        insertWithoutCreatingId(ADD_FRIEND, userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        log.info("Попытка удалить друга в базе данных");
        delete(DELETE_FRIEND, userId, friendId);
    }

    public Set<Long> getFriendsSet(Long userId) {
        log.info("Попытка получить список друзей пользователя");
        return new HashSet<>(jdbc.queryForList(GET_FRIENDS, Long.class, userId));
    }

    public Set<Long> getCommonFriends(Long userId, Long friendId) {
        log.info("Попытка получить список общих друзей 2 пользователей");
        return new HashSet<>(jdbc.queryForList(GET_COMMON_FRIENDS, Long.class, userId, friendId));
    }

    public int statusCount(Long userId, Long friendId) {
        log.info("Проверка существования 'дружбы' между пользователями");
        return jdbc.queryForObject(GET_STATUS, Integer.class, userId, friendId);
    }

    public void applyStatusApproved(Long userId, Long friendId) {
        log.info("Попытка установить статус дружбы ПОДТВЕРЖДЕНО");
        update(CHANGE_STATUS_APPROVED, userId, friendId);
    }

    public void applyStatusPending(Long userId, Long friendId) {
        log.info("Попытка установить статус дружбы ОТПРАВЛЕНА ЗАЯВКА");
        update(CHANGE_STATUS_PENDING, friendId, userId);
    }
}