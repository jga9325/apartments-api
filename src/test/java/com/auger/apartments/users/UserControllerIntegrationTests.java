package com.auger.apartments.users;

import com.auger.apartments.BaseControllerIntegrationTest;
import com.auger.apartments.apartments.Apartment;
import com.auger.apartments.applications.Application;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.auger.apartments.TestUtils.assertUsersAreEqual;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserControllerIntegrationTests extends BaseControllerIntegrationTest {

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    public void addData() {
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

    @AfterEach
    public void clearTable() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
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

    @Test
    public void testDeleteUser() {
        ResponseEntity<Void> deleteResponse = testRestTemplate
                .exchange("/users/{id}", HttpMethod.DELETE, null, Void.class, user1.id());

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = testRestTemplate
                .getForEntity("/users/{id}", String.class, user1.id());

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody())
                .isEqualTo(String.format("User with id %s does not exist", user1.id()));
    }

    @Test
    public void testDeleteUserInvalidId() {
        int invalidUserId = 0;

        ResponseEntity<String> deleteResponse = testRestTemplate.exchange("/users/{id}", HttpMethod.DELETE,
                null, String.class, invalidUserId);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(deleteResponse.getBody())
                .isEqualTo(String.format("User with id %s does not exist", invalidUserId));
    }

    @Test
    public void testDeleteUserIsRenter() {
        Apartment apt1 = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), user2.id());
        testRestTemplate.postForEntity("/apartments", apt1, Apartment.class);

        ResponseEntity<String> deleteResponse = testRestTemplate.exchange("/users/{id}", HttpMethod.DELETE,
                null, String.class, user2.id());

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(deleteResponse.getBody())
                .isEqualTo(String.format("Unable to delete user with id %s because they are renting an apartment",
                        user2.id()));
    }

    @Test
    public void testDeleteUserOwnsOccupiedApartment() {
        Apartment apt1 = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), user2.id());
        testRestTemplate.postForEntity("/apartments", apt1, Apartment.class);

        ResponseEntity<String> deleteResponse = testRestTemplate.exchange("/users/{id}", HttpMethod.DELETE,
                null, String.class, user1.id());

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(deleteResponse.getBody())
                .isEqualTo(String.format(
                        "Unable to delete user with id %s because they own at least one occupied apartment",
                        user1.id()));
    }

    @Test
    public void testDeleteUserWithApartmentsAndApplications() {
        Apartment apt1 = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), null);
        Apartment apt2 = new Apartment(null, "Suburban Getaway",
                "Entire unit in a quiet neighborhood", 3,
                2, "VA", "Norfolk", 1400, 310000,
                null, true, user1.id(), null);
        Apartment apt3 = new Apartment(null, "Beach Apartment",
                "One bed one bath apartment near the beach!", 1,
                1, "FL", "Miami", 800, 185000,
                null, true, user2.id(), null);
        Apartment apartment1 = testRestTemplate
                .postForEntity("/apartments", apt1, Apartment.class).getBody();
        Apartment apartment2 = testRestTemplate
                .postForEntity("/apartments", apt2, Apartment.class).getBody();
        Apartment apartment3 = testRestTemplate
                .postForEntity("/apartments", apt3, Apartment.class).getBody();

        Application a1 = new Application(null, null, true, false,
                user1.id(), apartment3.id());
        Application a2 = new Application(null, null, true, false,
                user2.id(), apartment1.id());
        Application a3 = new Application(null, null, true, false,
                user3.id(), apartment1.id());
        Application a4 = new Application(null, null, true, false,
                user2.id(), apartment2.id());
        Application a5 = new Application(null, null, true, false,
                user3.id(), apartment2.id());

        Application app1 = testRestTemplate.postForEntity("/applications", a1, Application.class).getBody();
        Application app2 = testRestTemplate.postForEntity("/applications", a2, Application.class).getBody();
        Application app3 = testRestTemplate.postForEntity("/applications", a3, Application.class).getBody();
        Application app4 = testRestTemplate.postForEntity("/applications", a4, Application.class).getBody();
        Application app5 = testRestTemplate.postForEntity("/applications", a5, Application.class).getBody();

        List<Integer> apartmentIdList = List.of(apartment1.id(), apartment2.id(), apartment3.id());
        List<Integer> applicationListId = List.of(app1.id(), app2.id(), app3.id(), app4.id(), app5.id());

        for (Integer id : apartmentIdList) {
            ResponseEntity<Apartment> getResponse = testRestTemplate
                    .getForEntity("/apartments/{id}", Apartment.class, id);
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        for (Integer id : applicationListId) {
            ResponseEntity<Application> getResponse = testRestTemplate
                    .getForEntity("/applications/{id}", Application.class, id);
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        testRestTemplate.exchange("/users/{id}", HttpMethod.DELETE, null, String.class, user1.id());

        ResponseEntity<String> apartment1Response = testRestTemplate
                .getForEntity("/apartments/{id}", String.class, apartment1.id());
        assertThat(apartment1Response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(apartment1Response.getBody()).isEqualTo(String.format("Apartment with id %s does not exist",
                apartment1.id()));

        ResponseEntity<String> apartment2Response = testRestTemplate
                .getForEntity("/apartments/{id}", String.class, apartment2.id());
        assertThat(apartment2Response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(apartment2Response.getBody()).isEqualTo(String.format("Apartment with id %s does not exist",
                apartment2.id()));

        ResponseEntity<Apartment> apartment3Response = testRestTemplate
                .getForEntity("/apartments/{id}", Apartment.class, apartment3.id());
        assertThat(apartment3Response.getStatusCode()).isEqualTo(HttpStatus.OK);

        for (Integer id : applicationListId) {
            ResponseEntity<String> getResponse = testRestTemplate
                    .getForEntity("/applications/{id}", String.class, id);
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(getResponse.getBody()).isEqualTo(String.format("Application with id %s does not exist", id));
        }
    }
}
