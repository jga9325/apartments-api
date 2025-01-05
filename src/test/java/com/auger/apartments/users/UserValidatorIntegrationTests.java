package com.auger.apartments.users;

import com.auger.apartments.BaseIntegrationTest;
import com.auger.apartments.exceptions.DuplicateDataException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class UserValidatorIntegrationTests extends BaseIntegrationTest {

    @Autowired
    UserValidator underTest;

    private User user1;
    private User user2;

    @BeforeEach
    public void addData() {
        User u1 = new User(0, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User u2 = new User(0, "Bob", "Daly", "bob@gmail.com",
                "7453928318", LocalDate.of(2000, 1, 1), LocalDate.now());
        user1 = userRepository.create(u1);
        user2 = userRepository.create(u2);
    }

    @AfterEach
    public void clearTable() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");
    }

    @Test
    public void testVerifyUniqueEmailForNewUser() {
        String validEmail = "email@email.com";

        assertThatNoException().isThrownBy(() -> underTest.verifyUniqueEmailForNewUser(validEmail));
        assertThatThrownBy(() -> underTest.verifyUniqueEmailForNewUser(user1.email()))
                .isInstanceOf(DuplicateDataException.class).hasMessage("A user with that email already exists");
    }

    @Test
    public void testVerifyUniquePhoneNumberForNewUser() {
        String validPhoneNumber = "7536251730";

        assertThatNoException().isThrownBy(() -> underTest.verifyUniquePhoneNumberForNewUser(validPhoneNumber));
        assertThatThrownBy(() -> underTest.verifyUniquePhoneNumberForNewUser(user1.phoneNumber()))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage("A user with that phone number already exists");
    }

    @Test
    public void testVerifyUniqueEmailForExistingUser() {
        User sameUser = new User(user2.id(), "Bob", "Daly", "bob@gmail.com",
                "7453928318", LocalDate.of(2000, 1, 1), LocalDate.now());
        assertThatNoException().isThrownBy(
                () -> underTest.verifyUniqueEmailForExistingUser(sameUser.id(), sameUser.email())
        );

        User duplicateEmailUser = new User(user2.id(), "Bob", "Daly",
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
        User sameUser = new User(user2.id(), "Bob", "Daly", "bob@gmail.com",
                "7453928318", LocalDate.of(2000, 1, 1), LocalDate.now());
        assertThatNoException().isThrownBy(() -> underTest.verifyUniquePhoneNumberForExistingUser(
                sameUser.id(), sameUser.phoneNumber())
        );

        User duplicatePhoneNumberUser = new User(user2.id(), "Bob", "Daly",
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
        User user = new User(0, "John", "Rogers", "johnny@gmail.com",
                "2438905436", LocalDate.of(1999, 4, 28), LocalDate.now());

        assertThatNoException().isThrownBy(() -> underTest.validateNewUser(user));
    }

    @Test
    public void testValidateNewUserDuplicateEmail() {
        assertThatThrownBy(() -> underTest.validateNewUser(user1))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage("A user with that email already exists");
    }

    @Test
    public void testValidateNewUserDuplicatePhoneNumber() {
        User duplicatePhoneNumberUser = new User(0, "John", "Rogers", "johnny@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        assertThatThrownBy(() -> underTest.validateNewUser(duplicatePhoneNumberUser))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage("A user with that phone number already exists");
    }

    @Test
    public void testValidateExistingUser() {
        User updatedUser = new User(user1.id(), "John", "Rogers", "johnny@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        assertThatNoException().isThrownBy(() -> underTest.validateExistingUser(updatedUser));
    }

    @Test
    public void testValidateExistingUserDuplicateEmail() {
        User duplicateEmailUser = new User(user2.id(), "Bob", "Daly",
                "john@gmail.com", "7453928318", LocalDate.of(2000, 1, 1),
                LocalDate.now());

        assertThatThrownBy(() -> underTest.validateExistingUser(duplicateEmailUser))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage("A user with that email already exists");
    }

    @Test
    public void testValidateExistingUserDuplicatePhoneNumber() {
        User duplicatePhoneNumberUser = new User(user2.id(), "Bob", "Daly",
                "bob@gmail.com", "1234567894", LocalDate.of(2000, 1, 1),
                LocalDate.now());

        assertThatThrownBy(() -> underTest.validateExistingUser(duplicatePhoneNumberUser))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage("A user with that phone number already exists");
    }
}
