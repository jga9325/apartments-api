package com.auger.apartments.users;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2-alpine");

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void clearTable() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
    }

    @Test
    public void testCreateUserAndFindUser() {
        User user = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());

        ResponseEntity<User> createdUser = testRestTemplate.postForEntity("/users", user, User.class);

        int userId = createdUser.getBody().id();
        assertThat(createdUser.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(userId).isNotZero();
        assertUsersAreEqual(user, createdUser.getBody());

        ResponseEntity<User> retrievedUser = testRestTemplate.getForEntity("/users/{id}", User.class, userId);

        assertThat(retrievedUser.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userId).isEqualTo(retrievedUser.getBody().id());
        assertUsersAreEqual(createdUser.getBody(), retrievedUser.getBody());
    }

    private void assertUsersAreEqual(User u1, User u2) {
        assertThat(u1.name()).isEqualTo(u2.name());
        assertThat(u1.email()).isEqualTo(u2.email());
        assertThat(u1.phoneNumber()).isEqualTo(u2.phoneNumber());
        assertThat(u1.birthDate()).isEqualTo(u2.birthDate());
        assertThat(u1.dateJoined()).isEqualTo(u2.dateJoined());
    }

}
