package com.auger.apartments.apartments;

import com.auger.apartments.exceptions.DuplicateDataException;
import com.auger.apartments.exceptions.UserNotFoundException;
import com.auger.apartments.users.UserService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ApartmentValidator {

    private final JdbcTemplate jdbcTemplate;
    private final UserService userService;

    public ApartmentValidator(JdbcTemplate jdbcTemplate, UserService userService) {
        this.jdbcTemplate = jdbcTemplate;
        this.userService = userService;
    }

    public void validateNewApartment(Apartment apartment) {
        verifyOwnerExists(apartment.ownerId());
        verifyRenterExists(apartment.renterId());
        verifyUniqueRenterForNewApartment(apartment.renterId());
    }

    public void validateExistingApartment(Apartment apartment) {
        verifyOwnerExists(apartment.ownerId());
        verifyRenterExists(apartment.renterId());
        verifyUniqueRenterForExistingApartment(apartment.id(), apartment.renterId());
    }

    public void verifyUniqueRenterForNewApartment(Integer renterId) {
        String sql = """
                SELECT COUNT(*)
                FROM apartments
                WHERE renter_id = ?;
                """;
        int duplicateRenterIdCount = jdbcTemplate.queryForObject(sql, Integer.class, renterId);
        if (duplicateRenterIdCount > 0) {
            throw new DuplicateDataException(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, renterId));
        }
    }

    public void verifyUniqueRenterForExistingApartment(int id, Integer renterId) {
        String sql = """
                SELECT COUNT(*)
                FROM apartments
                WHERE id != ?
                AND renter_id = ?;
                """;
        int duplicateRenterIdCount = jdbcTemplate.queryForObject(sql, Integer.class, id, renterId);
        if (duplicateRenterIdCount > 0) {
            throw new DuplicateDataException(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, renterId));
        }
    }

    public void verifyOwnerExists(int ownerId) {
        if(!userService.doesExist(ownerId)) {
            throw new UserNotFoundException(String.format("User with id %s does not exist", ownerId));
        }
    }

    public void verifyRenterExists(Integer renterId) {
        if(renterId != null && !userService.doesExist(renterId)) {
            throw new UserNotFoundException(String.format("User with id %s does not exist", renterId));
        }
    }
}
