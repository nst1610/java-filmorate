package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import ru.yandex.practicum.filmorate.model.Genre;

public interface GenreStorage {
    Collection<Genre> getGenres();

    Optional<Genre> getGenreById(Long id);

    Collection<Genre> getGenresByIds(Collection<Long> ids);

    Map<Long, Set<Genre>> getGenresForFilms(List<Long> filmIds);
}
