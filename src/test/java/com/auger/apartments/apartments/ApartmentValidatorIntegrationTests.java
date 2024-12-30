package com.auger.apartments.apartments;

import com.auger.apartments.exceptions.DuplicateDataException;
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
    public void testVerifyRenterIdForNewApartment() {
        User user1 = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "bob@gmail.com", "1845363790",
                LocalDate.of(2000, 7, 14), LocalDate.now());
        User createdUser1 = userService.createUser(user1);
        User createdUser2 = userService.createUser(user2);

        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), createdUser2.id());

        assertThatNoException().isThrownBy(() -> underTest.verifyRenterIdForNewApartment(apartment.renterId()));
        apartmentRepository.createApartment(apartment);
        assertThatThrownBy(() -> underTest.verifyRenterIdForNewApartment(apartment.renterId()))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, apartment.renterId()));
    }

    @Test
    public void testVerifyRenterIdForExistingApartment() {
        User user1 = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "bob@gmail.com", "1845363790",
                LocalDate.of(2000, 7, 14), LocalDate.now());
        User user3 = new User(0, "Stacy", "stacy@gmail.com", "7654334298",
                LocalDate.of(2000, 7, 14), LocalDate.now());
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
        apartmentRepository.createApartment(apartment1);
        Apartment createdApartment = apartmentRepository.createApartment(apartment2);

        Apartment sameApartment = new Apartment(createdApartment.id(), "Comfy Studio",
                "Studio space in downtown Manhattan. Great location", 0,
                1, "NY", "New York", 400, 280000,
                null, true, createdUser1.id(), createdUser3.id());
        assertThatNoException().isThrownBy(
                () -> underTest.verifyRenterIdForExistingApartment(sameApartment.id(), sameApartment.renterId())
        );

        Apartment duplicateRenterIdApartment = new Apartment(createdApartment.id(), "Comfy Studio",
                "Studio space in downtown Manhattan. Great location", 0,
                1, "NY", "New York", 400, 280000,
                null, true, createdUser1.id(), createdUser2.id());
        assertThatThrownBy(() -> underTest.verifyRenterIdForExistingApartment(
                duplicateRenterIdApartment.id(), duplicateRenterIdApartment.renterId())
        ).isInstanceOf(DuplicateDataException.class)
                .hasMessage(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, duplicateRenterIdApartment.renterId()));
    }

    @Test
    public void testVerifyNewApartment() {
        User user1 = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "bob@gmail.com", "1845363790",
                LocalDate.of(2000, 7, 14), LocalDate.now());

        User createdUser1 = userService.createUser(user1);
        User createdUser2 = userService.createUser(user2);

        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), createdUser2.id());

        assertThatNoException().isThrownBy(() -> underTest.verifyNewApartment(apartment));
    }

    @Test
    public void testVerifyNewApartmentDuplicateRenterId() {
        User user1 = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "bob@gmail.com", "1845363790",
                LocalDate.of(2000, 7, 14), LocalDate.now());

        User createdUser1 = userService.createUser(user1);
        User createdUser2 = userService.createUser(user2);

        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), createdUser2.id());

        apartmentRepository.createApartment(apartment);

        assertThatThrownBy(() -> underTest.verifyNewApartment(apartment))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, apartment.renterId()));
    }

    @Test
    public void testVerifyExistingApartment() {
        User user1 = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "bob@gmail.com", "1845363790",
                LocalDate.of(2000, 7, 14), LocalDate.now());
        User createdUser1 = userService.createUser(user1);
        User createdUser2 = userService.createUser(user2);

        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), createdUser2.id());
        Apartment createdApartment = apartmentRepository.createApartment(apartment);

        Apartment updatedApartment = new Apartment(createdApartment.id(), "Main Street Condo",
                "Best views in the city and in a great location!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, createdUser1.id(), createdUser2.id());
        assertThatNoException().isThrownBy(() -> underTest.verifyExistingApartment(updatedApartment));
    }

    @Test
    public void testVerifyExistingApartmentDuplicateRenterId() {
        User user1 = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "bob@gmail.com", "1845363790",
                LocalDate.of(2000, 7, 14), LocalDate.now());
        User user3 = new User(0, "Stacy", "stacy@gmail.com", "7654334298",
                LocalDate.of(2000, 7, 14), LocalDate.now());
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
        apartmentRepository.createApartment(apartment1);
        Apartment createdApartment = apartmentRepository.createApartment(apartment2);

        Apartment duplicateRenterIdApartment = new Apartment(createdApartment.id(), "Comfy Studio",
                "Good location, great for students and young professionals", 0,
                1, "NY", "New York", 400, 280000,
                null, true, createdUser1.id(), createdUser2.id());
        assertThatThrownBy(() -> underTest.verifyExistingApartment(duplicateRenterIdApartment))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, duplicateRenterIdApartment.renterId()));
    }

}
