package com.auger.apartments.apartments;

import com.auger.apartments.users.User;
import com.auger.apartments.users.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@SpringBootTest
public class ApartmentRepositoryImplIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2-alpine");

    @Autowired
    ApartmentRepositoryImpl underTest;

    @Autowired
    UserService userService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private User user;
    private Apartment apartment1;
    private Apartment apartment2;
    private Apartment apartment3;

    @BeforeEach
    public void prepareTable() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "apartments");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");

        User newUser = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        user = userService.createUser(newUser);

        Apartment apt1 = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, user.id(), null);
        Apartment apt2 = new Apartment(null, "Suburban Getaway",
                "Entire unit in a quiet neighborhood", 3,
                2, "VA", "Norfolk", 1400, 310000,
                null, true, user.id(), null);
        Apartment apt3 = new Apartment(null, "Beach Apartment",
                "One bed one bath apartment near the beach!", 1,
                1, "FL", "Miami", 800, 185000,
                null, true, user.id(), null);
        apartment1 = underTest.createApartment(apt1);
        apartment2 = underTest.createApartment(apt2);
        apartment3 = underTest.createApartment(apt3);
    }

    @Test
    public void testCreateAndFindOne() {
        Apartment apartment = new Apartment(null, "Downtown Studio",
                "Affordable apartment for students and young professionals", 0,
                1, "IL", "Chicago", 450, 95000,
                null, true, user.id(), null);

        assertThat(getRowCount()).isEqualTo(3);
        Apartment createdApartment = underTest.createApartment(apartment);
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

        assertThat(getRowCount()).isEqualTo(3);
        assertThat(apartments.size()).isEqualTo(3);

        for (Apartment apartment : apartments) {
            Apartment createdApartment = apartmentMap.get(apartment.id());
            assertApartmentsAreEqual(apartment, createdApartment);
            apartmentMap.remove(apartment.id());
        }
        assertThat(apartmentMap.size()).isEqualTo(0);
    }

    @Test
    public void testUpdateApartment() {
        Apartment updatedApartment = new Apartment(apartment1.id(), apartment1.title(),
                "Spacious, brand new appliances, new building, best views in the city!",
                apartment1.numberOfBedrooms(), apartment1.numberOfBathrooms(),
                apartment1.state(), apartment1.city(), apartment1.squareFeet(),
                675000, null, apartment1.available(),
                apartment1.id(), apartment1.renterId());

        underTest.updateApartment(updatedApartment);

        assertThat(getRowCount()).isEqualTo(3);

        Optional<Apartment> optionalApartment = underTest.findOne(updatedApartment.id());
        assertThat(optionalApartment).isPresent();

        Apartment expectedApartment = new Apartment(apartment1.id(), apartment1.title(),
                "Spacious, brand new appliances, new building, best views in the city!",
                apartment1.numberOfBedrooms(), apartment1.numberOfBathrooms(),
                apartment1.state(), apartment1.city(), apartment1.squareFeet(),
                675000, apartment1.dateListed(), apartment1.available(),
                apartment1.id(), apartment1.renterId());
        Apartment retrievedApartment = optionalApartment.get();
        assertApartmentsAreEqual(retrievedApartment, expectedApartment);
    }

    @Test
    public void testExists() {
        assertThat(underTest.exists(0)).isFalse();
        assertThat(underTest.exists(apartment1.id())).isTrue();
    }

    private int getRowCount() {
        return JdbcTestUtils.countRowsInTable(jdbcTemplate, "apartments");
    }

    private void assertApartmentsAreEqual(Apartment a1, Apartment a2) {
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

}
