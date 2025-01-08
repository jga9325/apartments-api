package com.auger.apartments.applications;

import com.auger.apartments.ControllerIntegrationTest;
import com.auger.apartments.apartments.Apartment;
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

import static com.auger.apartments.TestUtils.assertApplicationsAreEqual;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ApplicationControllerIntegrationTests extends ControllerIntegrationTest {

    private User user1;
    private User user2;
    private User user3;
    private Apartment apartment1;
    private Apartment apartment2;
    private Application application1;
    private Application application2;
    private Application application3;

    @BeforeEach
    public void addData() {
        User u1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), null);
        User u2 = new User(0, "Bob", "Daly", "bob@gmail.com",
                "8456320985", LocalDate.of(1994, 10, 11), null);
        User u3 = new User(null, "Jennifer", "Lilly", "jennifer@gmail.com",
                "1275643908", LocalDate.of(2001, 8, 15), null);
        user1 = testRestTemplate.postForEntity("/users", u1, User.class).getBody();
        user2 = testRestTemplate.postForEntity("/users", u2, User.class).getBody();
        user3 = testRestTemplate.postForEntity("/users", u3, User.class).getBody();

        Apartment apt1 = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), null);
        Apartment apt2 = new Apartment(null, "Suburban Getaway",
                "Entire unit in a quiet neighborhood", 3,
                2, "VA", "Norfolk", 1400, 310000,
                null, true, user1.id(), null);

        apartment1 = testRestTemplate.postForEntity("/apartments", apt1, Apartment.class)
                .getBody();
        apartment2 = testRestTemplate.postForEntity("/apartments", apt2, Apartment.class)
                .getBody();

        Application app1 = new Application(null, null, true, false,
                user2.id(), apartment1.id());
        Application app2 = new Application(null, null, true, false,
                user2.id(), apartment2.id());
        Application app3 = new Application(null, null, true, false,
                user3.id(), apartment1.id());

        application1 = testRestTemplate.postForEntity("/applications", app1, Application.class).getBody();
        application2 = testRestTemplate.postForEntity("/applications", app2, Application.class).getBody();
        application3 = testRestTemplate.postForEntity("/applications", app3, Application.class).getBody();
    }

    @AfterEach
    public void clearTables() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "applications");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "apartments");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
    }

    @Test
    public void testCreateApplicationAndGetApplication() {
        Application application = new Application(null, null, true, false,
                user3.id(), apartment2.id());
        ResponseEntity<Application> createResponse = testRestTemplate
                .postForEntity("/applications", application, Application.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        int applicationId = createResponse.getBody().id();
        assertThat(applicationId).isNotNull();

        ResponseEntity<Application> getResponse = testRestTemplate
                .getForEntity("/applications/{id}", Application.class, applicationId);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertApplicationsAreEqual(createResponse.getBody(), getResponse.getBody());
    }

    @Test
    public void testCreateApplicationInvalidUser() {
        Application application = new Application(null, null, true, false,
                0, apartment2.id());
        ResponseEntity<String> createResponse = testRestTemplate
                .postForEntity("/applications", application, String.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(createResponse.getBody())
                .isEqualTo(String.format("User with id %s does not exist", application.userId()));
    }

    @Test
    public void testCreateApplicationInvalidApartment() {
        Application application = new Application(null, null, true, false,
                user3.id(), 0);
        ResponseEntity<String> createResponse = testRestTemplate
                .postForEntity("/applications", application, String.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(createResponse.getBody())
                .isEqualTo(String.format("Apartment with id %s does not exist", application.apartmentId()));
    }

    @Test
    public void testGetApplicationInvalidId() {
        int invalidApplicationId = 0;

        ResponseEntity<String> getResponse = testRestTemplate
                .getForEntity("/applications/{id}", String.class, invalidApplicationId);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody())
                .isEqualTo(String.format("Application with id %s does not exist", invalidApplicationId));
    }

    @Test
    public void testGetAllApplications() {
        Map<Integer, Application> applicationMap = new HashMap<>();
        applicationMap.put(application1.id(), application1);
        applicationMap.put(application2.id(), application2);
        applicationMap.put(application3.id(), application3);

        ResponseEntity<List<Application>> getAllResponse = testRestTemplate
                .exchange("/applications", HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<Application>>() {});

        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllResponse.getBody().size()).isEqualTo(applicationMap.size());

        for (Application application : getAllResponse.getBody()) {
            Application createdApplication = applicationMap.get(application.id());
            assertApplicationsAreEqual(application, createdApplication);
            applicationMap.remove(application.id());
        }
        assertThat(applicationMap.size()).isZero();
    }

    @Test
    public void testUpdateApplication() {
        Application updatedApplication = new Application(application1.id(), null,
                false, true, application1.userId(), application1.apartmentId());

        HttpEntity<Application> requestEntity = new HttpEntity<>(updatedApplication);
        ResponseEntity<Void> updateResponse = testRestTemplate
                .exchange("/applications", HttpMethod.PUT, requestEntity, Void.class);
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Application retrievedApplication = testRestTemplate
                .getForEntity("/applications/{id}", Application.class, updatedApplication.id()).getBody();
        Application expectedApplication = new Application(application1.id(), application1.dateSubmitted(),
                false, true, application1.userId(), application1.apartmentId());
        assertApplicationsAreEqual(expectedApplication, retrievedApplication);
    }

    @Test
    public void testUpdateApplicationInvalidId() {
        Application updatedApplication = new Application(0, null,
                false, true, application1.userId(), application1.apartmentId());

        HttpEntity<Application> requestEntity = new HttpEntity<>(updatedApplication);
        ResponseEntity<String> updateResponse = testRestTemplate
                .exchange("/applications", HttpMethod.PUT, requestEntity, String.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(updateResponse.getBody())
                .isEqualTo(String.format("Application with id %s does not exist", updatedApplication.id()));
    }

    @Test
    public void testUpdateApplicationNullId() {
        Application updatedApplication = new Application(null, null,
                false, true, application1.userId(), application1.apartmentId());

        HttpEntity<Application> requestEntity = new HttpEntity<>(updatedApplication);
        ResponseEntity<String> updateResponse = testRestTemplate
                .exchange("/applications", HttpMethod.PUT, requestEntity, String.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(updateResponse.getBody())
                .isEqualTo(String.format("Application with id %s does not exist", updatedApplication.id()));
    }

    @Test
    public void testDeleteApplication() {
        ResponseEntity<Void> deleteResponse = testRestTemplate
                .exchange("/applications/{id}", HttpMethod.DELETE, null, Void.class, application1.id());

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getResponse = testRestTemplate
                .getForEntity("/applications/{id}", String.class, application1.id());

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody())
                .isEqualTo(String.format("Application with id %s does not exist", application1.id()));
    }

    @Test
    public void testDeleteApplicationInvalidId() {
        int invalidApplicationId = 0;

        ResponseEntity<String> deleteResponse = testRestTemplate.exchange("/applications/{id}", HttpMethod.DELETE,
                null, String.class, invalidApplicationId);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(deleteResponse.getBody())
                .isEqualTo(String.format("Application with id %s does not exist", invalidApplicationId));
    }
}
