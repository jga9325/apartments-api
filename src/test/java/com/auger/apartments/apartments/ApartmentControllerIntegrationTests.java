package com.auger.apartments.apartments;

import com.auger.apartments.ControllerIntegrationTest;
import com.auger.apartments.applications.Application;
import com.auger.apartments.users.User;
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

import static com.auger.apartments.TestUtils.assertApartmentsAreEqual;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ApartmentControllerIntegrationTests extends ControllerIntegrationTest {

    private User user1;
    private User user2;
    private User user3;
    private Apartment apartment1;
    private Apartment apartment2;
    private Apartment apartment3;

    @BeforeEach
    public void addData() {
        User u1 = new User(null, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), null);
        User u2 = new User(null, "Bob", "Daly", "bob@gmail.com",
                "8456320985", LocalDate.of(1994, 10, 11), null);
        User u3 = new User(null, "Jennifer", "Lilly", "jennifer@gmail.com",
                "1275643908", LocalDate.of(2001, 8, 15), null);
        user1 = testRestTemplate.postForEntity("/users", u1, User.class).getBody();
        user2 = testRestTemplate.postForEntity("/users", u2, User.class).getBody();
        user3 = testRestTemplate.postForEntity("/users", u3, User.class).getBody();

        Apartment apt1 = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), user2.id());
        Apartment apt2 = new Apartment(null, "Comfy Studio",
                "Studio space in downtown Manhattan. Great location", 0,
                1, "NY", "New York", 400, 280000,
                null, true, user2.id(), null);
        Apartment apt3 = new Apartment(null, "Beach Stay",
                "Secluded home, perfect for a quiet and relaxing getaway.", 2,
                2, "HI", "Honolulu", 400, 280000,
                null, true, user1.id(), null);

        apartment1 = testRestTemplate.postForEntity("/apartments", apt1, Apartment.class).getBody();
        apartment2 = testRestTemplate.postForEntity("/apartments", apt2, Apartment.class).getBody();
        apartment3 = testRestTemplate.postForEntity("/apartments", apt3, Apartment.class).getBody();
    }

    @AfterEach
    public void clearTables() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "apartments");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
    }

    @Test
    public void testCreateApartmentAndGetApartment() {
        Apartment apartment = new Apartment(null, "Condo #5",
                "New appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), null);
        ResponseEntity<Apartment> createResponse = testRestTemplate
                .postForEntity("/apartments", apartment, Apartment.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        int apartmentId = createResponse.getBody().id();
        assertThat(apartmentId).isNotNull();

        ResponseEntity<Apartment> getResponse = testRestTemplate
                .getForEntity("/apartments/{id}", Apartment.class, apartmentId);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertApartmentsAreEqual(createResponse.getBody(), getResponse.getBody());
    }

    @Test
    public void testCreateApartmentInvalidOwner() {
        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 0, null);
        ResponseEntity<String> createResponse = testRestTemplate
                .postForEntity("/apartments", apartment, String.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(createResponse.getBody())
                .isEqualTo(String.format("User with id %s does not exist", apartment.ownerId()));
    }

    @Test
    public void testCreateApartmentInvalidRenter() {
        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), 0);
        ResponseEntity<String> createResponse = testRestTemplate
                .postForEntity("/apartments", apartment, String.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(createResponse.getBody())
                .isEqualTo(String.format("User with id %s does not exist", apartment.renterId()));
    }

    @Test
    public void testCreateApartmentDuplicateRenter() {
        Apartment duplicateRenterApartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), user2.id());

        testRestTemplate.postForEntity("/apartments", duplicateRenterApartment, String.class);
        ResponseEntity<String> createResponse = testRestTemplate
                .postForEntity("/apartments", duplicateRenterApartment, String.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(createResponse.getBody())
                .isEqualTo(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, duplicateRenterApartment.renterId()));
    }

    @Test
    public void testGetApartmentInvalidId() {
        int invalidApartmentId = 0;

        ResponseEntity<String> getResponse = testRestTemplate
                .getForEntity("/apartments/{id}", String.class, invalidApartmentId);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody())
                .isEqualTo(String.format("Apartment with id %s does not exist", invalidApartmentId));
    }

    @Test
    public void testGetAllApartments() {
        Map<Integer, Apartment> apartmentMap = new HashMap<>();
        apartmentMap.put(apartment1.id(), apartment1);
        apartmentMap.put(apartment2.id(), apartment2);
        apartmentMap.put(apartment3.id(), apartment3);

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
        Apartment updatedApartment = new Apartment(apartment1.id(), "Condo #4",
                "Great views and updated appliances!", apartment1.numberOfBedrooms(),
                apartment1.numberOfBathrooms(), apartment1.state(), apartment1.city(), apartment1.squareFeet(),
                apartment1.monthlyRent(), null, apartment1.available(), apartment1.ownerId(),
                apartment1.renterId());

        HttpEntity<Apartment> requestEntity = new HttpEntity<>(updatedApartment);
        ResponseEntity<Void> updateResponse = testRestTemplate
                .exchange("/apartments", HttpMethod.PUT, requestEntity, Void.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Apartment retrievedApartment = testRestTemplate
                .getForEntity("/apartments/{id}", Apartment.class, updatedApartment.id()).getBody();
        Apartment expectedApartment = new Apartment(apartment1.id(), "Condo #4",
                "Great views and updated appliances!", apartment1.numberOfBedrooms(),
                apartment1.numberOfBathrooms(), apartment1.state(), apartment1.city(), apartment1.squareFeet(),
                apartment1.monthlyRent(), apartment1.dateListed(), apartment1.available(), apartment1.ownerId(),
                apartment1.renterId());
        assertApartmentsAreEqual(expectedApartment, retrievedApartment);
    }

    @Test
    public void testUpdateApartmentInvalidId() {
        Apartment apartment = new Apartment(0, "Main Street Condo",
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
    public void testUpdateApartmentNullId() {
        Apartment apartment = new Apartment(null, "Main Street Condo",
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
        Apartment updatedApartment = new Apartment(apartment1.id(), "Main Street Condo",
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
        Apartment updatedApartment = new Apartment(apartment1.id(), "Main Street Condo",
                "Great views and updated appliances!", 2,
                1, "NY", "New York", 800, 635000,
                null, true, user1.id(), 0);

        HttpEntity<Apartment> requestEntity = new HttpEntity<>(updatedApartment);
        ResponseEntity<String> updateResponse = testRestTemplate
                .exchange("/apartments", HttpMethod.PUT, requestEntity, String.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(updateResponse.getBody())
                .isEqualTo(String.format("User with id %s does not exist", updatedApartment.renterId()));
    }

    @Test
    public void testUpdateApartmentDuplicateRenter() {
        Apartment updatedApartment = new Apartment(apartment2.id(), "Comfy Studio",
                "Studio space in downtown Manhattan. Great location", 0,
                1, "NY", "New York", 400, 280000,
                null, true, user1.id(), user2.id());

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

    @Test
    public void testDeleteApartment() {
        ResponseEntity<Void> deleteResponse = testRestTemplate
                .exchange("/apartments/{id}", HttpMethod.DELETE, null, Void.class, apartment3.id());

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = testRestTemplate
                .getForEntity("/apartments/{id}", String.class, apartment3.id());

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody())
                .isEqualTo(String.format("Apartment with id %s does not exist", apartment3.id()));
    }

    @Test
    public void testDeleteApartmentInvalidId() {
        int invalidApartmentId = 0;

        ResponseEntity<String> deleteResponse = testRestTemplate.exchange("/apartments/{id}", HttpMethod.DELETE,
                null, String.class, invalidApartmentId);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(deleteResponse.getBody())
                .isEqualTo(String.format("Apartment with id %s does not exist", invalidApartmentId));
    }

    @Test
    public void testDeleteApartmentOccupiedApartment() {
        ResponseEntity<String> deleteResponse = testRestTemplate.exchange("/apartments/{id}", HttpMethod.DELETE,
                null, String.class, apartment1.id());

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(deleteResponse.getBody())
                .isEqualTo(String.format(
                        "Unable to delete apartment with id %s because it is occupied", apartment1.id()));
    }

    @Test
    public void testDeleteApartmentWithApplications() {
        Application app1 = new Application(null, null, true, false,
                user2.id(), apartment3.id());
        Application app2 = new Application(null, null, true, false,
                user3.id(), apartment3.id());
        Application application1 = testRestTemplate
                .postForEntity("/applications", app1, Application.class).getBody();
        Application application2 = testRestTemplate
                .postForEntity("/applications", app2, Application.class).getBody();

        List<Integer> applicationIdList = List.of(application1.id(), application2.id());

        for (Integer applicationId : applicationIdList) {
            ResponseEntity<Application> getResponse = testRestTemplate
                    .getForEntity("/applications/{id}", Application.class, applicationId);
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        ResponseEntity<Void> deleteResponse = testRestTemplate
                .exchange("/apartments/{id}", HttpMethod.DELETE, null, Void.class, apartment3.id());

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        for (Integer applicationId : applicationIdList) {
            ResponseEntity<String> getResponse = testRestTemplate
                    .getForEntity("/applications/{id}", String.class, applicationId);
            assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(getResponse.getBody())
                    .isEqualTo(String.format("Application with id %s does not exist", applicationId));
        }
    }
}
