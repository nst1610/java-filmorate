package ru.yandex.practicum.filmorate.storage.db;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.db.mapper.FilmRowMapper;

@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private static final String FIND_ALL_FILMS_QUERY = """
        SELECT f.*, m.id AS mpa_id, m.name AS mpa_name
        FROM films f
        LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
        """;
    private static final String FIND_FILM_BY_ID_QUERY = """
        SELECT f.*, m.id AS mpa_id, m.name AS mpa_name
        FROM films f
        LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
        WHERE f.id = ?
        """;
    private static final String FIND_POPULAR_FILMS_QUERY = """
        SELECT f.*, m.id AS mpa_id, m.name AS mpa_name
        FROM films f
        LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
        LEFT JOIN film_likes fl ON f.id = fl.film_id
        GROUP BY f.id
        ORDER BY COUNT(fl.user_id) DESC, f.id
        LIMIT ?
        """;
    private static final String INSERT_FILM_QUERY = """
        INSERT INTO films(name, description, release_date, duration, mpa_id)
        VALUES (?, ?, ?, ?, ?)
        """;
    private static final String UPDATE_FILM_QUERY = """
        UPDATE films
        SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
        WHERE id = ?
        """;
    private static final String DELETE_FILM_GENRES_QUERY = """
        DELETE FROM film_genres
        WHERE film_id = ?
        """;
    private static final String INSERT_LIKE_QUERY = """
        MERGE INTO film_likes (film_id, user_id)
        KEY (film_id, user_id)
        VALUES (?, ?)
        """;
    private static final String DELETE_LIKE_QUERY = """
        DELETE FROM film_likes
        WHERE film_id = ? AND user_id = ?
        """;
    private static final String INSERT_FILM_GENRE_QUERY = """
        MERGE INTO film_genres (film_id, genre_id)
        KEY (film_id, genre_id)
        VALUES (?, ?)
        """;
    private static final String FIND_FILM_LIKES_BY_FILM_IDS_QUERY = """
        SELECT film_id, user_id
        FROM film_likes
        WHERE film_id IN (%s)
        ORDER BY film_id, user_id
        """;

    private final GenreStorage genreStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, @Qualifier("genreDbStorage") GenreStorage genreStorage) {
        super(jdbcTemplate, new FilmRowMapper());
        this.genreStorage = genreStorage;
    }

    @Override
    public Collection<Film> getFilms() {
        return enrichFilms(findMany(FIND_ALL_FILMS_QUERY));
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        return findOne(FIND_FILM_BY_ID_QUERY, id).map(this::enrichFilm);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return enrichFilms(findMany(FIND_POPULAR_FILMS_QUERY, count));
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(insert(
            INSERT_FILM_QUERY,
            film.getName(),
            film.getDescription(),
            Date.valueOf(film.getReleaseDate()),
            film.getDuration(),
            film.getMpa() == null ? null : film.getMpa().getId()
        ));
        saveGenres(film);
        return getFilmById(film.getId()).orElseThrow();
    }

    @Override
    public Film updateFilm(Film film) {
        update(
            UPDATE_FILM_QUERY,
            film.getName(),
            film.getDescription(),
            Date.valueOf(film.getReleaseDate()),
            film.getDuration(),
            film.getMpa() == null ? null : film.getMpa().getId(),
            film.getId()
        );
        delete(DELETE_FILM_GENRES_QUERY, film.getId());
        saveGenres(film);
        return getFilmById(film.getId()).orElseThrow();
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        update(INSERT_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        delete(DELETE_LIKE_QUERY, filmId, userId);
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        List<Genre> genres = new ArrayList<>(film.getGenres());
        jdbc.batchUpdate(
            INSERT_FILM_GENRE_QUERY,
            genres,
            genres.size(),
            (ps, genre) -> {
                ps.setLong(1, film.getId());
                ps.setLong(2, genre.getId());
            }
        );
    }

    private List<Film> enrichFilms(List<Film> films) {
        if (films.isEmpty()) {
            return films;
        }
        enrichFilmsWithGenres(films);
        enrichFilmsWithLikes(films);
        return films;
    }

    private Film enrichFilm(Film film) {
        return enrichFilms(new ArrayList<>(List.of(film))).getFirst();
    }

    private void enrichFilmsWithGenres(List<Film> films) {
        List<Long> filmIds = new ArrayList<>();
        for (Film film : films) {
            filmIds.add(film.getId());
        }
        Map<Long, Set<Genre>> genresByFilmId = genreStorage.getGenresForFilms(filmIds);
        for (Film film : films) {
            film.setGenres(new LinkedHashSet<>(genresByFilmId.getOrDefault(film.getId(), Set.of())));
        }
    }

    private void enrichFilmsWithLikes(List<Film> films) {
        List<Long> filmIds = new ArrayList<>();
        for (Film film : films) {
            filmIds.add(film.getId());
        }
        Map<Long, Set<Long>> likesByFilmId = loadLikesByFilmIds(filmIds);
        for (Film film : films) {
            film.setLikes(new HashSet<>(likesByFilmId.getOrDefault(film.getId(), Set.of())));
        }
    }

    private Map<Long, Set<Long>> loadLikesByFilmIds(List<Long> filmIds) {
        Map<Long, Set<Long>> likesByFilmId = new HashMap<>();
        if (filmIds.isEmpty()) {
            return likesByFilmId;
        }
        String query = FIND_FILM_LIKES_BY_FILM_IDS_QUERY.formatted(buildPlaceholders(filmIds.size()));
        jdbc.query(query, rs -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");
            likesByFilmId.computeIfAbsent(filmId, key -> new LinkedHashSet<>()).add(userId);
        }, filmIds.toArray());
        return likesByFilmId;
    }

    private String buildPlaceholders(int count) {
        return String.join(", ", Collections.nCopies(count, "?"));
    }
}
