package com.auger.apartments.apartments;

import com.auger.apartments.BaseIntegrationTest;
import com.auger.apartments.exceptions.DeleteApartmentException;
import com.auger.apartments.exceptions.DuplicateDataException;
import com.auger.apartments.exceptions.UserNotFoundException;
import com.auger.apartments.users.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class ApartmentValidatorIntegrationTests extends BaseIntegrationTest {

    @Autowired
    ApartmentValidator underTest;

    private User user1;
    private User user2;
    private User user3;
    private Apartment apartment1;
    private Apartment apartment2;
    private Apartment apartment3;

    @BeforeEach
    public void addData() {
        User u1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User u2 = new User(0, "Bob", "Daly", "bob@gmail.com",
                "1845363790", LocalDate.of(2000, 7, 14), LocalDate.now());
        User u3 = new User(0, "Stacy", "Jones", "stacy@gmail.com",
                "7654334298", LocalDate.of(2000, 7, 14), LocalDate.now());
        user1 = userService.createUser(u1);
        user2 = userService.createUser(u2);
        user3 = userService.createUser(u3);

        Apartment apt1 = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), user2.id());
        Apartment apt2 = new Apartment(null, "Comfy Studio",
                "Studio space in downtown Manhattan. Great location", 0,
                1, "NY", "New York", 400, 280000,
                null, true, user1.id(), user3.id());
        Apartment apt3 = new Apartment(null, "Beach Retreat",
                "Lovely escape from city life!", 2,
                2, "Fl", "Key West", 2100, 250000,
                null, true, user1.id(), null);
        apartment1 = apartmentRepository.create(apt1);
        apartment2 = apartmentRepository.create(apt2);
        apartment3 = apartmentRepository.create(apt3);
    }

    @AfterEach
    public void clearTables() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "apartments");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
    }

    @Test
    public void testVerifyUniqueRenterForNewApartment() {
        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user3.id(), user1.id());

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
        Apartment sameApartment = new Apartment(apartment2.id(), "Comfy Studio",
                "Studio space in downtown Manhattan. Great location", 0,
                1, "NY", "New York", 400, 280000,
                null, true, user1.id(), user3.id());
        assertThatNoException().isThrownBy(
                () -> underTest.verifyUniqueRenterForExistingApartment(sameApartment.id(), sameApartment.renterId())
        );

        Apartment duplicateRenterIdApartment = new Apartment(apartment2.id(), "Comfy Studio",
                "Studio space in downtown Manhattan. Great location", 0,
                1, "NY", "New York", 400, 280000,
                null, true, user1.id(), user2.id());
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
        Apartment nullRenterIdApartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), null);
        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user3.id(), user1.id());

        assertThatNoException().isThrownBy(() -> underTest.validateNewApartment(nullRenterIdApartment));
        assertThatNoException().isThrownBy(() -> underTest.validateNewApartment(apartment));
    }

    @Test
    public void testValidateNewApartmentDuplicateRenterId() {
        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), user2.id());

        assertThatThrownBy(() -> underTest.validateNewApartment(apartment))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, apartment.renterId()));
    }

    @Test
    public void testValidateExistingApartment() {
        Apartment updatedApartment = new Apartment(apartment1.id(), "Main Street Condo",
                "Best views in the city and in a great location!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), user2.id());
        Apartment nullRenterIdApartment = new Apartment(apartment1.id(), "Main Street Condo",
                "Best views in the city and in a great location!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), null);
        assertThatNoException().isThrownBy(() -> underTest.validateExistingApartment(updatedApartment));
        assertThatNoException().isThrownBy(() -> underTest.validateExistingApartment(nullRenterIdApartment));
    }

    @Test
    public void testValidateExistingApartmentDuplicateRenterId() {
        Apartment duplicateRenterIdApartment = new Apartment(apartment2.id(), "Comfy Studio",
                "Good location, great for students and young professionals", 0,
                1, "NY", "New York", 400, 280000,
                null, true, user1.id(), user2.id());
        assertThatThrownBy(() -> underTest.validateExistingApartment(duplicateRenterIdApartment))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, duplicateRenterIdApartment.renterId()));
    }

    @Test
    public void testVerifyOwnerExists() {
        int invalidOwnerId = 0;
        assertThatThrownBy(() -> underTest.verifyOwnerExists(invalidOwnerId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", invalidOwnerId));

        assertThatNoException().isThrownBy(() -> underTest.verifyOwnerExists(user1.id()));
    }

    @Test
    public void testVerifyRenterExists() {
        int invalidRenterId = 0;
        assertThatThrownBy(() -> underTest.verifyRenterExists(invalidRenterId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", invalidRenterId));

        assertThatNoException().isThrownBy(() -> underTest.verifyRenterExists(null));
        assertThatNoException().isThrownBy(() -> underTest.verifyRenterExists(user2.id()));
    }

    @Test
    public void testValidateNewApartmentInvalidOwner() {
        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 0, null);
        assertThatThrownBy(() -> underTest.validateNewApartment(apartment))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", apartment.ownerId()));
    }

    @Test
    public void testValidateNewApartmentInvalidRenter() {
        Apartment apartment = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), 0);
        assertThatThrownBy(() -> underTest.validateNewApartment(apartment))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", apartment.renterId()));
    }

    @Test
    public void testValidateExistingApartmentInvalidOwner() {
        Apartment updatedApartment = new Apartment(apartment1.id(), "Main Street Condo",
                "Great views!!!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 0, null);
        assertThatThrownBy(() -> underTest.validateExistingApartment(updatedApartment))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", updatedApartment.ownerId()));
    }

    @Test
    public void testValidateExistingApartmentInvalidRenter() {
        Apartment updatedApartment = new Apartment(apartment1.id(), "Main Street Condo",
                "Great views!!!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), 0);
        assertThatThrownBy(() -> underTest.validateExistingApartment(updatedApartment))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", updatedApartment.renterId()));
    }

    @Test
    public void testValidateApartmentDeletion() {
        assertThatNoException().isThrownBy(() -> underTest.validateApartmentDeletion(apartment3.id()));

        assertThatThrownBy(() -> underTest.validateApartmentDeletion(apartment2.id()))
                .isInstanceOf(DeleteApartmentException.class)
                .hasMessage(String.format("""
                    Unable to delete apartment with id %s because it is occupied
                    """, apartment2.id()));
    }
}
