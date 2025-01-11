package com.auger.apartments.applications;

import java.time.LocalDate;

/**
 * Represents a user's application for an apartment
 *
 * @param id
 * @param dateSubmitted
 * @param active
 * @param successful
 * @param userId must reference an existing user
 * @param apartmentId must reference an existing apartment
 */
public record Application(Integer id,
                          LocalDate dateSubmitted,
                          boolean active,
                          boolean successful,
                          int userId,
                          int apartmentId) {}
