package com.auger.apartments.apartments;

import com.auger.apartments.users.User;
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

        ResponseEntity<Apartment> getResponse = testRestTemplate
                .getForEntity("/apartments/{id}", Apartment.class, createResponse.getBody().id());

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(apartmentId).isEqualTo(getResponse.getBody().id());
        assertApartmentsAreEqual(createResponse.getBody(), getResponse.getBody());
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

        ResponseEntity<String> getResponse = testRestTemplate
                .getForEntity("/apartments/{id}", String.class, invalidApartmentId);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody())
                .isEqualTo(String.format("Apartment with id %s does not exist", invalidApartmentId));
    }

    @Test
    public void testGetAllApartments() {
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
        Apartment apartment2 = new Apartment(null, "Comfy Studio",
                "Studio space in downtown Manhattan. Great location", 0,
                1, "NY", "New York", 400, 280000,
                null, true, createdUser2.id(), null);
        Apartment apartment3 = new Apartment(null, "Beach Stay",
                "Secluded home, perfect for a quiet and relaxing getaway.", 2,
                2, "HI", "Honolulu", 400, 280000,
                null, true, createdUser1.id(), null);

        Apartment createdApartment1 = testRestTemplate.postForEntity("/apartments", apartment1, Apartment.class)
                .getBody();
        Apartment createdApartment2 = testRestTemplate.postForEntity("/apartments", apartment2, Apartment.class)
                .getBody();
        Apartment createdApartment3 = testRestTemplate.postForEntity("/apartments", apartment3, Apartment.class)
                .getBody();

        Map<Integer, Apartment> apartmentMap = new HashMap<>();
        apartmentMap.put(createdApartment1.id(), createdApartment1);
        apartmentMap.put(createdApartment2.id(), createdApartment2);
        apartmentMap.put(createdApartment3.id(), createdApartment3);

        ResponseEntity<List<Apartment>> getAllResponse = testRestTemplate
                .exchange("/apartments", HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<Apartment>>() {});

        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllResponse.getBody().size()).isEqualTo(apartmentMap.size());

        for (Apartment apt : getAllResponse.getBody()) {
            Apartment createdApartment = apartmentMap.get(apt.id());
            assertApartmentsAreEqual(apt, createdApartment);
            apartmentMap.remove(apt.id());
        }
        assertThat(apartmentMap.size()).isZero();
    }

    @Test
    public void testUpdateApartment() {
        User user = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User createdUser = testRestTemplate.postForEntity("/users", user, User.class).getBody();

        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser.id(), null);
        Apartment createdApartment = testRestTemplate
                .postForEntity("/apartments", apartment, Apartment.class).getBody();

        Apartment updatedApartment = new Apartment(createdApartment.id(), "Main Street Condo",
                "Great views and updated appliances!", 2,
                1, "NY", "New York", 800, 635000,
                createdApartment.dateListed(), true, createdUser.id(), null);

        HttpEntity<Apartment> requestEntity = new HttpEntity<>(updatedApartment);
        ResponseEntity<Void> updateResponse = testRestTemplate
                .exchange("/apartments", HttpMethod.PUT, requestEntity, Void.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Apartment retrievedApartment = testRestTemplate
                .getForEntity("/apartments/{id}", Apartment.class, createdApartment.id()).getBody();
        assertApartmentsAreEqual(updatedApartment, retrievedApartment);
    }

    @Test
    public void testUpdateApartmentInvalidId() {
        Apartment apartment = new Apartment(1, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 0, null);

        HttpEntity<Apartment> requestEntity = new HttpEntity<>(apartment);
        ResponseEntity<String> updateResponse = testRestTemplate
                .exchange("/apartments", HttpMethod.PUT, requestEntity, String.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(updateResponse.getBody())
                .isEqualTo(String.format("Apartment with id %s does not exist", apartment.id()));
    }

    @Test
    public void testUpdateApartmentInvalidOwner() {
        User user = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User createdUser = testRestTemplate.postForEntity("/users", user, User.class).getBody();

        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser.id(), null);
        Apartment createdApartment = testRestTemplate
                .postForEntity("/apartments", apartment, Apartment.class).getBody();

        Apartment updatedApartment = new Apartment(createdApartment.id(), "Main Street Condo",
                "Great views and updated appliances!", 2,
                1, "NY", "New York", 800, 635000,
                null, true, 0, null);

        HttpEntity<Apartment> requestEntity = new HttpEntity<>(updatedApartment);
        ResponseEntity<String> updateResponse = testRestTemplate
                .exchange("/apartments", HttpMethod.PUT, requestEntity, String.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(updateResponse.getBody())
                .isEqualTo(String.format("User with id %s does not exist", updatedApartment.ownerId()));
    }

    @Test
    public void testUpdateApartmentInvalidRenter() {
        User user = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User createdUser = testRestTemplate.postForEntity("/users", user, User.class).getBody();

        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser.id(), null);
        Apartment createdApartment = testRestTemplate
                .postForEntity("/apartments", apartment, Apartment.class).getBody();

        Apartment updatedApartment = new Apartment(createdApartment.id(), "Main Street Condo",
                "Great views and updated appliances!", 2,
                1, "NY", "New York", 800, 635000,
                null, true, createdUser.id(), 0);

        HttpEntity<Apartment> requestEntity = new HttpEntity<>(updatedApartment);
        ResponseEntity<String> updateResponse = testRestTemplate
                .exchange("/apartments", HttpMethod.PUT, requestEntity, String.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(updateResponse.getBody())
                .isEqualTo(String.format("User with id %s does not exist", updatedApartment.renterId()));
    }

    @Test
    public void testUpdateApartmentDuplicateRenter() {
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
        Apartment apartment2 = new Apartment(null, "Comfy Studio",
                "Studio space in downtown Manhattan. Great location", 0,
                1, "NY", "New York", 400, 280000,
                null, true, createdUser1.id(), null);
        testRestTemplate.postForEntity("/apartments", apartment1, Apartment.class).getBody();
        Apartment createdApartment2 = testRestTemplate
                .postForEntity("/apartments", apartment2, Apartment.class).getBody();

        Apartment updatedApartment = new Apartment(createdApartment2.id(), "Comfy Studio",
                "Studio space in downtown Manhattan. Great location", 0,
                1, "NY", "New York", 400, 280000,
                null, true, createdUser1.id(), createdUser2.id());

        HttpEntity<Apartment> requestEntity = new HttpEntity<>(updatedApartment);
        ResponseEntity<String> updateResponse = testRestTemplate
                .exchange("/apartments", HttpMethod.PUT, requestEntity, String.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(updateResponse.getBody())
                .isEqualTo(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, updatedApartment.renterId()));
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
