package com.auger.apartments.users;

import org.junit.jupiter.api.AfterEach;
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
import java.util.List;
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

    @BeforeEach
    public void addUsers() {
        // TODO
    }

    @AfterEach
    public void clearTable() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
    }

    @Test
    public void testCreateAndFindAllAndFindOne() {
        User user = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        int rowsAffected = underTest.create(user);
        assertThat(rowsAffected).isEqualTo(1);
        List<User> result = underTest.findAll();
        assertThat(result.size()).isEqualTo(1);

        User createdUser = result.get(0);
        assertThat(createdUser.name()).isEqualTo("John");
        assertThat(createdUser.email()).isEqualTo("john@gmail.com");
        assertThat(createdUser.phoneNumber()).isEqualTo("1234567894");
        assertThat(createdUser.birthDate()).isEqualTo(LocalDate.of(1999, 4, 28));
        assertThat(createdUser.dateJoined()).isEqualTo(LocalDate.now());

        int userId = createdUser.id();
        Optional<User> optionalUser = underTest.findOne(userId);
        assertThat(optionalUser.isPresent()).isTrue();
        User existingUser = optionalUser.get();
        assertThat(existingUser.name()).isEqualTo("John");
        assertThat(existingUser.email()).isEqualTo("john@gmail.com");
        assertThat(existingUser.phoneNumber()).isEqualTo("1234567894");
        assertThat(existingUser.birthDate()).isEqualTo(LocalDate.of(1999, 4, 28));
        assertThat(existingUser.dateJoined()).isEqualTo(LocalDate.now());
    }

    @Test
    public void testCreateDuplicateUser() {
        User user = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        int rowsAffected = underTest.create(user);
        assertThat(rowsAffected).isEqualTo(1);
        assertThatThrownBy(() -> underTest.create(user)).isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    public void testFindOneWithInvalidId() {
        Optional<User> result = underTest.findOne(1);
        assertThat(result.isPresent()).isFalse();
    }

    @Test
    public void testFindAllWithMultipleUsers() {
        // TODO
    }
}
