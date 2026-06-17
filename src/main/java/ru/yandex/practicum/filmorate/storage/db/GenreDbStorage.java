package ru.yandex.practicum.filmorate.storage.db;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.db.mapper.GenreRowMapper;

@Repository
@Qualifier("genreDbStorage")
public class GenreDbStorage extends BaseRepository<Genre> implements GenreStorage {
    private static final String FIND_ALL_GENRES_QUERY = "SELECT * FROM genres ORDER BY id";
    private static final String FIND_GENRE_BY_ID_QUERY = "SELECT * FROM genres WHERE id = ?";
    private static final String FIND_GENRES_BY_IDS_QUERY = """
        SELECT *
        FROM genres
        WHERE id IN (:ids)
        ORDER BY id
        """;
    private static final String FIND_GENRES_FOR_FILMS_QUERY = """
        SELECT fg.film_id, g.id, g.name
        FROM film_genres fg
        JOIN genres g ON g.id = fg.genre_id
        WHERE fg.film_id IN (:ids)
        ORDER BY fg.film_id, g.id
        """;

    private final NamedParameterJdbcTemplate namedJdbc;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new GenreRowMapper());
        this.namedJdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public Collection<Genre> getGenres() {
        return findMany(FIND_ALL_GENRES_QUERY);
    }

    @Override
    public Optional<Genre> getGenreById(Long id) {
        return findOne(FIND_GENRE_BY_ID_QUERY, id);
    }

    @Override
    public Collection<Genre> getGenresByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
        return namedJdbc.query(FIND_GENRES_BY_IDS_QUERY, parameters, mapper);
    }

    @Override
    public Map<Long, Set<Genre>> getGenresForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Map.of();
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource("ids", filmIds);
        Map<Long, Set<Genre>> result = new HashMap<>();
        namedJdbc.query(FIND_GENRES_FOR_FILMS_QUERY, parameters, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = mapper.mapRow(rs, 0);
            result.computeIfAbsent(filmId, id -> new LinkedHashSet<>()).add(genre);
        });
        return result;
    }
}
