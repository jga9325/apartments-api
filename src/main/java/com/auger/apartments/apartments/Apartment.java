package com.auger.apartments.apartments;

import java.time.LocalDate;

/**
 * Represents an apartment
 *
 * @param id
 * @param title
 * @param description
 * @param numberOfBedrooms
 * @param numberOfBathrooms
 * @param state
 * @param city
 * @param squareFeet
 * @param monthlyRent in cents
 * @param dateListed
 * @param available
 * @param ownerId must reference an existing user
 * @param renterId must reference an existing user or be null to represent an unoccupied apartment
 */
public record Apartment(Integer id,
                        String title,
                        String description,
                        int numberOfBedrooms,
                        int numberOfBathrooms,
                        String state,
                        String city,
                        int squareFeet,
                        int monthlyRent,
                        LocalDate dateListed,
                        boolean available,
                        int ownerId,
                        Integer renterId) {

    public Apartment {
        if (numberOfBedrooms < 0) {
            throw new IllegalArgumentException("Number of bedrooms must be greater than or equal to zero");
        } else if (numberOfBathrooms <= 0) {
            throw new IllegalArgumentException("Number of bathrooms must be greater than zero");
        } else if (state == null) {
            throw new IllegalArgumentException("State is required");
        } else if (city == null) {
            throw new IllegalArgumentException("City is required");
        } else if (squareFeet <= 0) {
            throw new IllegalArgumentException("Square footage must be greater than zero");
        } else if (monthlyRent <= 0) {
            throw new IllegalArgumentException("Monthly rent must be greater than zero");
        }
    }
}
