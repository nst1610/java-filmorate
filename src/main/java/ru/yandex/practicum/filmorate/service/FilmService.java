package ru.yandex.practicum.filmorate.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InvalidFilmDataException;
import ru.yandex.practicum.filmorate.model.Film;

@Service
@Slf4j
public class FilmService {
    private final Map<Long, Film> films = new HashMap<>();
    private Long currentId = 1L;

    public Collection<Film> getFilms() {
        log.info("Get all films, size={}", films.size());
        return films.values();
    }

    public Film addFilm(Film film) {
        setCorrectId(film);
        films.put(film.getId(), film);
        log.info("Film added: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            log.warn("Film update failed: id is empty");
            throw new InvalidFilmDataException("Film id is empty.");
        }
        if (!films.containsKey(film.getId())) {
            log.warn("Film update failed: film with id {} does not exist", film.getId());
            throw new NoSuchElementException("Film with id " + film.getId() + " does not exist.");
        }
        films.put(film.getId(), film);
        log.info("Film updated: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    private void setCorrectId(Film film) {
        if (film.getId() == null) {
            film.setId(currentId++);
        } else if (films.containsKey(film.getId())) {
            log.warn("Film add failed: film id {} already exists", film.getId());
            throw new InvalidFilmDataException("Film id " + film.getId() + " already exists.");
        } else if (film.getId() < currentId) {
            log.warn(
                "Film add failed: film id {} less than current max id {}",
                film.getId(),
                currentId - 1
            );
            throw new InvalidFilmDataException(
                "Film id cannot be less or equals than the current maximum value " + (currentId - 1)
            );
        } else if (film.getId() > currentId) {
            currentId = film.getId();
        }
    }
}
