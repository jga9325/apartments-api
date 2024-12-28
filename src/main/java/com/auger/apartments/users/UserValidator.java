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

    public void verifyNewUser(User user) {
        verifyEmailForNewUser(user.email());
        verifyPhoneNumberForNewUser(user.phoneNumber());
    }

    public void verifyExistingUser(User user) {
        verifyEmailForExistingUser(user.id(), user.email());
        verifyPhoneNumberForExistingUser(user.id(), user.phoneNumber());
    }

    public void verifyEmailForNewUser(String email) {
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

    public void verifyEmailForExistingUser(int id, String email) {
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

    public void verifyPhoneNumberForNewUser(String phoneNumber) {
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

    public void verifyPhoneNumberForExistingUser(int id, String phoneNumber) {
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
