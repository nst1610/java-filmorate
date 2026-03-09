package ru.yandex.practicum.filmorate.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InvalidUserDataException;
import ru.yandex.practicum.filmorate.model.User;

@Service
public class UserService {
    private final Map<Long, User> users = new HashMap<>();
    private Long currentId = 1L;

    public Collection<User> getUsers() {
        return users.values();
    }

    public User addUser(User user) {
        setCorrectId(user);
        users.put(user.getId(), user);
        return user;
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new InvalidUserDataException("User id is empty.");
        }
        if (!users.containsKey(user.getId())) {
            throw new NoSuchElementException("User with id " + user.getId() + " does not exist.");
        }
        users.put(user.getId(), user);
        return user;
    }

    private void setCorrectId(User user) {
        if (user.getId() == null) {
            user.setId(currentId++);
        } else if (users.containsKey(user.getId())) {
            throw new InvalidUserDataException("User id " + user.getId() + " already exists.");
        } else if (user.getId() < currentId) {
            throw new InvalidUserDataException(
                "User id cannot be less or equals than the current maximum value " + (currentId - 1)
            );
        } else if (user.getId() > currentId) {
            currentId = user.getId();
        }
    }
}
