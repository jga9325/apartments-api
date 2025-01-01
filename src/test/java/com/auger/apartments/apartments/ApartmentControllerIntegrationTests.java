package com.auger.apartments.apartments;

import com.auger.apartments.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
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
public class ApartmentControllerIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2-alpine");

    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void clearTable() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "apartments");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
    }

    @Test
    public void testCreateApartmentAndFindApartment() {
        User user = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User createdUser = testRestTemplate.postForEntity("/users", user, User.class).getBody();

        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser.id(), null);
        ResponseEntity<Apartment> createResponse = testRestTemplate
                .postForEntity("/apartments", apartment, Apartment.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        int apartmentId = createResponse.getBody().id();
        assertThat(apartmentId).isNotNull();

        ResponseEntity<Apartment> retrievedApartment = testRestTemplate
                .getForEntity("/apartments/{id}", Apartment.class, createResponse.getBody().id());

        assertThat(retrievedApartment.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(apartmentId).isEqualTo(retrievedApartment.getBody().id());
        assertApartmentsAreEqual(createResponse.getBody(), retrievedApartment.getBody());
    }

    @Test
    public void testCreateApartmentInvalidOwner() {
        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, null);
        ResponseEntity<String> createResponse = testRestTemplate
                .postForEntity("/apartments", apartment, String.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(createResponse.getBody())
                .isEqualTo(String.format("User with id %s does not exist", apartment.ownerId()));
    }

    @Test
    public void testCreateApartmentInvalidRenter() {
        User user = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User createdUser = testRestTemplate.postForEntity("/users", user, User.class).getBody();

        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser.id(), 0);
        ResponseEntity<String> createResponse = testRestTemplate
                .postForEntity("/apartments", apartment, String.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(createResponse.getBody())
                .isEqualTo(String.format("User with id %s does not exist", apartment.renterId()));
    }

    @Test
    public void testCreateApartmentDuplicateRenter() {
        User user1 = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "bob@gmail.com", "8456320985",
                LocalDate.of(1994, 10, 11), LocalDate.now());
        User createdUser1 = testRestTemplate.postForEntity("/users", user1, User.class).getBody();
        User createdUser2 = testRestTemplate.postForEntity("/users", user2, User.class).getBody();

        Apartment apartment1 = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), createdUser2.id());
        Apartment apartment2 = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), createdUser2.id());
        testRestTemplate.postForEntity("/apartments", apartment1, String.class);
        ResponseEntity<String> createResponse = testRestTemplate
                .postForEntity("/apartments", apartment2, String.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(createResponse.getBody())
                .isEqualTo(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, apartment2.renterId()));
    }

    @Test
    public void testGetApartmentInvalidId() {
        int invalidApartmentId = 1;

        ResponseEntity<String> findResponse = testRestTemplate
                .getForEntity("/apartments/{id}", String.class, invalidApartmentId);

        assertThat(findResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(findResponse.getBody())
                .isEqualTo(String.format("Apartment with id %s does not exist", invalidApartmentId));
    }

    @Test
    public void testGetAllApartments() {

    }

    @Test
    public void testUpdateApartment() {

    }

    @Test
    public void testUpdateApartmentInvalidId() {

    }

    @Test
    public void testUpdateApartmentInvalidOwner() {

    }

    @Test
    public void testUpdateApartmentInvalidRenter() {

    }

    @Test
    public void testUpdateApartmentDuplicateRenter() {

    }

    private void assertApartmentsAreEqual(Apartment a1, Apartment a2) {
        assertThat(a1.id()).isEqualTo(a2.id());
        assertThat(a1.title()).isEqualTo(a2.title());
        assertThat(a1.description()).isEqualTo(a2.description());
        assertThat(a1.numberOfBedrooms()).isEqualTo(a2.numberOfBedrooms());
        assertThat(a1.numberOfBathrooms()).isEqualTo(a2.numberOfBathrooms());
        assertThat(a1.state()).isEqualTo(a2.state());
        assertThat(a1.city()).isEqualTo(a2.city());
        assertThat(a1.squareFeet()).isEqualTo(a2.squareFeet());
        assertThat(a1.monthlyRent()).isEqualTo(a2.monthlyRent());
        assertThat(a1.dateListed()).isEqualTo(a2.dateListed());
        assertThat(a1.available()).isEqualTo(a2.available());
        assertThat(a1.ownerId()).isEqualTo(a2.ownerId());
        assertThat(a1.renterId()).isEqualTo(a2.renterId());
    }
}
