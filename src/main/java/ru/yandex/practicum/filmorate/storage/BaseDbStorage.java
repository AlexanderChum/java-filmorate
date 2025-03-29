package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exceptions.DbDeletionException;
import ru.yandex.practicum.filmorate.exceptions.InternalDatabaseException;
import ru.yandex.practicum.filmorate.exceptions.InternalServerException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class BaseDbStorage<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;

    protected Optional<T> findOne(String query, Object... params) {
        try {
            T result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    protected List<T> findMany(String query, Object... params) {
        return jdbc.query(query, mapper, params);
    }

    protected void delete(String query, Object... params) {
        int rowsDeleted = jdbc.update(query, params);
        if (rowsDeleted < 1) throw new DbDeletionException("Не удалось удалить данные");
    }

    protected void update(String query, Object... params) throws InternalDatabaseException {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) throw new InternalDatabaseException("Не удалось обновить данные");
    }

    protected Optional<T> insertWithId(String insertQuery, String selectQuery, Object... params)
            throws InternalServerException {

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);
        return findOne(selectQuery, id);
    }

    protected void insertWithoutCreatingId(String query, Object... params) {
        int rowsAdded = jdbc.update(query, params);
        if (rowsAdded == 0) throw new InternalDatabaseException("Не удалось добавить данные");
    }
}
