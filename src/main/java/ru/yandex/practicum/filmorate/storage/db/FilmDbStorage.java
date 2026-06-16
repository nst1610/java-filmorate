package ru.yandex.practicum.filmorate.storage.db;

import java.sql.Date;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.db.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.db.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

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
    private static final String FIND_FILM_GENRES_QUERY = """
        SELECT g.id, g.name
        FROM genres g
        JOIN film_genres fg ON g.id = fg.genre_id
        WHERE fg.film_id = ?
        """;
    private static final String FIND_FILM_LIKES_QUERY = """
        SELECT user_id
        FROM film_likes
        WHERE film_id = ?
        """;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new FilmRowMapper());
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
        if (film.getGenres() == null) {
            return;
        }
        for (Genre genre : film.getGenres()) {
            update(INSERT_FILM_GENRE_QUERY, film.getId(), genre.getId());
        }
    }

    private List<Film> enrichFilms(List<Film> films) {
        return films.stream().map(this::enrichFilm).toList();
    }

    private Film enrichFilm(Film film) {
        film.setGenres(new LinkedHashSet<>(loadGenres(film.getId())));
        film.setLikes(new java.util.HashSet<>(loadLikes(film.getId())));
        return film;
    }

    private List<Genre> loadGenres(Long filmId) {
        return jdbc.query(FIND_FILM_GENRES_QUERY, new GenreRowMapper(), filmId);
    }

    private List<Long> loadLikes(Long filmId) {
        return jdbc.queryForList(FIND_FILM_LIKES_QUERY, Long.class, filmId);
    }
}
