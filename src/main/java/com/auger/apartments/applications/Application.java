package com.auger.apartments.applications;

import java.time.LocalDate;

public record Application(Integer id,
                          LocalDate dateSubmitted,
                          boolean active,
                          boolean successful,
                          int userId,
                          int apartmentId) {}
