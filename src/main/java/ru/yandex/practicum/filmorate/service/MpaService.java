package ru.yandex.practicum.filmorate.service;

import java.util.Collection;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    public MpaService(@Qualifier("mpaDbStorage") MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Collection<Mpa> getMpaRatings() {
        return mpaStorage.getMpaRatings();
    }

    public Mpa getMpaById(Long id) {
        return mpaStorage.getMpaById(id)
            .orElseThrow(() -> new NoSuchElementException("MPA rating with id " + id + " does not exist."));
    }
}
