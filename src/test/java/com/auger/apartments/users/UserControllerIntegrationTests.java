package com.auger.apartments.users;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.auger.apartments.TestUtils.assertUsersAreEqual;
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

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    public void setUp() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");

        User u1 = new User(null, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), null);
        User u2 = new User(null, "Bob", "Daly", "bob@gmail.com",
                "7564839402", LocalDate.of(1985, 2, 7), null);
        User u3 = new User(null, "Jennifer", "Lilly", "jennifer@gmail.com",
                "1275643908", LocalDate.of(2001, 8, 15), null);

        user1 = testRestTemplate.postForEntity("/users", u1, User.class).getBody();
        user2 = testRestTemplate.postForEntity("/users", u2, User.class).getBody();
        user3 = testRestTemplate.postForEntity("/users", u3, User.class).getBody();
    }

    @Test
    public void testCreateUserAndFindUser() {
        User user = new User(null, "Conrad", "Wiggins", "conrad@gmail.com",
                "4876280912", LocalDate.of(1972, 11, 14), null);

        ResponseEntity<User> createdUser = testRestTemplate.postForEntity("/users", user, User.class);

        int userId = createdUser.getBody().id();
        assertThat(createdUser.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(userId).isNotNull();

        ResponseEntity<User> retrievedUser = testRestTemplate.getForEntity("/users/{id}", User.class, userId);

        assertThat(retrievedUser.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userId).isEqualTo(retrievedUser.getBody().id());
        assertUsersAreEqual(createdUser.getBody(), retrievedUser.getBody());
    }

    @Test
    public void testCreateUserDuplicateEmail() {
        User duplicateEmailUser = new User(null, "John", "Rogers", "john@gmail.com",
                "1834276539", LocalDate.of(1999, 4, 28), null);

        ResponseEntity<String> response = testRestTemplate
                .postForEntity("/users", duplicateEmailUser, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("A user with that email already exists");
    }

    @Test
    public void testCreateUserDuplicatePhoneNumber() {
        User duplicatePhoneNumberUser = new User(null, "John", "Rogers", "johnny@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), null);

        ResponseEntity<String> response = testRestTemplate
                .postForEntity("/users", duplicatePhoneNumberUser, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo("A user with that phone number already exists");
    }

    @Test
    public void testGetUserInvalidId() {
        int invalidUserId = 0;

        ResponseEntity<String> response = testRestTemplate
                .getForEntity("/users/{id}", String.class, invalidUserId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(String.format("User with id %s does not exist", invalidUserId));
    }

    @Test
    public void testGetAllUsers() {
        Map<Integer, User> userMap = new HashMap<>();
        userMap.put(user1.id(), user1);
        userMap.put(user2.id(), user2);
        userMap.put(user3.id(), user3);

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

    @Test
    public void testUpdateUser() {
        User updatedUser = new User(user1.id(), user1.firstName(), user1.lastName(), "johnnie@gmail.com",
                user1.phoneNumber(), user1.birthDate(), null);

        HttpEntity<User> requestEntity = new HttpEntity<>(updatedUser);
        ResponseEntity<Void> updateResponse = testRestTemplate
                .exchange("/users", HttpMethod.PUT, requestEntity, Void.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        User retrievedUser = testRestTemplate.getForEntity("/users/{id}", User.class, updatedUser.id()).getBody();

        User expectedUser = new User(user1.id(), user1.firstName(), user1.lastName(), "johnnie@gmail.com",
                user1.phoneNumber(), user1.birthDate(), user1.dateJoined());
        assertUsersAreEqual(expectedUser, retrievedUser);
    }

    @Test
    public void testUpdateUserInvalidId() {
        User user = new User(0, "John", "john@gmail.com", "Rogers",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        HttpEntity<User> requestEntity = new HttpEntity<>(user);
        ResponseEntity<String> updateResponse = testRestTemplate
                .exchange("/users", HttpMethod.PUT, requestEntity, String.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(updateResponse.getBody()).isEqualTo(String.format("User with id %s does not exist", user.id()));
    }

    @Test
    public void testUpdateUserNullId() {
        User user = new User(null, "John", "john@gmail.com", "Rogers",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        HttpEntity<User> requestEntity = new HttpEntity<>(user);
        ResponseEntity<String> updateResponse = testRestTemplate
                .exchange("/users", HttpMethod.PUT, requestEntity, String.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(updateResponse.getBody()).isEqualTo(String.format("User with id %s does not exist", user.id()));
    }

    @Test
    public void testUpdateUserDuplicateEmail() {
        User duplicateEmailUser = new User(user2.id(), "Bob", "Daly",
                "john@gmail.com", "1876542567", LocalDate.of(1989, 7, 30),
                LocalDate.now());

        HttpEntity<User> requestEntity = new HttpEntity<>(duplicateEmailUser);
        ResponseEntity<String> updateResponse = testRestTemplate
                .exchange("/users", HttpMethod.PUT, requestEntity, String.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(updateResponse.getBody()).isEqualTo("A user with that email already exists");
    }

    @Test
    public void testUpdateUserDuplicatePhoneNumber() {
        User duplicatePhoneNumberUser = new User(user2.id(), "Bob", "Daly",
                "bob@gmail.com", "1234567894", LocalDate.of(1989, 7, 30),
                LocalDate.now());

        HttpEntity<User> requestEntity = new HttpEntity<>(duplicatePhoneNumberUser);
        ResponseEntity<String> updateResponse = testRestTemplate
                .exchange("/users", HttpMethod.PUT, requestEntity, String.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(updateResponse.getBody()).isEqualTo("A user with that phone number already exists");
    }
}
