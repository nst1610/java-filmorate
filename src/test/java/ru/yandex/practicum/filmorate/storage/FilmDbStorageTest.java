package ru.yandex.practicum.filmorate.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, GenreDbStorage.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private User user;

    @BeforeEach
    void setUp() {
        user = userStorage.addUser(createUser("user@mail.test", "user"));
    }

    @Test
    void testReturnAllFilms() {
        Film firstFilm = filmStorage.addFilm(createFilm("First film", 1L, Set.of(genre(1L), genre(2L))));
        Film secondFilm = filmStorage.addFilm(createFilm("Second film", 2L, Set.of(genre(3L))));
        assertThat(filmStorage.getFilms())
            .extracting(Film::getId)
            .containsExactly(firstFilm.getId(), secondFilm.getId());
    }

    @Test
    void testFindFilmByIdWithMpaAndGenres() {
        Film savedFilm = filmStorage.addFilm(createFilm("Film", 1L, Set.of(genre(1L), genre(2L))));
        assertThat(filmStorage.getFilmById(savedFilm.getId()))
            .isPresent()
            .hasValueSatisfying(film -> {
                assertThat(film.getMpa().getId()).isEqualTo(1L);
                assertThat(film.getGenres()).extracting(Genre::getId).containsExactly(1L, 2L);
            });
    }

    @Test
    void testAddFilm() {
        Film savedFilm = filmStorage.addFilm(createFilm("Film", 1L, Set.of(genre(1L))));
        assertThat(savedFilm.getId()).isNotNull();
        assertThat(savedFilm.getLikes()).isEmpty();
    }

    @Test
    void testUpdateFilm() {
        Film film = filmStorage.addFilm(createFilm("Film", 1L, Set.of(genre(1L))));
        film.setName("Updated");
        film.setMpa(mpa(2L));
        film.setGenres(Set.of(genre(2L), genre(3L)));
        Film updatedFilm = filmStorage.updateFilm(film);
        assertThat(updatedFilm.getName()).isEqualTo("Updated");
        assertThat(updatedFilm.getMpa().getId()).isEqualTo(2L);
        assertThat(updatedFilm.getGenres()).extracting(Genre::getId).containsExactly(2L, 3L);
    }

    @Test
    void testAddAndRemoveLike() {
        Film film = filmStorage.addFilm(createFilm("Film", 1L, Set.of(genre(1L))));
        filmStorage.addLike(film.getId(), user.getId());
        assertThat(filmStorage.getFilmById(film.getId())).get()
            .satisfies(foundFilm -> assertThat(foundFilm.getLikes()).containsExactly(user.getId()));
        filmStorage.removeLike(film.getId(), user.getId());
        assertThat(filmStorage.getFilmById(film.getId())).get()
            .satisfies(foundFilm -> assertThat(foundFilm.getLikes()).isEmpty());
    }

    @Test
    void testReturnPopularFilms() {
        Film mostPopular = filmStorage.addFilm(createFilm("Most popular", 1L, Set.of(genre(1L))));
        Film lessPopular = filmStorage.addFilm(createFilm("Less popular", 1L, Set.of(genre(1L))));
        Film withoutLikes = filmStorage.addFilm(createFilm("Without likes", 1L, Set.of(genre(1L))));
        User secondUser = userStorage.addUser(createUser("second@mail.test", "second"));
        filmStorage.addLike(mostPopular.getId(), user.getId());
        filmStorage.addLike(mostPopular.getId(), secondUser.getId());
        filmStorage.addLike(lessPopular.getId(), user.getId());
        assertThat(filmStorage.getPopularFilms(2)).extracting(Film::getId)
            .containsExactly(mostPopular.getId(), lessPopular.getId())
            .doesNotContain(withoutLikes.getId());
    }

    private Film createFilm(String name, Long mpaId, Set<Genre> genres) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(120);
        film.setMpa(mpa(mpaId));
        film.setGenres(genres);
        return film;
    }

    private Genre genre(Long id) {
        Genre genre = new Genre();
        genre.setId(id);
        return genre;
    }

    private Mpa mpa(Long id) {
        Mpa mpa = new Mpa();
        mpa.setId(id);
        return mpa;
    }

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}
