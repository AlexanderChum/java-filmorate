package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validators.FilmValidator;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmValidationsTests {
    Film film;

    @BeforeEach
    public void start() {
        film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100L);
    }

    @Test
    void durationShouldBePositive() {
        film.setDuration(0L);
        assertThrows(RuntimeException.class, () -> FilmValidator.filmValidation(film));
        film.setDuration(-50L);
        assertThrows(RuntimeException.class, () -> FilmValidator.filmValidation(film));
    }

    @Test
    void shouldNotCreateFilmIfEmptyIsNullOrBlank() {
        film.setName(null);
        assertThrows(RuntimeException.class, () -> FilmValidator.filmValidation(film));
        film.setName("");
        assertThrows(RuntimeException.class, () -> FilmValidator.filmValidation(film));
    }

    @Test
    void shouldNotBeReleasedBeforeFirstFilmInHistory() {
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        assertThrows(RuntimeException.class, () -> FilmValidator.filmValidation(film));
    }

    @Test
    void descriptionShouldBeShorterThan200Symbols() {
        film.setDescription("sndaiouvfbnasdibfbnasidufbasijhdbfn iasdbfiasbndfiujbnasdifu basdiufbais dfbasidj" +
                " fbnniasdbnf iasdbnf iasdbfi abnsdfib nasdfujnbasjdukfnbiasdb fisa bdfih basdifb nasojdfn iasdbfiha " +
                "sbdfihbasdf bnasjdfbniasdbfiausdbfihasebfikaswbef iabsefiabs e fhbasifeb saeuifbsueaenfausoejnfjase" +
                "rfbiasurbfgiouasbnf sadf ousad fubnasdif bsaidufb asudfb asudfb ausdbfou asdnf ojuasnbdfibna siufe" +
                "bnawiefbnaijuwresfgbnisrbfashnfekjanbsfiubhadsrgfi bnsadfhiabsdfi bhassweif basjfbaisdbfiabwenfb" +
                "dsfiubasdfibasdfihbaiwesfb asifb saif siuaebf hasbfhasbndfi ausbdfias befiu");
        assertThrows(RuntimeException.class, () -> FilmValidator.filmValidation(film));
    }
}
