package com.auger.apartments.applications;

import com.auger.apartments.BaseIntegrationTest;
import com.auger.apartments.apartments.Apartment;
import com.auger.apartments.users.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.auger.apartments.TestUtils.assertApplicationsAreEqual;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ApplicationRepositoryImplIntegrationTests extends BaseIntegrationTest {

    @Autowired
    ApplicationRepositoryImpl underTest;

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
        user1 = userService.createUser(u1);
        user2 = userService.createUser(u2);
        user3 = userService.createUser(u3);

        Apartment apt1 = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), null);
        Apartment apt2 = new Apartment(null, "Suburban Getaway",
                "Entire unit in a quiet neighborhood", 3,
                2, "VA", "Norfolk", 1400, 310000,
                null, true, user1.id(), null);

        apartment1 = apartmentService.createApartment(apt1);
        apartment2 = apartmentService.createApartment(apt2);

        Application app1 = new Application(null, null, true, false,
                user2.id(), apartment1.id());
        Application app2 = new Application(null, null, true, false,
                user2.id(), apartment2.id());
        Application app3 = new Application(null, null, true, false,
                user3.id(), apartment1.id());

        application1 = underTest.create(app1);
        application2 = underTest.create(app2);
        application3 = underTest.create(app3);
    }

    @AfterEach
    public void clearTables() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "applications");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "apartments");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
    }

    @Test
    public void testCreateAndFindOne() {
        Application application = new Application(null, null, true, false,
                user3.id(), apartment2.id());

        assertThat(getRowCount()).isEqualTo(3);
        Application createdApplication = underTest.create(application);
        assertThat(getRowCount()).isEqualTo(4);
        assertThat(createdApplication.id()).isNotNull();

        Optional<Application> optionalApplication = underTest.findOne(createdApplication.id());
        assertThat(optionalApplication).isPresent();
        Application retrievedApplication = optionalApplication.get();
        assertApplicationsAreEqual(createdApplication, retrievedApplication);
    }

    @Test
    public void testFindOneInvalidId() {
        Optional<Application> optionalApplication = underTest.findOne(0);
        assertThat(optionalApplication).isNotPresent();
    }

    @Test
    public void testFindAll() {
        Map<Integer, Application> applicationMap = new HashMap<>();
        applicationMap.put(application1.id(), application1);
        applicationMap.put(application2.id(), application2);
        applicationMap.put(application3.id(), application3);

        List<Application> applications = underTest.findAll();

        assertThat(getRowCount()).isEqualTo(applicationMap.size());
        assertThat(applications.size()).isEqualTo(applicationMap.size());

        for (Application application : applications) {
            Application createdApplication = applicationMap.get(application.id());
            assertApplicationsAreEqual(application, createdApplication);
            applicationMap.remove(application.id());
        }
        assertThat(applicationMap.size()).isZero();
    }

    @Test
    public void testUpdate() {
        Application updatedApplication = new Application(application1.id(), null, false,
                true, application1.userId(), application1.apartmentId());

        underTest.update(updatedApplication);

        assertThat(getRowCount()).isEqualTo(3);

        Optional<Application> optionalApplication = underTest.findOne(updatedApplication.id());
        assertThat(optionalApplication).isPresent();

        Application expectedApplication = new Application(application1.id(), application1.dateSubmitted(), false,
                true, application1.userId(), application1.apartmentId());
        Application retrievedApplication = optionalApplication.get();
        assertApplicationsAreEqual(expectedApplication, retrievedApplication);
    }

    @Test
    public void testExists() {
        assertThat(underTest.exists(0)).isFalse();
        assertThat(underTest.exists(application1.id())).isTrue();
    }

    @Test
    public void testDelete() {
        assertThat(getRowCount()).isEqualTo(3);
        assertThat(underTest.exists(application1.id())).isTrue();

        underTest.delete(application1.id());

        assertThat(getRowCount()).isEqualTo(2);
        assertThat(underTest.exists(application1.id())).isFalse();
    }

    private int getRowCount() {
        return JdbcTestUtils.countRowsInTable(jdbcTemplate, "applications");
    }
}
