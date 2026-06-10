package ru.yandex.practicum.filmorate.service;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InvalidUserDataException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> getUsers() {
        log.info("Request to get all users");
        return userStorage.getUsers();
    }

    public User getUserById(Long id) {
        log.info("Request to get user by id {}", id);
        return findUserById(id);
    }

    public User addUser(User user) {
        log.info("Request to add user with login {}", user.getLogin());
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            log.warn("User update failed: id is empty");
            throw new InvalidUserDataException("User id is empty.");
        }
        findUserById(user.getId());
        log.info("Request to update user with id {}", user.getId());
        return userStorage.updateUser(user);
    }

    public void addFriend(Long userId, Long friendId) {
        validateDifferentUsers(userId, friendId);
        User user = findUserById(userId);
        User friend = findUserById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Users {} and {} are now friends", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        validateDifferentUsers(userId, friendId);
        User user = findUserById(userId);
        User friend = findUserById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Users {} and {} are no longer friends", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        User user = findUserById(userId);
        log.info("Request to get friends for user {}", userId);
        return user.getFriends().stream()
            .map(this::findUserById)
            .toList();
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        validateDifferentUsers(userId, otherId);
        User user = findUserById(userId);
        User otherUser = findUserById(otherId);
        log.info("Request to get common friends for users {} and {}", userId, otherId);
        return user.getFriends().stream()
            .filter(friendId -> otherUser.getFriends().contains(friendId))
            .map(this::findUserById)
            .toList();
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
}
