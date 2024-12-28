package com.auger.apartments.users;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.web.client.RestClientException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Test
    public void testCreateUserDuplicateEmail() {
        User originalUser = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User duplicateEmailUser = new User(0, "John", "john@gmail.com", "1834276539",
                LocalDate.of(1999, 4, 28), LocalDate.now());

        testRestTemplate.postForEntity("/users", originalUser, User.class);

        ResponseEntity<?> response;
        try {
            response = testRestTemplate
                    .postForEntity("/users", duplicateEmailUser, User.class);
        } catch (RestClientException ex) {
            response = testRestTemplate
                    .postForEntity("/users", duplicateEmailUser, String.class);
        }

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("A user with that email already exists");
    }

    @Test
    public void testCreateUserDuplicatePhoneNumber() {
        User originalUser = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User duplicateEmailUser = new User(0, "John", "johnny@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());

        testRestTemplate.postForEntity("/users", originalUser, User.class);

        ResponseEntity<?> response;
        try {
            response = testRestTemplate
                    .postForEntity("/users", duplicateEmailUser, User.class);
        } catch (RestClientException ex) {
            response = testRestTemplate
                    .postForEntity("/users", duplicateEmailUser, String.class);
        }

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("A user with that phone number already exists");
    }

    @Test
    public void testGetUserInvalidId() {
        int invalidUserId = 1;

        ResponseEntity<?> response;
        try {
            response = testRestTemplate
                    .getForEntity("/users/{id}", User.class, invalidUserId);
        } catch (RestClientException ex) {
            response = testRestTemplate
                    .getForEntity("/users/{id}", String.class, invalidUserId);
        }

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(String.format("User with id %s does not exist", invalidUserId));
    }

    @Test
    public void testGetAllUsers() {
        User user1 = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "bob@gmail.com", "7564839402",
                LocalDate.of(1985, 2, 7), LocalDate.now());
        User user3 = new User(0, "Jennifer", "jennifer@gmail.com", "1275643908",
                LocalDate.of(2001, 8, 15), LocalDate.now());

        User createdUser1 = testRestTemplate.postForEntity("/users", user1, User.class).getBody();
        User createdUser2 = testRestTemplate.postForEntity("/users", user2, User.class).getBody();
        User createdUser3 = testRestTemplate.postForEntity("/users", user3, User.class).getBody();

        Map<Integer, User> userMap = new HashMap<>();
        userMap.put(createdUser1.id(), createdUser1);
        userMap.put(createdUser2.id(), createdUser2);
        userMap.put(createdUser3.id(), createdUser3);

        ResponseEntity<List<User>> users = testRestTemplate
                .exchange("/users", HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<User>>() {});

        assertThat(users.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(users.getBody().size()).isEqualTo(userMap.size());

        for (User user : users.getBody()) {
            User originalUser = userMap.get(user.id());
            assertUsersAreEqual(user, originalUser);
            userMap.remove(user.id());
        }
        assertThat(userMap.size()).isZero();
    }

    private void assertUsersAreEqual(User u1, User u2) {
        assertThat(u1.name()).isEqualTo(u2.name());
        assertThat(u1.email()).isEqualTo(u2.email());
        assertThat(u1.phoneNumber()).isEqualTo(u2.phoneNumber());
        assertThat(u1.birthDate()).isEqualTo(u2.birthDate());
        assertThat(u1.dateJoined()).isEqualTo(u2.dateJoined());
    }

}
