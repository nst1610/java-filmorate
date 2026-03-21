package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InvalidFilmDataException;
import ru.yandex.practicum.filmorate.model.Film;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private Long currentId = 1L;

    @Override
    public Collection<Film> getFilms() {
        log.info("Get all films, size={}", films.size());
        return films.values();
    }

    @Override
    public Film getFilmById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            log.warn("Film with id {} does not exist", id);
            throw new NoSuchElementException("Film with id " + id + " does not exist.");
        }
        return film;
    }

    @Override
    public Film addFilm(Film film) {
        setCorrectId(film);
        initializeLikes(film);
        films.put(film.getId(), film);
        log.info("Film added: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (film.getId() == null) {
            log.warn("Film update failed: id is empty");
            throw new InvalidFilmDataException("Film id is empty.");
        }
        if (!films.containsKey(film.getId())) {
            log.warn("Film update failed: film with id {} does not exist", film.getId());
            throw new NoSuchElementException("Film with id " + film.getId() + " does not exist.");
        }
        Film existingFilm = films.get(film.getId());
        film.setLikes(new HashSet<>(existingFilm.getLikes()));
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
            currentId = film.getId() + 1;
        }
    }

    private void initializeLikes(Film film) {
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        } else {
            film.setLikes(new HashSet<>(film.getLikes()));
        }
    }
}
