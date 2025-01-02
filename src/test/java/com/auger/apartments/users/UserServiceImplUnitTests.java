package com.auger.apartments.users;

import com.auger.apartments.exceptions.DuplicateDataException;
import com.auger.apartments.exceptions.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplUnitTests {

    @Mock
    UserRepository userRepository;

    @Mock
    UserValidator userValidator;

    @InjectMocks
    UserServiceImpl underTest;

    @Test
    public void testCreateUser() {
        User user = new User(1, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());

        doNothing().when(userValidator).validateNewUser(user);
        when(userRepository.create(user)).thenReturn(user);

        User createdUser = underTest.createUser(user);

        verify(userValidator, times(1)).validateNewUser(user);
        verify(userRepository, times(1)).create(user);
        assertUsersAreEqual(user, createdUser);
    }

    @Test
    public void testCreateUserDuplicateEmail() {
        User user = new User(1, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());

        doThrow(new DuplicateDataException("A user with that email already exists"))
                .when(userValidator).validateNewUser(user);

        assertThatThrownBy(() -> underTest.createUser(user)).isInstanceOf(DuplicateDataException.class)
                .hasMessage("A user with that email already exists");
        verify(userValidator, times(1)).validateNewUser(user);
        verifyNoInteractions(userRepository);
    }

    @Test
    public void testCreateUserDuplicatePhoneNumber() {
        User user = new User(1, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());

        doThrow(new DuplicateDataException("A user with that phone number already exists"))
                .when(userValidator).validateNewUser(user);

        assertThatThrownBy(() -> underTest.createUser(user)).isInstanceOf(DuplicateDataException.class)
                .hasMessage("A user with that phone number already exists");
        verify(userValidator, times(1)).validateNewUser(user);
        verifyNoInteractions(userRepository);
    }

    @Test
    public void testGetUser() {
        User user = new User(1, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        int nonExistingUserId = 2;

        when(userRepository.findOne(user.id())).thenReturn(Optional.of(user));
        when(userRepository.findOne(nonExistingUserId)).thenReturn(Optional.empty());

        Optional<User> retrievedUser = underTest.getUser(user.id());
        assertThat(retrievedUser).isPresent();
        assertUsersAreEqual(user, retrievedUser.get());
        verify(userRepository, times(1)).findOne(user.id());

        Optional<User> emptyUser = underTest.getUser(nonExistingUserId);
        assertThat(emptyUser).isNotPresent();
        verify(userRepository, times(1)).findOne(nonExistingUserId);
    }

    @Test
    public void testGetAllUsers() {
        User user1 = new User(1, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(2, "Bob", "bob@gmail.com", "6015234567",
                LocalDate.of(1982, 7, 14), LocalDate.now());
        User user3 = new User(3, "Beth", "beth@gmail.com", "9876420341",
                LocalDate.of(1990, 2, 17), LocalDate.now());

        List<User> emptyList = List.of();
        List<User> userList = List.of(user1, user2, user3);

        when(userRepository.findAll()).thenReturn(emptyList);
        List<User> noUsers = underTest.getAllUsers();
        assertThat(noUsers.size()).isZero();
        assertThat(noUsers).isEqualTo(emptyList);
        verify(userRepository, times(1)).findAll();

        when(userRepository.findAll()).thenReturn(userList);
        List<User> multipleUsers = underTest.getAllUsers();
        assertThat(multipleUsers.size()).isEqualTo(userList.size());
        assertThat(multipleUsers).isEqualTo(userList);
        verify(userRepository, times(2)).findAll();
    }

    @Test
    public void testUpdateUser() {
        User user = new User(1, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());

        when(userRepository.exists(user.id())).thenReturn(true);
        doNothing().when(userValidator).validateExistingUser(user);
        doNothing().when(userRepository).update(user);

        underTest.updateUser(user);

        verify(userRepository, times(1)).exists(user.id());
        verify(userValidator, times(1)).validateExistingUser(user);
        verify(userRepository, times(1)).update(user);
        assertThatNoException().isThrownBy(() -> underTest.updateUser(user));
    }

    @Test
    public void testUpdateUserDuplicateEmail() {
        User user = new User(1, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());

        when(userRepository.exists(user.id())).thenReturn(true);
        doThrow(new DuplicateDataException("A user with that email already exists"))
                .when(userValidator).validateExistingUser(user);

        assertThatThrownBy(() -> underTest.updateUser(user)).isInstanceOf(DuplicateDataException.class)
                .hasMessage("A user with that email already exists");
        verify(userRepository, times(1)).exists(user.id());
        verify(userValidator, times(1)).validateExistingUser(user);
        verify(userRepository, times(0)).update(user);
    }

    @Test
    public void testUpdateUserDuplicatePhoneNumber() {
        User user = new User(1, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());

        when(userRepository.exists(user.id())).thenReturn(true);
        doThrow(new DuplicateDataException("A user with that phone number already exists"))
                .when(userValidator).validateExistingUser(user);

        assertThatThrownBy(() -> underTest.updateUser(user)).isInstanceOf(DuplicateDataException.class)
                .hasMessage("A user with that phone number already exists");
        verify(userRepository, times(1)).exists(user.id());
        verify(userValidator, times(1)).validateExistingUser(user);
        verify(userRepository, times(0)).update(user);
    }

    @Test
    public void testUpdateUserInvalidId() {
        User user = new User(1, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());

        when(userRepository.exists(user.id())).thenReturn(false);

        assertThatThrownBy(() -> underTest.updateUser(user)).isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", user.id()));
        verify(userRepository, times(1)).exists(user.id());
        verifyNoInteractions(userValidator);
        verify(userRepository, times(0)).update(user);
    }

    @Test
    public void testDoesExist() {
        int existingUserId = 1;
        int nonExistingUserId = 2;

        when(userRepository.exists(existingUserId)).thenReturn(true);
        when(userRepository.exists(nonExistingUserId)).thenReturn(false);

        assertThat(underTest.doesExist(existingUserId)).isTrue();
        verify(userRepository, times(1)).exists(existingUserId);

        assertThat(underTest.doesExist(nonExistingUserId)).isFalse();
        verify(userRepository, times(1)).exists(nonExistingUserId);
    }

    private void assertUsersAreEqual(User u1, User u2) {
        assertThat(u1.name()).isEqualTo(u2.name());
        assertThat(u1.email()).isEqualTo(u2.email());
        assertThat(u1.phoneNumber()).isEqualTo(u2.phoneNumber());
        assertThat(u1.birthDate()).isEqualTo(u2.birthDate());
        assertThat(u1.dateJoined()).isEqualTo(u2.dateJoined());
    }
}
