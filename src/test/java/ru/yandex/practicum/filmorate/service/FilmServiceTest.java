package ru.yandex.practicum.filmorate.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

class FilmServiceTest {
    private FilmService filmService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        filmService = new FilmService(filmStorage, userStorage, new StubGenreStorage(), new StubMpaStorage());
        userService = new UserService(userStorage);
    }

    @Test
    void testAddLikeOnlyOncePerUser() {
        Film film = filmService.addFilm(createFilm("Film one"));
        User user = userService.addUser(createUser("user@mail.test", "user"));
        filmService.addLike(film.getId(), user.getId());
        filmService.addLike(film.getId(), user.getId());
        assertThat(filmService.getPopularFilms(10).getFirst().getLikes())
            .containsExactly(user.getId());
    }

    @Test
    void testRemoveLike() {
        Film film = filmService.addFilm(createFilm("Film one"));
        User user = userService.addUser(createUser("user@mail.test", "user"));
        filmService.addLike(film.getId(), user.getId());
        filmService.removeLike(film.getId(), user.getId());
        assertThat(filmService.getPopularFilms(10).getFirst().getLikes()).isEmpty();
    }

    @Test
    void testReturnMostPopularFilmsSortedByLikes() {
        Film mostPopular = filmService.addFilm(createFilm("Most popular"));
        Film lessPopular = filmService.addFilm(createFilm("Less popular"));
        Film withoutLikes = filmService.addFilm(createFilm("Without likes"));
        User firstUser = userService.addUser(createUser("first@mail.test", "first"));
        User secondUser = userService.addUser(createUser("second@mail.test", "second"));
        filmService.addLike(mostPopular.getId(), firstUser.getId());
        filmService.addLike(mostPopular.getId(), secondUser.getId());
        filmService.addLike(lessPopular.getId(), firstUser.getId());
        List<Film> popularFilms = filmService.getPopularFilms(2);
        assertThat(popularFilms).extracting(Film::getId)
            .containsExactly(mostPopular.getId(), lessPopular.getId());
        assertThat(popularFilms).doesNotContain(withoutLikes);
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(120);
        film.setMpa(createMpa(1L));
        film.setGenres(java.util.Set.of(createGenre(1L)));
        return film;
    }

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    private Genre createGenre(Long id) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName("Genre");
        return genre;
    }

    private Mpa createMpa(Long id) {
        Mpa mpa = new Mpa();
        mpa.setId(id.intValue());
        mpa.setName("PG");
        return mpa;
    }

    private static class StubGenreStorage implements GenreStorage {
        @Override
        public Collection<Genre> getGenres() {
            return List.of();
        }

        @Override
        public Optional<Genre> getGenreById(Long id) {
            Genre genre = new Genre();
            genre.setId(id);
            genre.setName("Genre");
            return Optional.of(genre);
        }
    }

    private static class StubMpaStorage implements MpaStorage {
        @Override
        public Collection<Mpa> getMpaRatings() {
            return List.of();
        }

        @Override
        public Optional<Mpa> getMpaById(Long id) {
            Mpa mpa = new Mpa();
            mpa.setId(id.intValue());
            mpa.setName("PG");
            return Optional.of(mpa);
        }
    }
}
