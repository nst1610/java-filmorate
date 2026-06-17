package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import ru.yandex.practicum.filmorate.model.Film;

public interface FilmStorage {
    Collection<Film> getFilms();

    Optional<Film> getFilmById(Long id);

    List<Film> getPopularFilms(int count);

    Film addFilm(Film film);

    Film updateFilm(Film film);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);
}
