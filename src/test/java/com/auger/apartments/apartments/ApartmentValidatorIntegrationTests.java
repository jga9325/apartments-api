package com.auger.apartments.apartments;

import com.auger.apartments.exceptions.DuplicateDataException;
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

import static org.assertj.core.api.AssertionsForClassTypes.*;

@Testcontainers
@SpringBootTest
public class ApartmentValidatorIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2-alpine");

    @Autowired
    ApartmentValidator underTest;

    @Autowired
    ApartmentRepository apartmentRepository;

    @Autowired
    UserService userService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void clearTable() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "apartments");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
    }

    @Test
    public void testVerifyUniqueRenterForNewApartment() {
        User user1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "Daly", "bob@gmail.com",
                "1845363790", LocalDate.of(2000, 7, 14), LocalDate.now());
        User createdUser1 = userService.createUser(user1);
        User createdUser2 = userService.createUser(user2);

        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), createdUser2.id());

        assertThatNoException().isThrownBy(() -> underTest.verifyUniqueRenterForNewApartment(apartment.renterId()));
        apartmentRepository.create(apartment);
        assertThatThrownBy(() -> underTest.verifyUniqueRenterForNewApartment(apartment.renterId()))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, apartment.renterId()));
    }

    @Test
    public void testVerifyUniqueRenterForExistingApartment() {
        User user1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "Daly", "bob@gmail.com",
                "1845363790", LocalDate.of(2000, 7, 14), LocalDate.now());
        User user3 = new User(0, "Stacy", "Jones", "stacy@gmail.com",
                "7654334298", LocalDate.of(2000, 7, 14), LocalDate.now());
        User createdUser1 = userService.createUser(user1);
        User createdUser2 = userService.createUser(user2);
        User createdUser3 = userService.createUser(user3);

        Apartment apartment1 = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), createdUser2.id());
        Apartment apartment2 = new Apartment(null, "Comfy Studio",
                "Studio space in downtown Manhattan. Great location", 0,
                1, "NY", "New York", 400, 280000,
                null, true, createdUser1.id(), createdUser3.id());
        apartmentRepository.create(apartment1);
        Apartment createdApartment = apartmentRepository.create(apartment2);

        Apartment sameApartment = new Apartment(createdApartment.id(), "Comfy Studio",
                "Studio space in downtown Manhattan. Great location", 0,
                1, "NY", "New York", 400, 280000,
                null, true, createdUser1.id(), createdUser3.id());
        assertThatNoException().isThrownBy(
                () -> underTest.verifyUniqueRenterForExistingApartment(sameApartment.id(), sameApartment.renterId())
        );

        Apartment duplicateRenterIdApartment = new Apartment(createdApartment.id(), "Comfy Studio",
                "Studio space in downtown Manhattan. Great location", 0,
                1, "NY", "New York", 400, 280000,
                null, true, createdUser1.id(), createdUser2.id());
        assertThatThrownBy(() -> underTest.verifyUniqueRenterForExistingApartment(
                duplicateRenterIdApartment.id(), duplicateRenterIdApartment.renterId())
        ).isInstanceOf(DuplicateDataException.class)
                .hasMessage(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, duplicateRenterIdApartment.renterId()));
    }

    @Test
    public void testValidateNewApartment() {
        User user1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "Daly", "bob@gmail.com",
                "1845363790", LocalDate.of(2000, 7, 14), LocalDate.now());

        User createdUser1 = userService.createUser(user1);
        User createdUser2 = userService.createUser(user2);

        Apartment nullRenterIdApartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), null);
        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), createdUser2.id());

        assertThatNoException().isThrownBy(() -> underTest.validateNewApartment(nullRenterIdApartment));
        assertThatNoException().isThrownBy(() -> underTest.validateNewApartment(apartment));
    }

    @Test
    public void testValidateNewApartmentDuplicateRenterId() {
        User user1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "Daly", "bob@gmail.com",
                "1845363790", LocalDate.of(2000, 7, 14), LocalDate.now());

        User createdUser1 = userService.createUser(user1);
        User createdUser2 = userService.createUser(user2);

        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), createdUser2.id());

        apartmentRepository.create(apartment);

        assertThatThrownBy(() -> underTest.validateNewApartment(apartment))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, apartment.renterId()));
    }

    @Test
    public void testValidateExistingApartment() {
        User user1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "Daly", "bob@gmail.com",
                "1845363790", LocalDate.of(2000, 7, 14), LocalDate.now());
        User createdUser1 = userService.createUser(user1);
        User createdUser2 = userService.createUser(user2);

        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), createdUser2.id());
        Apartment createdApartment = apartmentRepository.create(apartment);

        Apartment updatedApartment = new Apartment(createdApartment.id(), "Main Street Condo",
                "Best views in the city and in a great location!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), createdUser2.id());
        Apartment nullRenterIdApartment = new Apartment(createdApartment.id(), "Main Street Condo",
                "Best views in the city and in a great location!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), null);
        assertThatNoException().isThrownBy(() -> underTest.validateExistingApartment(updatedApartment));
        assertThatNoException().isThrownBy(() -> underTest.validateExistingApartment(nullRenterIdApartment));
    }

    @Test
    public void testValidateExistingApartmentDuplicateRenterId() {
        User user1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "Daly", "bob@gmail.com",
                "1845363790", LocalDate.of(2000, 7, 14), LocalDate.now());
        User user3 = new User(0, "Stacy", "Jones", "stacy@gmail.com",
                "7654334298", LocalDate.of(2000, 7, 14), LocalDate.now());
        User createdUser1 = userService.createUser(user1);
        User createdUser2 = userService.createUser(user2);
        User createdUser3 = userService.createUser(user3);

        Apartment apartment1 = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), createdUser2.id());
        Apartment apartment2 = new Apartment(null, "Comfy Studio",
                "Studio space in downtown Manhattan. Great location", 0,
                1, "NY", "New York", 400, 280000,
                null, true, createdUser1.id(), createdUser3.id());
        apartmentRepository.create(apartment1);
        Apartment createdApartment = apartmentRepository.create(apartment2);

        Apartment duplicateRenterIdApartment = new Apartment(createdApartment.id(), "Comfy Studio",
                "Good location, great for students and young professionals", 0,
                1, "NY", "New York", 400, 280000,
                null, true, createdUser1.id(), createdUser2.id());
        assertThatThrownBy(() -> underTest.validateExistingApartment(duplicateRenterIdApartment))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, duplicateRenterIdApartment.renterId()));
    }

    @Test
    public void testVerifyOwnerExists() {
        int invalidOwnerId = 1;
        assertThatThrownBy(() -> underTest.verifyOwnerExists(invalidOwnerId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", invalidOwnerId));

        User user = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User createdUser = userService.createUser(user);

        assertThatNoException().isThrownBy(() -> underTest.verifyOwnerExists(createdUser.id()));
    }

    @Test
    public void testVerifyRenterExists() {
        int invalidRenterId = 1;
        assertThatThrownBy(() -> underTest.verifyRenterExists(invalidRenterId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", invalidRenterId));

        assertThatNoException().isThrownBy(() -> underTest.verifyRenterExists(null));

        User user = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User createdUser = userService.createUser(user);

        assertThatNoException().isThrownBy(() -> underTest.verifyRenterExists(createdUser.id()));
    }

    @Test
    public void testValidateNewApartmentInvalidOwner() {
        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, null);
        assertThatThrownBy(() -> underTest.validateNewApartment(apartment))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", apartment.ownerId()));
    }

    @Test
    public void testValidateNewApartmentInvalidRenter() {
        User user1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User createdUser1 = userService.createUser(user1);

        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), 0);
        assertThatThrownBy(() -> underTest.validateNewApartment(apartment))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", apartment.renterId()));
    }

    @Test
    public void testValidateExistingApartmentInvalidOwner() {
        User user1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User createdUser1 = userService.createUser(user1);

        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), null);
        Apartment createdApartment = apartmentRepository.create(apartment);

        Apartment updatedApartment = new Apartment(createdApartment.id(), "Main Street Condo",
                "Great views!!!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 0, null);
        assertThatThrownBy(() -> underTest.validateExistingApartment(updatedApartment))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", updatedApartment.ownerId()));
    }

    @Test
    public void testValidateExistingApartmentInvalidRenter() {
        User user1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User createdUser1 = userService.createUser(user1);

        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), null);
        Apartment createdApartment = apartmentRepository.create(apartment);

        Apartment updatedApartment = new Apartment(createdApartment.id(), "Main Street Condo",
                "Great views!!!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), 0);
        assertThatThrownBy(() -> underTest.validateExistingApartment(updatedApartment))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", updatedApartment.renterId()));
    }
}
