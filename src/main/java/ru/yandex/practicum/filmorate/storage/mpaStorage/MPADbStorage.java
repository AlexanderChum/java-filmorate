package ru.yandex.practicum.filmorate.storage.mpaStorage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class MPADbStorage extends BaseDbStorage<MPA> {
    private static final String ADD_MPA = "INSERT INTO mpa (name) VALUES(:name)";
    private static final String GET_MPA_BY_ID = "SELECT * FROM mpa WHERE id = ?";
    private static final String GET_MPA = "SELECT * FROM mpa ORDER BY id";

    public MPADbStorage(JdbcTemplate jdbc, RowMapper<MPA> mapper) {
        super(jdbc, mapper);
    }

    public MPA addMpa(MPA mpa) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", mpa.getName());
        return insertWithId(ADD_MPA, GET_MPA_BY_ID, params).get();
    }

    public Optional<MPA> getMpaById(Long id) {
        return findOne(GET_MPA_BY_ID, id);
    }

    public List<MPA> getAllMpa() {
        return findMany(GET_MPA);
    }
}