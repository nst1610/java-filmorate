package ru.yandex.practicum.filmorate.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    @Test
    void testReturnAllUsers() {
        User firstUser = userStorage.addUser(createUser("first@mail.test", "first"));
        User secondUser = userStorage.addUser(createUser("second@mail.test", "second"));
        assertThat(userStorage.getUsers())
            .extracting(User::getId)
            .containsExactly(firstUser.getId(), secondUser.getId());
    }

    @Test
    void testFindUserById() {
        User user = userStorage.addUser(createUser("user@mail.test", "user"));
        assertThat(userStorage.getUserById(user.getId()))
            .isPresent()
            .hasValueSatisfying(foundUser -> assertThat(foundUser.getEmail()).isEqualTo("user@mail.test"));
    }

    @Test
    void testAddUser() {
        User savedUser = userStorage.addUser(createUser("user@mail.test", "user"));
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getFriends()).isEmpty();
    }

    @Test
    void testUpdateUser() {
        User user = userStorage.addUser(createUser("user@mail.test", "user"));
        user.setName("Updated name");
        User updatedUser = userStorage.updateUser(user);
        assertThat(updatedUser.getName()).isEqualTo("Updated name");
        assertThat(userStorage.getUserById(user.getId())).get().extracting(User::getName).isEqualTo("Updated name");
    }

    @Test
    void testAddAndReturnFriendsOneWay() {
        User user = userStorage.addUser(createUser("user@mail.test", "user"));
        User friend = userStorage.addUser(createUser("friend@mail.test", "friend"));
        userStorage.addFriend(user.getId(), friend.getId());
        assertThat(userStorage.getFriends(user.getId())).extracting(User::getId).containsExactly(friend.getId());
        assertThat(userStorage.getFriends(friend.getId())).isEmpty();
    }

    @Test
    void testRemoveFriend() {
        User user = userStorage.addUser(createUser("user@mail.test", "user"));
        User friend = userStorage.addUser(createUser("friend@mail.test", "friend"));
        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.removeFriend(user.getId(), friend.getId());
        assertThat(userStorage.getFriends(user.getId())).isEmpty();
    }

    @Test
    void testReturnCommonFriends() {
        User firstUser = userStorage.addUser(createUser("first@mail.test", "first"));
        User secondUser = userStorage.addUser(createUser("second@mail.test", "second"));
        User commonFriend = userStorage.addUser(createUser("common@mail.test", "common"));
        User uniqueFriend = userStorage.addUser(createUser("unique@mail.test", "unique"));
        userStorage.addFriend(firstUser.getId(), commonFriend.getId());
        userStorage.addFriend(secondUser.getId(), commonFriend.getId());
        userStorage.addFriend(firstUser.getId(), uniqueFriend.getId());
        assertThat(userStorage.getCommonFriends(firstUser.getId(), secondUser.getId()))
            .extracting(User::getId)
            .containsExactly(commonFriend.getId());
    }

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}
