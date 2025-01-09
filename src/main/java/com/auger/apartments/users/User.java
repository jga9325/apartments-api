package com.auger.apartments.users;

import java.time.LocalDate;

/**
 * Represents a user
 *
 * @param id
 * @param firstName
 * @param lastName
 * @param email must be unique
 * @param phoneNumber must be unique
 * @param birthDate
 * @param dateJoined
 */
public record User(Integer id,
                   String firstName,
                   String lastName,
                   String email,
                   String phoneNumber,
                   LocalDate birthDate,
                   LocalDate dateJoined) {

    public User {
        if (firstName == null) {
            throw new IllegalArgumentException("First name must be provided");
        } else if (lastName == null) {
            throw new IllegalArgumentException("Last name must be provided");
        } else if (email == null) {
            throw new IllegalArgumentException("A valid email must be provided");
        } else if (phoneNumber == null) {
            throw new IllegalArgumentException("A valid phone number must be provided");
        } else if (phoneNumber.length() != 10) {
            throw new IllegalArgumentException("Phone number must be in the format 1234567890");
        } else if (birthDate == null) {
            throw new IllegalArgumentException("A valid birth date must be provided");
        }

        for (int i = 0; i < phoneNumber.length(); i++) {
            if (!Character.isDigit(phoneNumber.charAt(i))) {
                throw new IllegalArgumentException("Phone number must be in the format 1234567890");
            }
        }

        LocalDate earliestDate = LocalDate.now().minusYears(100);
        LocalDate latestDate = LocalDate.now().minusYears(18);
        if (birthDate.isBefore(earliestDate)) {
            throw new IllegalArgumentException("Age limit exceeded");
        } else if (birthDate.isAfter(latestDate)) {
            throw new IllegalArgumentException("Must be at least 18 years of age");
        }
    }
}
