package ru.yandex.practicum.filmorate.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

@WebMvcTest(FilmController.class)
class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FilmController filmController;

    @Test
    void testReturnAllFilms() throws Exception {
        Film film = createFilm(1L);
        when(filmController.getFilms()).thenReturn(List.of(film));
        mockMvc.perform(get("/films"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Film"));
    }

    @Test
    void testCreateFilmWhenBodyValid() throws Exception {
        Film film = createFilm(1L);
        when(filmController.addFilm(any(Film.class))).thenReturn(film);
        mockMvc.perform(post("/films")
                .contentType(APPLICATION_JSON)
                .content("""
                    {
                      "name": "Film",
                      "description": "Film description",
                      "releaseDate": "2010-07-16",
                      "duration": 120
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Film"));
        verify(filmController).addFilm(any(Film.class));
    }

    @Test
    void testReturnBadRequestWhenBodyEmptyObject() throws Exception {
        mockMvc.perform(post("/films")
                .contentType(APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
        verify(filmController, never()).addFilm(any(Film.class));
    }

    @Test
    void testReturnBadRequestWhenBodyCompletelyEmpty() throws Exception {
        mockMvc.perform(post("/films")
                .contentType(APPLICATION_JSON)
                .content(""))
            .andExpect(status().isBadRequest());
        verify(filmController, never()).addFilm(any(Film.class));
    }

    private Film createFilm(Long id) {
        Film film = new Film();
        film.setId(id);
        film.setName("Film");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(120);
        return film;
    }
}
