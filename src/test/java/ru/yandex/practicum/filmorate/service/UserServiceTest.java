package ru.yandex.practicum.filmorate.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

class UserServiceTest {
    private UserService userService;

    @BeforeEach
    void setUp() {
        UserStorage userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
    }

    @Test
    void testAddFriendBidirectionally() {
        User firstUser = userService.addUser(createUser("first@mail.test", "first"));
        User secondUser = userService.addUser(createUser("second@mail.test", "second"));
        userService.addFriend(firstUser.getId(), secondUser.getId());
        assertThat(userService.getFriends(firstUser.getId())).extracting(User::getId)
            .containsExactly(secondUser.getId());
        assertThat(userService.getFriends(secondUser.getId())).extracting(User::getId)
            .containsExactly(firstUser.getId());
    }

    @Test
    void testRemoveFriendBidirectionally() {
        User firstUser = userService.addUser(createUser("first@mail.test", "first"));
        User secondUser = userService.addUser(createUser("second@mail.test", "second"));
        userService.addFriend(firstUser.getId(), secondUser.getId());
        userService.removeFriend(firstUser.getId(), secondUser.getId());
        assertThat(userService.getFriends(firstUser.getId())).isEmpty();
        assertThat(userService.getFriends(secondUser.getId())).isEmpty();
    }

    @Test
    void testReturnOnlyCommonFriends() {
        User firstUser = userService.addUser(createUser("first@mail.test", "first"));
        User secondUser = userService.addUser(createUser("second@mail.test", "second"));
        User commonFriend = userService.addUser(createUser("common@mail.test", "common"));
        User uniqueFriend = userService.addUser(createUser("unique@mail.test", "unique"));
        userService.addFriend(firstUser.getId(), commonFriend.getId());
        userService.addFriend(secondUser.getId(), commonFriend.getId());
        userService.addFriend(firstUser.getId(), uniqueFriend.getId());
        List<User> commonFriends = userService.getCommonFriends(firstUser.getId(), secondUser.getId());
        assertThat(commonFriends).extracting(User::getId).containsExactly(commonFriend.getId());
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
