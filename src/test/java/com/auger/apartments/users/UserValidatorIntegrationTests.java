package com.auger.apartments.users;

import com.auger.apartments.exceptions.DuplicateDataException;
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

import static org.assertj.core.api.AssertionsForClassTypes.*;

@Testcontainers
@SpringBootTest
public class UserValidatorIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2-alpine");

    @Autowired
    UserValidator underTest;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void clearTable() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
    }

    @Test
    public void testVerifyEmailForNewUser() {
        User user = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());

        assertThatNoException().isThrownBy(() -> underTest.verifyEmailForNewUser(user.email()));
        userRepository.create(user);
        assertThatThrownBy(() -> underTest.verifyEmailForNewUser(user.email()))
                .isInstanceOf(DuplicateDataException.class).hasMessage("A user with that email already exists");
    }

    @Test
    public void testVerifyPhoneNumberForNewUser() {
        User user = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());

        assertThatNoException().isThrownBy(() -> underTest.verifyPhoneNumberForNewUser(user.phoneNumber()));
        userRepository.create(user);
        assertThatThrownBy(() -> underTest.verifyPhoneNumberForNewUser(user.phoneNumber()))
                .isInstanceOf(DuplicateDataException.class).hasMessage("A user with that phone number already exists");
    }

    @Test
    public void testVerifyEmailForExistingUser() {
        User user1 = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "bob@gmail.com", "7453928318",
                LocalDate.of(2000, 1, 1), LocalDate.now());
        userRepository.create(user1);
        User createdUser2 = userRepository.create(user2);

        User sameUser = new User(createdUser2.id(), "Bob", "bob@gmail.com", "7453928318",
                LocalDate.of(2000, 1, 1), LocalDate.now());
        assertThatNoException().isThrownBy(() -> underTest.verifyEmailForExistingUser(sameUser.id(), sameUser.email()));

        User duplicateEmailUser = new User(createdUser2.id(), "Bob", "john@gmail.com",
                "7453928318", LocalDate.of(2000, 1, 1), LocalDate.now());
        assertThatThrownBy(() -> underTest.verifyEmailForExistingUser(
                duplicateEmailUser.id(), duplicateEmailUser.email())
        ).isInstanceOf(DuplicateDataException.class).hasMessage("A user with that email already exists");
    }

    @Test
    public void testVerifyPhoneNumberForExistingUser() {
        User user1 = new User(0, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "bob@gmail.com", "7453928318",
                LocalDate.of(2000, 1, 1), LocalDate.now());
        userRepository.create(user1);
        User createdUser2 = userRepository.create(user2);

        User sameUser = new User(createdUser2.id(), "Bob", "bob@gmail.com", "7453928318",
                LocalDate.of(2000, 1, 1), LocalDate.now());
        assertThatNoException().isThrownBy(() -> underTest.verifyPhoneNumberForExistingUser(
                sameUser.id(), sameUser.phoneNumber())
        );

        User duplicatePhoneNumberUser = new User(createdUser2.id(), "Bob", "bob@gmail.com",
                "1234567894", LocalDate.of(2000, 1, 1), LocalDate.now());
        assertThatThrownBy(() -> underTest.verifyPhoneNumberForExistingUser(
                duplicatePhoneNumberUser.id(), duplicatePhoneNumberUser.phoneNumber())
        ).isInstanceOf(DuplicateDataException.class).hasMessage("A user with that phone number already exists");
    }
}
