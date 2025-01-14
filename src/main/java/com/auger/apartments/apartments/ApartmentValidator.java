package com.auger.apartments.apartments;

import com.auger.apartments.exceptions.DeleteApartmentException;
import com.auger.apartments.exceptions.DuplicateDataException;
import com.auger.apartments.exceptions.UserNotFoundException;
import com.auger.apartments.users.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Provides methods to validate that an apartment meets database and business constraints before
 * executing database operations such as creating, updating, and deleting apartments.
 */
@Component
public class ApartmentValidator {

    private static final Logger logger = LoggerFactory.getLogger(ApartmentValidator.class);
    private final JdbcTemplate jdbcTemplate;
    private final UserService userService;

    public ApartmentValidator(JdbcTemplate jdbcTemplate, UserService userService) {
        this.jdbcTemplate = jdbcTemplate;
        this.userService = userService;
    }

    public void validateNewApartment(Apartment apartment) {
        logger.info("Validating new apartment");
        verifyOwnerExists(apartment.ownerId());
        verifyRenterExists(apartment.renterId());
        verifyUniqueRenterForNewApartment(apartment.renterId());
        logger.info("Validation complete");
    }

    public void validateExistingApartment(Apartment apartment) {
        logger.info("Validating existing apartment");
        verifyOwnerExists(apartment.ownerId());
        verifyRenterExists(apartment.renterId());
        verifyUniqueRenterForExistingApartment(apartment.id(), apartment.renterId());
        logger.info("Validation complete");
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

    public void validateApartmentDeletion(int apartmentId) {
        logger.info("Validating apartment can be deleted");
        verifyApartmentIsVacant(apartmentId);
        logger.info("Validation complete");
    }

    private void verifyApartmentIsVacant(int apartmentId) {
        String sql = """
                SELECT COUNT(*)
                FROM apartments
                WHERE id = ?
                AND renter_id IS NOT NULL;
                """;
        int occupiedApartments = jdbcTemplate.queryForObject(sql, Integer.class, apartmentId);
        if (occupiedApartments > 0) {
            throw new DeleteApartmentException(
                    String.format("Unable to delete apartment with id %s because it is occupied", apartmentId)
            );
        }
    }
}
