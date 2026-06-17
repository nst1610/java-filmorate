package ru.yandex.practicum.filmorate.service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InvalidFilmDataException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

@Service
@Slf4j
public class FilmService {
    private static final int DEFAULT_POPULAR_FILMS_COUNT = 10;

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final MpaStorage mpaStorage;

    public FilmService(
        @Qualifier("filmDbStorage") FilmStorage filmStorage,
        @Qualifier("userDbStorage") UserStorage userStorage,
        @Qualifier("genreDbStorage") GenreStorage genreStorage,
        @Qualifier("mpaDbStorage") MpaStorage mpaStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.mpaStorage = mpaStorage;
    }

    public Collection<Film> getFilms() {
        log.info("Request to get all films");
        return filmStorage.getFilms();
    }

    public Film getFilmById(Long id) {
        log.info("Request to get film by id {}", id);
        return findFilmById(id);
    }

    public Film addFilm(Film film) {
        validateFilmData(film);
        log.info("Request to add film with name {}", film.getName());
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            log.warn("Film update failed: id is empty");
            throw new InvalidFilmDataException("Film id is empty.");
        }
        findFilmById(film.getId());
        validateFilmData(film);
        log.info("Request to update film with id {}", film.getId());
        return filmStorage.updateFilm(film);
    }

    public void addLike(Long filmId, Long userId) {
        findFilmById(filmId);
        findUserById(userId);
        filmStorage.addLike(filmId, userId);
        log.info("User {} added like to film {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        findFilmById(filmId);
        findUserById(userId);
        filmStorage.removeLike(filmId, userId);
        log.info("User {} removed like from film {}", userId, filmId);
    }

    public List<Film> getPopularFilms(Integer count) {
        int limit = count == null ? DEFAULT_POPULAR_FILMS_COUNT : count;
        if (limit <= 0) {
            log.warn("Popular films request failed: invalid count {}", limit);
            throw new InvalidFilmDataException("Count must be greater than zero.");
        }
        log.info("Request to get {} popular films", limit);
        return filmStorage.getPopularFilms(limit);
    }

    private Film findFilmById(Long id) {
        return filmStorage.getFilmById(id)
            .orElseThrow(() -> new NoSuchElementException("Film with id " + id + " does not exist."));
    }

    private void findUserById(Long id) {
        userStorage.getUserById(id)
            .orElseThrow(() -> new NoSuchElementException("User with id " + id + " does not exist."));
    }

    private void validateFilmData(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new InvalidFilmDataException("MPA rating is required.");
        }
        mpaStorage.getMpaById(film.getMpa().getId())
            .orElseThrow(() -> new NoSuchElementException(
                "MPA rating with id " + film.getMpa().getId() + " does not exist."
            ));
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        Set<Long> genreIds = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
        Collection<Genre> foundGenres = genreStorage.getGenresByIds(genreIds);
        if (foundGenres.size() != genreIds.size()) {
            throw new NoSuchElementException("One or more genres do not exist.");
        }
        film.setGenres(new LinkedHashSet<>(foundGenres));
    }
}
