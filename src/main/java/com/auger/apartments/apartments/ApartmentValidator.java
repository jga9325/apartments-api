package com.auger.apartments.apartments;

import com.auger.apartments.exceptions.DuplicateDataException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ApartmentValidator {

    private final JdbcTemplate jdbcTemplate;

    public ApartmentValidator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void verifyNewApartment(Apartment apartment) {
        verifyRenterIdForNewApartment(apartment.renterId());
    }

    public void verifyExistingApartment(Apartment apartment) {
        verifyRenterIdForExistingApartment(apartment.id(), apartment.renterId());
    }

    public void verifyRenterIdForNewApartment(int renterId) {
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

    public void verifyRenterIdForExistingApartment(int id, int renterId) {
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
}
