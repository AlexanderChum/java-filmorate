package ru.yandex.practicum.filmorate.storageTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.mpaStorage.MPADbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MPADbStorage.class, MpaRowMapper.class})
class MPADbStorageTest {
    private final JdbcTemplate jdbc;
    private final MPADbStorage mpaStorage;

    private MPA mpa1 = createTestMpa(1L, "G");
    private MPA mpa2 = createTestMpa(2L, "PG");
    private MPA mpa3 = createTestMpa(null, "PG-13");

    @BeforeEach
    void setup() {
        jdbc.update("DELETE FROM mpa");
        jdbc.update("ALTER TABLE mpa ALTER COLUMN id RESTART WITH 1");
    }

    private MPA createTestMpa(Long id, String name) {
        MPA mpa = new MPA();
        mpa.setId(id);
        mpa.setName(name);
        return mpa;
    }

    @Test
    void testAddMpa() {
        MPA added = mpaStorage.addMpa(mpa3);

        assertThat(added)
                .hasFieldOrPropertyWithValue("name", "PG-13")
                .hasFieldOrProperty("id");
    }

    @Test
    void testGetMpaById() {
        mpaStorage.addMpa(mpa1);
        Optional<MPA> result = mpaStorage.getMpaById(1L);

        assertEquals(mpa1.getName(), result.get().getName());
    }

    @Test
    void testGetAllMpa() {
        mpaStorage.addMpa(mpa1);
        mpaStorage.addMpa(mpa2);
        mpaStorage.addMpa(createTestMpa(null, "R"));

        List<MPA> result = mpaStorage.getAllMpa();

        assertThat(result)
                .extracting("name")
                .containsExactly("G", "PG", "R");
    }
}
