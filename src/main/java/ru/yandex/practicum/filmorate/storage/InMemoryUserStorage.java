package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InvalidUserDataException;
import ru.yandex.practicum.filmorate.model.User;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long currentId = 1L;

    @Override
    public Collection<User> getUsers() {
        log.info("Get all users, size={}", users.size());
        return users.values();
    }

    @Override
    public User addUser(User user) {
        setCorrectId(user);
        setNameIfBlank(user);
        users.put(user.getId(), user);
        log.info("User added: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (user.getId() == null) {
            log.warn("User update failed: id is empty");
            throw new InvalidUserDataException("User id is empty.");
        }
        if (!users.containsKey(user.getId())) {
            log.warn("User update failed: user with id {} does not exist", user.getId());
            throw new NoSuchElementException("User with id " + user.getId() + " does not exist.");
        }
        setNameIfBlank(user);
        users.put(user.getId(), user);
        log.info("User updated: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    private void setCorrectId(User user) {
        if (user.getId() == null) {
            user.setId(currentId++);
        } else if (users.containsKey(user.getId())) {
            log.warn("User add failed: user id {} already exists", user.getId());
            throw new InvalidUserDataException("User id " + user.getId() + " already exists.");
        } else if (user.getId() < currentId) {
            log.warn(
                "User add failed: user id {} less than current max id {}",
                user.getId(),
                currentId - 1
            );
            throw new InvalidUserDataException(
                "User id cannot be less or equals than the current maximum value " + (currentId - 1)
            );
        } else if (user.getId() > currentId) {
            currentId = user.getId();
        }
    }

    private void setNameIfBlank(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
