package com.auger.apartments.users;

import com.auger.apartments.exceptions.DuplicateDataException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    private final JdbcTemplate jdbcTemplate;

    public UserValidator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void validateNewUser(User user) {
        verifyUniqueEmailForNewUser(user.email());
        verifyUniquePhoneNumberForNewUser(user.phoneNumber());
    }

    public void validateExistingUser(User user) {
        verifyUniqueEmailForExistingUser(user.id(), user.email());
        verifyUniquePhoneNumberForExistingUser(user.id(), user.phoneNumber());
    }

    public void verifyUniqueEmailForNewUser(String email) {
        String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE email = ?;
                """;
        int duplicateEmailCount = jdbcTemplate.queryForObject(sql, Integer.class, email);
        if (duplicateEmailCount > 0) {
            throw new DuplicateDataException("A user with that email already exists");
        }
    }

    public void verifyUniqueEmailForExistingUser(int id, String email) {
        String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE id != ?
                AND email = ?;
                """;
        int duplicateEmailCount = jdbcTemplate.queryForObject(sql, Integer.class, id, email);
        if (duplicateEmailCount > 0) {
            throw new DuplicateDataException("A user with that email already exists");
        }
    }

    public void verifyUniquePhoneNumberForNewUser(String phoneNumber) {
        String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE phone_number = ?;
                """;
        int duplicatePhoneNumberCount = jdbcTemplate.queryForObject(sql, Integer.class, phoneNumber);
        if (duplicatePhoneNumberCount > 0) {
            throw new DuplicateDataException("A user with that phone number already exists");
        }
    }

    public void verifyUniquePhoneNumberForExistingUser(int id, String phoneNumber) {
        String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE id != ?
                AND phone_number = ?;
                """;
        int duplicatePhoneNumberCount = jdbcTemplate.queryForObject(sql, Integer.class, id, phoneNumber);
        if (duplicatePhoneNumberCount > 0) {
            throw new DuplicateDataException("A user with that phone number already exists");
        }
    }
}
