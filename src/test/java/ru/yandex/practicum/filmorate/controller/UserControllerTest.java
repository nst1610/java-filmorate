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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserController userController;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testReturnAllUsers() throws Exception {
        User user = createUser(1L);
        when(userController.getUsers()).thenReturn(List.of(user));
        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].email").value("user@test.com"));
    }

    @Test
    void testCreateUserWhenBodyValid() throws Exception {
        User user = createUser(1L);
        when(userController.addUser(any(User.class))).thenReturn(user);
        mockMvc.perform(post("/users")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUser(1L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("user@test.com"));
        verify(userController).addUser(any(User.class));
    }

    @Test
    void testReturnBadRequestWhenBodyEmptyObject() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
        verify(userController, never()).addUser(any(User.class));
    }

    @Test
    void testReturnBadRequestWhenBodyCompletelyEmpty() throws Exception {
        mockMvc.perform(post("/users")
                .contentType(APPLICATION_JSON)
                .content(""))
            .andExpect(status().isBadRequest());
        verify(userController, never()).addUser(any(User.class));
    }

    private User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("user@test.com");
        user.setLogin("user");
        user.setName("name");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}
