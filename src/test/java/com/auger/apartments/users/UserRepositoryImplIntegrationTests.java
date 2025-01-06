package com.auger.apartments.users;

import com.auger.apartments.BaseIntegrationTest;
import com.auger.apartments.apartments.Apartment;
import com.auger.apartments.applications.Application;
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

import static com.auger.apartments.TestUtils.assertUsersAreEqual;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserRepositoryImplIntegrationTests extends BaseIntegrationTest {

    @Autowired
    UserRepositoryImpl underTest;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    public void addData() {
        User u1 = new User(null, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), null);
        User u2 = new User(null, "Jennifer", "Lilly", "jennifer@gmail.com",
                "9876543214", LocalDate.of(1975, 8, 3), null);
        User u3 = new User(null, "Bob", "Daly", "Bob@gmail.com",
                "7365490142", LocalDate.of(2001, 12, 19), null);
        user1 = underTest.create(u1);
        user2 = underTest.create(u2);
        user3 = underTest.create(u3);
    }

    @AfterEach
    public void clearTable() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
    }

    @Test
    public void testCreateAndFindOne() {
        User user = new User(null, "Conrad", "Wiggins", "conrad@gmail.com",
                "4876280912", LocalDate.of(1972, 11, 14), null);

        assertThat(getRowCount()).isEqualTo(3);
        User createdUser = underTest.create(user);
        assertThat(getRowCount()).isEqualTo(4);
        assertThat(createdUser.id()).isNotNull();

        Optional<User> optionalUser = underTest.findOne(createdUser.id());
        assertThat(optionalUser).isPresent();
        User retrievedUser = optionalUser.get();
        assertUsersAreEqual(createdUser, retrievedUser);
    }

    @Test
    public void testFindOneWithInvalidId() {
        Optional<User> result = underTest.findOne(0);
        assertThat(result).isNotPresent();
    }

    @Test
    public void testFindAll() {
        Map<Integer, User> userMap = new HashMap<>();
        userMap.put(user1.id(), user1);
        userMap.put(user2.id(), user2);
        userMap.put(user3.id(), user3);

        List<User> users = underTest.findAll();

        assertThat(getRowCount()).isEqualTo(userMap.size());
        assertThat(users.size()).isEqualTo(userMap.size());

        for (User user : users) {
            User createdUser = userMap.get(user.id());
            assertUsersAreEqual(user, createdUser);
            userMap.remove(user.id());
        }
        assertThat(userMap.size()).isZero();
    }

    @Test
    public void testUpdate() {
        User updatedUser = new User(user1.id(), "Kai", "Asakura", "kai@gmail.com",
                "7865436549", LocalDate.of(2003, 1, 18), null);

        underTest.update(updatedUser);

        assertThat(getRowCount()).isEqualTo(3);

        Optional<User> optionalUser = underTest.findOne(updatedUser.id());
        assertThat(optionalUser).isPresent();

        User retrievedUser = optionalUser.get();
        User expectedUser = new User(updatedUser.id(), "Kai", "Asakura", "kai@gmail.com",
                "7865436549", LocalDate.of(2003, 1, 18), user1.dateJoined());
        assertUsersAreEqual(retrievedUser, expectedUser);
    }

    @Test
    public void testExists() {
        assertThat(underTest.exists(0)).isFalse();
        assertThat(underTest.exists(user1.id())).isTrue();
    }

    @Test
    public void testDelete() {
        assertThat(getRowCount()).isEqualTo(3);
        assertThat(underTest.exists(user1.id())).isTrue();

        underTest.delete(user1.id());

        assertThat(getRowCount()).isEqualTo(2);
        assertThat(underTest.exists(user1.id())).isFalse();
    }

    @Test
    public void testDeleteWithApartmentsAndApplications() {
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
                null, true, user2.id(), null);
        Apartment apartment1 = apartmentService.createApartment(apt1);
        Apartment apartment2 = apartmentService.createApartment(apt2);
        Apartment apartment3 = apartmentService.createApartment(apt3);

        Application app1 = new Application(null, null, true, false,
                user1.id(), apartment3.id());
        Application app2 = new Application(null, null, true, false,
                user2.id(), apartment1.id());
        Application app3 = new Application(null, null, true, false,
                user3.id(), apartment1.id());
        Application app4 = new Application(null, null, true, false,
                user2.id(), apartment2.id());
        Application app5 = new Application(null, null, true, false,
                user3.id(), apartment2.id());

        applicationService.createApplication(app1);
        applicationService.createApplication(app2);
        applicationService.createApplication(app3);
        applicationService.createApplication(app4);
        applicationService.createApplication(app5);

        assertThat(getRowCount()).isEqualTo(3);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "apartments")).isEqualTo(3);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "applications")).isEqualTo(5);

        underTest.delete(user1.id());

        assertThat(getRowCount()).isEqualTo(2);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "apartments")).isEqualTo(1);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "applications")).isEqualTo(0);
    }

    private int getRowCount() {
        return JdbcTestUtils.countRowsInTable(jdbcTemplate, "users");
    }
}
