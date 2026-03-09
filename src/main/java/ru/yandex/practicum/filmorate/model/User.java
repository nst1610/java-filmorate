package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import lombok.Data;

@Data
public class User {
    private Long id;

    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Неверный формат электронной почты")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелы")
    private String login;

    private String name;

    @NotNull(message = "Дата рождения не может быть пустой")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
}
