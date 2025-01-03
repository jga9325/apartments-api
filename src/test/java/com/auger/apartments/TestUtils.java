package com.auger.apartments;

import com.auger.apartments.apartments.Apartment;
import com.auger.apartments.applications.Application;
import com.auger.apartments.users.User;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public final class TestUtils {

    private TestUtils() {
    }

    public static void assertUsersAreEqual(User u1, User u2) {
        assertThat(u1.id()).isEqualTo(u2.id());
        assertThat(u1.firstName()).isEqualTo(u2.firstName());
        assertThat(u1.lastName()).isEqualTo(u2.lastName());
        assertThat(u1.email()).isEqualTo(u2.email());
        assertThat(u1.phoneNumber()).isEqualTo(u2.phoneNumber());
        assertThat(u1.birthDate()).isEqualTo(u2.birthDate());
        assertThat(u1.dateJoined()).isEqualTo(u2.dateJoined());
    }

    public static void assertApartmentsAreEqual(Apartment a1, Apartment a2) {
        assertThat(a1.id()).isEqualTo(a2.id());
        assertThat(a1.title()).isEqualTo(a2.title());
        assertThat(a1.description()).isEqualTo(a2.description());
        assertThat(a1.numberOfBedrooms()).isEqualTo(a2.numberOfBedrooms());
        assertThat(a1.numberOfBathrooms()).isEqualTo(a2.numberOfBathrooms());
        assertThat(a1.state()).isEqualTo(a2.state());
        assertThat(a1.city()).isEqualTo(a2.city());
        assertThat(a1.squareFeet()).isEqualTo(a2.squareFeet());
        assertThat(a1.monthlyRent()).isEqualTo(a2.monthlyRent());
        assertThat(a1.dateListed()).isEqualTo(a2.dateListed());
        assertThat(a1.available()).isEqualTo(a2.available());
        assertThat(a1.ownerId()).isEqualTo(a2.ownerId());
        assertThat(a1.renterId()).isEqualTo(a2.renterId());
    }

    public static void assertApplicationsAreEqual(Application a1, Application a2) {
        assertThat(a1.id()).isEqualTo(a2.id());
        assertThat(a1.dateSubmitted()).isEqualTo(a2.dateSubmitted());
        assertThat(a1.active()).isEqualTo(a2.active());
        assertThat(a1.successful()).isEqualTo(a2.successful());
        assertThat(a1.userId()).isEqualTo(a2.userId());
        assertThat(a1.apartmentId()).isEqualTo(a2.apartmentId());
    }
}
