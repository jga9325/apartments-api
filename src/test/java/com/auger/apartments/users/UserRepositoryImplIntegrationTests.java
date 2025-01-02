package com.auger.apartments.users;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.auger.apartments.TestUtils.assertUsersAreEqual;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@SpringBootTest
public class UserRepositoryImplIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2-alpine");

    @Autowired
    UserRepositoryImpl underTest;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    public void setUp() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");

        User u1 = new User(null, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), null);
        User u2 = new User(null, "Jennifer", "Lilly", "jennifer@gmail.com",
                "9876543214", LocalDate.of(1975, 8, 3), null);
        User u3 = new User(null, "Bob", "Daly", "Bob@gmail.com",
                "7365490142", LocalDate.of(2001, 12, 19), null);
        user1 = underTest.create(u1);
        user2 = underTest.create(u2);
        user3 = underTest.create(u3);
    }

    @Test
    public void testCreateAndFindOne() {
        User user = new User(null, "Conrad", "Wiggins", "conrad@gmail.com",
                "4876280912", LocalDate.of(1972, 11, 14), null);

        assertThat(getRowCount()).isEqualTo(3);
        User createdUser = underTest.create(user);
        assertThat(getRowCount()).isEqualTo(4);
        assertThat(createdUser.id()).isNotNull();

        Optional<User> optionalUser = underTest.findOne(createdUser.id());
        assertThat(optionalUser).isPresent();
        User retrievedUser = optionalUser.get();
        assertUsersAreEqual(createdUser, retrievedUser);
    }

    @Test
    public void testFindOneWithInvalidId() {
        Optional<User> result = underTest.findOne(0);
        assertThat(result).isNotPresent();
    }

    @Test
    public void testFindAll() {
        Map<Integer, User> userMap = new HashMap<>();
        userMap.put(user1.id(), user1);
        userMap.put(user2.id(), user2);
        userMap.put(user3.id(), user3);

        List<User> users = underTest.findAll();

        assertThat(getRowCount()).isEqualTo(userMap.size());
        assertThat(users.size()).isEqualTo(userMap.size());

        for (User user : users) {
            User createdUser = userMap.get(user.id());
            assertUsersAreEqual(user, createdUser);
            userMap.remove(user.id());
        }
        assertThat(userMap.size()).isZero();
    }

    @Test
    public void testUpdate() {
        User updatedUser = new User(user1.id(), "Kai", "Asakura", "kai@gmail.com",
                "7865436549", LocalDate.of(2003, 1, 18), null);

        underTest.update(updatedUser);

        assertThat(getRowCount()).isEqualTo(3);

        Optional<User> optionalUser = underTest.findOne(updatedUser.id());
        assertThat(optionalUser).isPresent();

        User retrievedUser = optionalUser.get();
        User expectedUser = new User(updatedUser.id(), "Kai", "Asakura", "kai@gmail.com",
                "7865436549", LocalDate.of(2003, 1, 18), user1.dateJoined());
        assertUsersAreEqual(retrievedUser, expectedUser);
    }

    // Delete - Create a user, delete it, and check that it isn't there

    // Delete - Delete invalid id and verify exception

    @Test
    public void testExists() {
        assertThat(underTest.exists(0)).isFalse();
        assertThat(underTest.exists(user1.id())).isTrue();
    }

    private int getRowCount() {
        return JdbcTestUtils.countRowsInTable(jdbcTemplate, "users");
    }
}
