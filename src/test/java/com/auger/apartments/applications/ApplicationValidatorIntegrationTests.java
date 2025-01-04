package com.auger.apartments.applications;

import com.auger.apartments.apartments.Apartment;
import com.auger.apartments.apartments.ApartmentService;
import com.auger.apartments.exceptions.ApartmentNotFoundException;
import com.auger.apartments.exceptions.UserNotFoundException;
import com.auger.apartments.users.User;
import com.auger.apartments.users.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Testcontainers
@SpringBootTest
public class ApplicationValidatorIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2-alpine");

    @Autowired
    ApplicationValidator underTest;

    @Autowired
    UserService userService;

    @Autowired
    ApartmentService apartmentService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private User user1;
    private Apartment apartment1;

    @BeforeEach
    public void setUp() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "applications");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "apartments");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");

        User u1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), null);
        user1 = userService.createUser(u1);

        Apartment apt1 = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), null);
        apartment1 = apartmentService.createApartment(apt1);
    }

    @Test
    public void testVerifyUserExists() {
        int invalidUserId = 0;
        assertThatThrownBy(() -> underTest.verifyUserExists(invalidUserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", invalidUserId));
        assertThatNoException().isThrownBy(() -> underTest.verifyUserExists(user1.id()));
    }

    @Test
    public void testVerifyApartmentExists() {
        int invalidApartmentId = 0;
        assertThatThrownBy(() -> underTest.verifyApartmentExists(invalidApartmentId))
                .isInstanceOf(ApartmentNotFoundException.class)
                .hasMessage(String.format("Apartment with id %s does not exist", invalidApartmentId));
        assertThatNoException().isThrownBy(() -> underTest.verifyApartmentExists(apartment1.id()));
    }

    @Test
    public void testValidateNewApplication() {
        Application application =
                new Application(null, null, true, false, user1.id(), apartment1.id());

        assertThatNoException().isThrownBy(() -> underTest.validateNewApplication(application));
    }

    @Test
    public void testValidateNewApplicationInvalidUser() {
        Application application =
                new Application(null, null, true, false, 0, apartment1.id());

        assertThatThrownBy(() -> underTest.validateNewApplication(application))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", application.userId()));
    }

    @Test
    public void testValidateNewApplicationInvalidApartment() {
        Application application =
                new Application(null, null, true, false, user1.id(), 0);

        assertThatThrownBy(() -> underTest.validateNewApplication(application))
                .isInstanceOf(ApartmentNotFoundException.class)
                .hasMessage(String.format("Apartment with id %s does not exist", application.apartmentId()));
    }
}