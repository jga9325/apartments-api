package com.auger.apartments.users;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

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

    @Autowired
    UserRowMapper userRowMapper;

    @BeforeEach
    public void clearTable() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
    }

    @Test
    public void testCreate() {
        User user = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        int rowsAffected = underTest.create(user);
        assertThat(rowsAffected).isEqualTo(1);
        assertThat(getRowCount()).isEqualTo(1);

        Optional<User> optionalUser = jdbcTemplate.query(
                "SELECT * FROM users WHERE email = ? LIMIT 1",
                userRowMapper, "john@gmail.com").stream().findFirst();
        assertThat(optionalUser).isPresent();
        User createdUser = optionalUser.get();
        assertUsersAreEqual(user, createdUser);
    }

    @Test
    public void testCreateDuplicateUser() {
        User user = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        underTest.create(user);
        assertThatThrownBy(() -> underTest.create(user)).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    public void testFindOne() {
        User user = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        underTest.create(user);

        int userId = getIdFromEmail(user.email());
        Optional<User> optionalUser = underTest.findOne(userId);
        assertThat(optionalUser).isPresent();
        User createdUser = optionalUser.get();
        assertUsersAreEqual(user, createdUser);
    }

    @Test
    public void testFindOneWithInvalidId() {
        Optional<User> result = underTest.findOne(1);
        assertThat(result).isNotPresent();
    }

    @Test
    public void testFindAll() {
        User user1 = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Jennifer", "jennifer@gmail.com", "9876543214",
                LocalDate.of(1975, 8, 3), LocalDate.now());
        User user3 = new User(0, "Bob", "Bob@gmail.com", "7365490142",
                LocalDate.of(2001, 12, 19), LocalDate.now());

        assertThat(getRowCount()).isEqualTo(0);
        assertThat(underTest.findAll().size()).isEqualTo(0);
        underTest.create(user1);
        underTest.create(user2);
        underTest.create(user3);
        assertThat(getRowCount()).isEqualTo(3);
        assertThat(underTest.findAll().size()).isEqualTo(3);
    }

    @Test
    public void testUpdate() {
        User originalUser = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        underTest.create(originalUser);

        int userId = getIdFromEmail(originalUser.email());
        User updatedUser = new User(userId, "Kai", "kai@gmail.com", "7865436549",
                LocalDate.of(2003, 1, 18), null);

        int rowsAffected = underTest.update(updatedUser);
        assertThat(rowsAffected).isEqualTo(1);
        assertThat(getRowCount()).isEqualTo(1);

        Optional<User> optionalUser = underTest.findOne(userId);
        assertThat(optionalUser).isPresent();

        User user = optionalUser.get();
        User expectedUser = new User(userId, "Kai", "kai@gmail.com", "7865436549",
                LocalDate.of(2003, 1, 18), LocalDate.now());
        assertUsersAreEqual(user, expectedUser);
    }

    // Delete - Create a user, delete it, and check that it isn't there

    // Delete - Delete invalid id and verify exception

    @Test
    public void testExists() {
        assertThat(underTest.exists(1)).isFalse();
        User user = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        underTest.create(user);
        int userId = getIdFromEmail(user.email());
        assertThat(underTest.exists(userId)).isTrue();
    }

    private int getRowCount() {
        return JdbcTestUtils.countRowsInTable(jdbcTemplate, "users");
    }

    private void assertUsersAreEqual(User u1, User u2) {
        assertThat(u1.name()).isEqualTo(u2.name());
        assertThat(u1.email()).isEqualTo(u2.email());
        assertThat(u1.phoneNumber()).isEqualTo(u2.phoneNumber());
        assertThat(u1.birthDate()).isEqualTo(u2.birthDate());
        assertThat(u1.dateJoined()).isEqualTo(u2.dateJoined());
    }

    private int getIdFromEmail(String email) {
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Integer.class, "john@gmail.com");
    }
}
