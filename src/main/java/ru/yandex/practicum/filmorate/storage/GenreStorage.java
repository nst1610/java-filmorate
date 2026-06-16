package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;
import java.util.Optional;
import ru.yandex.practicum.filmorate.model.Genre;

public interface GenreStorage {
    Collection<Genre> getGenres();

    Optional<Genre> getGenreById(Long id);
}
