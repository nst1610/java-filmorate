package ru.yandex.practicum.filmorate.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {
    private final GenreDbStorage genreStorage;

    @Test
    void testReturnAllGenres() {
        assertThat(genreStorage.getGenres()).hasSize(6);
    }

    @Test
    void testReturnGenreById() {
        assertThat(genreStorage.getGenreById(1L))
            .isPresent()
            .get()
            .extracting(Genre::getName)
            .isEqualTo("Комедия");
    }

    @Test
    void testReturnGenresByIds() {
        assertThat(genreStorage.getGenresByIds(List.of(1L, 3L)))
            .extracting(Genre::getId)
            .containsExactly(1L, 3L);
    }
}
