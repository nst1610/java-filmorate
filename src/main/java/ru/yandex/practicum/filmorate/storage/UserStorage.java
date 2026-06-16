package ru.yandex.practicum.filmorate.storage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import ru.yandex.practicum.filmorate.model.User;

public interface UserStorage {
    Collection<User> getUsers();

    Optional<User> getUserById(Long id);

    User addUser(User user);

    User updateUser(User user);

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    List<User> getFriends(Long userId);

    List<User> getCommonFriends(Long userId, Long otherId);
}
