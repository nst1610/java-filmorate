package ru.yandex.practicum.filmorate.storage.db;

import java.sql.Date;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.db.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

@Repository
@Qualifier("userDbStorage")
public class UserDbStorage extends BaseRepository<User> implements UserStorage {
    private static final String FIND_ALL_USERS_QUERY = "SELECT * FROM users ORDER BY id";
    private static final String FIND_USER_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_FRIEND_IDS_QUERY = """
        SELECT friend_id
        FROM friendships
        WHERE user_id = ?
        ORDER BY friend_id
        """;
    private static final String FIND_FRIENDS_QUERY = """
        SELECT u.*
        FROM users u
        JOIN friendships f ON u.id = f.friend_id
        WHERE f.user_id = ?
        ORDER BY u.id
        """;
    private static final String FIND_COMMON_FRIENDS_QUERY = """
        SELECT u.*
        FROM users u
        JOIN friendships f1 ON u.id = f1.friend_id
        JOIN friendships f2 ON u.id = f2.friend_id
        WHERE f1.user_id = ? AND f2.user_id = ?
        ORDER BY u.id
        """;
    private static final String INSERT_USER_QUERY = """
        INSERT INTO users(email, login, name, birthday)
        VALUES (?, ?, ?, ?)
        """;
    private static final String UPDATE_USER_QUERY = """
        UPDATE users
        SET email = ?, login = ?, name = ?, birthday = ?
        WHERE id = ?
        """;
    private static final String INSERT_FRIEND_QUERY = """
        MERGE INTO friendships (user_id, friend_id)
        KEY (user_id, friend_id)
        VALUES (?, ?)
        """;
    private static final String DELETE_FRIEND_QUERY = """
        DELETE FROM friendships
        WHERE user_id = ? AND friend_id = ?
        """;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, new UserRowMapper());
    }

    @Override
    public Collection<User> getUsers() {
        return findMany(FIND_ALL_USERS_QUERY);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return findOne(FIND_USER_BY_ID_QUERY, id).map(this::enrichUser);
    }

    @Override
    public User addUser(User user) {
        user.setId(insert(
            INSERT_USER_QUERY,
            user.getEmail(),
            user.getLogin(),
            user.getName(),
            Date.valueOf(user.getBirthday())
        ));
        user.setFriends(new HashSet<>());
        return user;
    }

    @Override
    public User updateUser(User user) {
        update(
            UPDATE_USER_QUERY,
            user.getEmail(),
            user.getLogin(),
            user.getName(),
            Date.valueOf(user.getBirthday()),
            user.getId()
        );
        return enrichUser(user);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        update(
            INSERT_FRIEND_QUERY,
            userId,
            friendId
        );
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        delete(DELETE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        return findMany(FIND_FRIENDS_QUERY, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        return findMany(FIND_COMMON_FRIENDS_QUERY, userId, otherId);
    }

    private List<Long> getFriendIds(Long userId) {
        return jdbc.queryForList(FIND_FRIEND_IDS_QUERY, Long.class, userId);
    }

    private User enrichUser(User user) {
        user.setFriends(new HashSet<>(getFriendIds(user.getId())));
        return user;
    }
}
