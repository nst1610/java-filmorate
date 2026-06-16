package ru.yandex.practicum.filmorate.storage.db;

import java.util.Collection;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;
import ru.yandex.practicum.filmorate.storage.db.mapper.MpaRowMapper;

@Repository
@Qualifier("mpaDbStorage")
public class MpaDbStorage extends BaseRepository<Mpa> implements MpaStorage {
    private static final String FIND_ALL_MPA_QUERY = "SELECT * FROM mpa_ratings ORDER BY id";
    private static final String FIND_MPA_BY_ID_QUERY = "SELECT * FROM mpa_ratings WHERE id = ?";

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new MpaRowMapper());
    }

    @Override
    public Collection<Mpa> getMpaRatings() {
        return findMany(FIND_ALL_MPA_QUERY);
    }

    @Override
    public Optional<Mpa> getMpaById(Long id) {
        return findOne(FIND_MPA_BY_ID_QUERY, id);
    }
}
