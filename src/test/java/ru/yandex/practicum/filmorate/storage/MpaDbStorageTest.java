package ru.yandex.practicum.filmorate.storage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.db.MpaDbStorage;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {
    private final MpaDbStorage mpaStorage;

    @Test
    void testReturnAllRatings() {
        assertThat(mpaStorage.getMpaRatings()).hasSize(5);
    }

    @Test
    void testReturnRatingById() {
        assertThat(mpaStorage.getMpaById(1L))
            .isPresent()
            .get()
            .extracting(Mpa::getName)
            .isEqualTo("G");
    }
}
