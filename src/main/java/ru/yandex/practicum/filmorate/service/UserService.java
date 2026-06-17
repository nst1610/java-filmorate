package ru.yandex.practicum.filmorate.service;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InvalidUserDataException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getUsers() {
        log.info("Request to get all users");
        return userStorage.getUsers();
    }

    public User getUserById(Long id) {
        log.info("Request to get user by id {}", id);
        return findUserById(id);
    }

    public User addUser(User user) {
        normalizeName(user);
        log.info("Request to add user with login {}", user.getLogin());
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            log.warn("User update failed: id is empty");
            throw new InvalidUserDataException("User id is empty.");
        }
        findUserById(user.getId());
        normalizeName(user);
        log.info("Request to update user with id {}", user.getId());
        return userStorage.updateUser(user);
    }

    public void addFriend(Long userId, Long friendId) {
        validateDifferentUsers(userId, friendId);
        findUserById(userId);
        findUserById(friendId);
        userStorage.addFriend(userId, friendId);
        log.info("User {} added user {} to friends", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        validateDifferentUsers(userId, friendId);
        findUserById(userId);
        findUserById(friendId);
        userStorage.removeFriend(userId, friendId);
        log.info("User {} removed user {} from friends", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        findUserById(userId);
        log.info("Request to get friends for user {}", userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        validateDifferentUsers(userId, otherId);
        findUserById(userId);
        findUserById(otherId);
        log.info("Request to get common friends for users {} and {}", userId, otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }

    private User findUserById(Long id) {
        return userStorage.getUserById(id)
            .orElseThrow(() -> new NoSuchElementException("User with id " + id + " does not exist."));
    }

    private void validateDifferentUsers(Long userId, Long otherUserId) {
        if (userId.equals(otherUserId)) {
            log.warn("User operation failed: ids must be different.");
            throw new InvalidUserDataException("Users must be different.");
        }
    }

    private void normalizeName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
