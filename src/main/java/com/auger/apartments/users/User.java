package com.auger.apartments.users;

import java.time.LocalDate;

public record User(int id, String name, String email, String phoneNumber, LocalDate birthDate, LocalDate dateJoined) {

    public User {
        if (phoneNumber.length() != 10) {
            throw new IllegalArgumentException("Phone number must be in the format 1234567890");
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
        }
        if (birthDate.isAfter(latestDate)) {
            throw new IllegalArgumentException("Must be 18 years of age");
        }
    }
}
