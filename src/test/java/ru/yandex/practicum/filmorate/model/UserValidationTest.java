package ru.yandex.practicum.filmorate.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserValidationTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void testPassValidationValidUser() {
        User user = validUser();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    @Test
    void testFailValidationWhenEmailBlank() {
        User user = validUser();
        user.setEmail(" ");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testFailValidationWhenEmailInvalid() {
        User user = validUser();
        user.setEmail("not-an-email");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testFailValidationWhenLoginContainsSpaces() {
        User user = validUser();
        user.setLogin("log in");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void testFailValidationWhenBirthdayInFuture() {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
    }

    private User validUser() {
        User user = new User();
        user.setEmail("user@test.com");
        user.setLogin("user");
        user.setName("name");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}
