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
    public void testVerifyUniqueEmailForNewUser() {
        User user = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        assertThatNoException().isThrownBy(() -> underTest.verifyUniqueEmailForNewUser(user.email()));
        userRepository.create(user);
        assertThatThrownBy(() -> underTest.verifyUniqueEmailForNewUser(user.email()))
                .isInstanceOf(DuplicateDataException.class).hasMessage("A user with that email already exists");
    }

    @Test
    public void testVerifyUniquePhoneNumberForNewUser() {
        User user = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        assertThatNoException().isThrownBy(() -> underTest.verifyUniquePhoneNumberForNewUser(user.phoneNumber()));
        userRepository.create(user);
        assertThatThrownBy(() -> underTest.verifyUniquePhoneNumberForNewUser(user.phoneNumber()))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage("A user with that phone number already exists");
    }

    @Test
    public void testVerifyUniqueEmailForExistingUser() {
        User user1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "Daly", "bob@gmail.com",
                "7453928318", LocalDate.of(2000, 1, 1), LocalDate.now());
        userRepository.create(user1);
        User createdUser2 = userRepository.create(user2);

        User sameUser = new User(createdUser2.id(), "Bob", "Daly", "bob@gmail.com",
                "7453928318", LocalDate.of(2000, 1, 1), LocalDate.now());
        assertThatNoException().isThrownBy(
                () -> underTest.verifyUniqueEmailForExistingUser(sameUser.id(), sameUser.email())
        );

        User duplicateEmailUser = new User(createdUser2.id(), "Bob", "Daly",
                "john@gmail.com", "7453928318", LocalDate.of(2000, 1, 1),
                LocalDate.now());
        assertThatThrownBy(() -> underTest.verifyUniqueEmailForExistingUser(
                duplicateEmailUser.id(), duplicateEmailUser.email())
        )
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage("A user with that email already exists");
    }

    @Test
    public void testVerifyUniquePhoneNumberForExistingUser() {
        User user1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "Daly", "bob@gmail.com",
                "7453928318", LocalDate.of(2000, 1, 1), LocalDate.now());
        userRepository.create(user1);
        User createdUser2 = userRepository.create(user2);

        User sameUser = new User(createdUser2.id(), "Bob", "Daly", "bob@gmail.com",
                "7453928318", LocalDate.of(2000, 1, 1), LocalDate.now());
        assertThatNoException().isThrownBy(() -> underTest.verifyUniquePhoneNumberForExistingUser(
                sameUser.id(), sameUser.phoneNumber())
        );

        User duplicatePhoneNumberUser = new User(createdUser2.id(), "Bob", "Daly",
                "bob@gmail.com", "1234567894", LocalDate.of(2000, 1, 1),
                LocalDate.now());
        assertThatThrownBy(() -> underTest.verifyUniquePhoneNumberForExistingUser(
                duplicatePhoneNumberUser.id(), duplicatePhoneNumberUser.phoneNumber())
        )
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage("A user with that phone number already exists");
    }

    @Test
    public void testValidateNewUser() {
        User user = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        assertThatNoException().isThrownBy(() -> underTest.validateNewUser(user));
    }

    @Test
    public void testValidateNewUserDuplicateEmail() {
        User user = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        userRepository.create(user);
        assertThatThrownBy(() -> underTest.validateNewUser(user))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage("A user with that email already exists");
    }

    @Test
    public void testValidateNewUserDuplicatePhoneNumber() {
        User user = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User duplicatePhoneNumberUser = new User(0, "John", "Rogers", "johnny@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        userRepository.create(user);
        assertThatThrownBy(() -> underTest.validateNewUser(duplicatePhoneNumberUser))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage("A user with that phone number already exists");
    }

    @Test
    public void testValidateExistingUser() {
        User user = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User createdUser = userRepository.create(user);

        User updatedUser = new User(createdUser.id(), "John", "Rogers", "johnny@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        assertThatNoException().isThrownBy(() -> underTest.validateExistingUser(updatedUser));
    }

    @Test
    public void testValidateExistingUserDuplicateEmail() {
        User user1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "Daly", "bob@gmail.com",
                "7453928318", LocalDate.of(2000, 1, 1), LocalDate.now());
        userRepository.create(user1);
        User createdUser2 = userRepository.create(user2);

        User duplicateEmailUser = new User(createdUser2.id(), "Bob", "Daly",
                "john@gmail.com", "7453928318", LocalDate.of(2000, 1, 1),
                LocalDate.now());

        assertThatThrownBy(() -> underTest.validateExistingUser(duplicateEmailUser))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage("A user with that email already exists");
    }

    @Test
    public void testValidateExistingUserDuplicatePhoneNumber() {
        User user1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(0, "Bob", "Daly", "bob@gmail.com",
                "7453928318", LocalDate.of(2000, 1, 1), LocalDate.now());
        userRepository.create(user1);
        User createdUser2 = userRepository.create(user2);

        User duplicatePhoneNumberUser = new User(createdUser2.id(), "Bob", "Daly",
                "bob@gmail.com", "1234567894", LocalDate.of(2000, 1, 1),
                LocalDate.now());

        assertThatThrownBy(() -> underTest.validateExistingUser(duplicatePhoneNumberUser))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage("A user with that phone number already exists");
    }
}
