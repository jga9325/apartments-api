package com.auger.apartments.apartments;

import com.auger.apartments.IntegrationTest;
import com.auger.apartments.applications.Application;
import com.auger.apartments.users.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.auger.apartments.TestUtils.assertApartmentsAreEqual;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ApartmentRepositoryImplIntegrationTests extends IntegrationTest {

    @Autowired
    ApartmentRepositoryImpl underTest;

    private User user1;
    private User user2;
    private User user3;
    private Apartment apartment1;
    private Apartment apartment2;
    private Apartment apartment3;

    @BeforeEach
    public void addData() {
        User u1 = new User(null, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28),
                null);
        User u2 = new User(null, "Bob", "Daly", "bob@gmail.com",
                "7564839402", LocalDate.of(1985, 2, 7), null);
        User u3 = new User(null, "Jennifer", "Lilly", "jennifer@gmail.com",
                "1275643908", LocalDate.of(2001, 8, 15), null);
        user1 = userService.createUser(u1);
        user2 = userService.createUser(u2);
        user3 = userService.createUser(u3);

        Apartment apt1 = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user1.id(), null);
        Apartment apt2 = new Apartment(null, "Suburban Getaway",
                "Entire unit in a quiet neighborhood", 3,
                2, "VA", "Norfolk", 1400, 310000,
                null, true, user1.id(), null);
        Apartment apt3 = new Apartment(null, "Beach Apartment",
                "One bed one bath apartment near the beach!", 1,
                1, "FL", "Miami", 800, 185000,
                null, true, user1.id(), null);
        apartment1 = underTest.create(apt1);
        apartment2 = underTest.create(apt2);
        apartment3 = underTest.create(apt3);
    }

    @AfterEach
    public void clearTables() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "apartments");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
    }

    @Test
    public void testCreateAndFindOne() {
        Apartment apartment = new Apartment(null, "Downtown Studio",
                "Affordable apartment for students and young professionals", 0,
                1, "IL", "Chicago", 450, 95000,
                null, true, user1.id(), null);

        assertThat(getRowCount()).isEqualTo(3);
        Apartment createdApartment = underTest.create(apartment);
        assertThat(getRowCount()).isEqualTo(4);
        assertThat(createdApartment.id()).isNotNull();

        Optional<Apartment> optionalApartment = underTest.findOne(createdApartment.id());
        assertThat(optionalApartment).isPresent();
        Apartment retrievedApartment = optionalApartment.get();
        assertApartmentsAreEqual(createdApartment, retrievedApartment);
    }

    @Test
    public void testFindOneInvalidId() {
        Optional<Apartment> optionalApartment = underTest.findOne(0);
        assertThat(optionalApartment).isNotPresent();
    }

    @Test
    public void testFindAll() {
        Map<Integer, Apartment> apartmentMap = new HashMap<>();
        apartmentMap.put(apartment1.id(), apartment1);
        apartmentMap.put(apartment2.id(), apartment2);
        apartmentMap.put(apartment3.id(), apartment3);

        List<Apartment> apartments = underTest.findAll();

        assertThat(getRowCount()).isEqualTo(apartmentMap.size());
        assertThat(apartments.size()).isEqualTo(apartmentMap.size());

        for (Apartment apartment : apartments) {
            Apartment createdApartment = apartmentMap.get(apartment.id());
            assertApartmentsAreEqual(apartment, createdApartment);
            apartmentMap.remove(apartment.id());
        }
        assertThat(apartmentMap.size()).isZero();
    }

    @Test
    public void testUpdate() {
        Apartment updatedApartment = new Apartment(apartment1.id(), apartment1.title(),
                "Spacious, brand new appliances, new building, best views in the city!",
                apartment1.numberOfBedrooms(), apartment1.numberOfBathrooms(),
                apartment1.state(), apartment1.city(), apartment1.squareFeet(),
                675000, null, apartment1.available(),
                apartment1.ownerId(), apartment1.renterId());

        underTest.update(updatedApartment);

        assertThat(getRowCount()).isEqualTo(3);

        Optional<Apartment> optionalApartment = underTest.findOne(updatedApartment.id());
        assertThat(optionalApartment).isPresent();

        Apartment expectedApartment = new Apartment(apartment1.id(), apartment1.title(),
                "Spacious, brand new appliances, new building, best views in the city!",
                apartment1.numberOfBedrooms(), apartment1.numberOfBathrooms(),
                apartment1.state(), apartment1.city(), apartment1.squareFeet(),
                675000, apartment1.dateListed(), apartment1.available(),
                apartment1.ownerId(), apartment1.renterId());
        Apartment retrievedApartment = optionalApartment.get();
        assertApartmentsAreEqual(retrievedApartment, expectedApartment);
    }

    @Test
    public void testExists() {
        assertThat(underTest.exists(0)).isFalse();
        assertThat(underTest.exists(apartment1.id())).isTrue();
    }

    @Test
    public void testDelete() {
        assertThat(getRowCount()).isEqualTo(3);
        assertThat(underTest.exists(apartment1.id())).isTrue();

        underTest.delete(apartment1.id());

        assertThat(getRowCount()).isEqualTo(2);
        assertThat(underTest.exists(apartment1.id())).isFalse();
    }

    @Test
    public void testDeleteWithApplications() {
        Application app1 = new Application(null, null, true, false,
                user2.id(), apartment1.id());
        Application app2 = new Application(null, null, true, false,
                user3.id(), apartment1.id());
        applicationService.createApplication(app1);
        applicationService.createApplication(app2);

        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "applications"))
                .isEqualTo(2);

        underTest.delete(apartment1.id());

        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "applications"))
                .isEqualTo(0);
    }

    private int getRowCount() {
        return JdbcTestUtils.countRowsInTable(jdbcTemplate, "apartments");
    }
}
