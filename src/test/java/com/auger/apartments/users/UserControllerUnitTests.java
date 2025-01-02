package com.auger.apartments.users;

import com.auger.apartments.exceptions.DuplicateDataException;
import com.auger.apartments.exceptions.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.auger.apartments.TestUtils.assertUsersAreEqual;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerUnitTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @Test
    public void testCreateUser() throws Exception {
        User user = new User(1, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        when(userService.createUser(user)).thenReturn(user);

        String userJson = objectMapper.writeValueAsString(user);

        MvcResult result = mockMvc.perform(post("/users")
                .content(userJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        User createdUser = objectMapper.readValue(responseString, User.class);

        assertUsersAreEqual(user, createdUser);
        verify(userService, times(1)).createUser(user);
    }

    @Test
    public void testCreateUserDuplicateEmail() throws Exception {
        User user = new User(1, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        doThrow(new DuplicateDataException("A user with that email already exists"))
                .when(userService).createUser(user);

        String userJson = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/users")
                .content(userJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("A user with that email already exists"));

        verify(userService, times(1)).createUser(user);
    }

    @Test
    public void testCreateUserDuplicatePhoneNumber() throws Exception {
        User user = new User(1, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        doThrow(new DuplicateDataException("A user with that phone number already exists"))
                .when(userService).createUser(user);

        String userJson = objectMapper.writeValueAsString(user);

        mockMvc.perform(post("/users")
                .content(userJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("A user with that phone number already exists"));

        verify(userService, times(1)).createUser(user);
    }

    @Test
    public void testGetUser() throws Exception {
        User user = new User(1, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        when(userService.getUser(user.id())).thenReturn(Optional.of(user));

        MvcResult result = mockMvc.perform(get("/users/{id}", user.id()))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        User retrievedUser = objectMapper.readValue(responseString, User.class);

        assertUsersAreEqual(user, retrievedUser);
        verify(userService, times(1)).getUser(user.id());
    }

    @Test
    public void testGetUserInvalidId() throws Exception {
        int userId = 1;

        when(userService.getUser(userId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(String.format("User with id %s does not exist", userId)));

        verify(userService, times(1)).getUser(userId);
    }

    @Test
    public void testGetAllUsers() throws Exception {
        User user1 = new User(1, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());
        User user2 = new User(2, "Bob", "Daly", "bob@gmail.com",
                "6015234567", LocalDate.of(1982, 7, 14), LocalDate.now());
        User user3 = new User(3, "Beth", "Smith", "beth@gmail.com",
                "9876420341", LocalDate.of(1990, 2, 17), LocalDate.now());

        List<User> userList = List.of(user1, user2, user3);
        when(userService.getAllUsers()).thenReturn(userList);

        MvcResult result = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, User.class);
        List<User> allUsers = objectMapper.readValue(responseString, collectionType);

        assertThat(allUsers).isEqualTo(userList);
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    public void testGetAllUsersEmptyResponse() throws Exception {
        List<User> emptyList = List.of();

        when(userService.getAllUsers()).thenReturn(emptyList);

        MvcResult result = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, User.class);
        List<User> emptyListResponse = objectMapper.readValue(responseString, collectionType);

        assertThat(emptyListResponse).isEqualTo(emptyList);
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    public void testUpdateUser() throws Exception {
        User user = new User(1, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        doNothing().when(userService).updateUser(user);

        String userJson = objectMapper.writeValueAsString(user);

        mockMvc.perform(put("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).updateUser(user);
    }

    @Test
    public void testUpdateUserInvalidId() throws Exception {
        User user = new User(1, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        doThrow(new UserNotFoundException(String.format("User with id %s does not exist", user.id())))
                .when(userService).updateUser(user);

        String userJson = objectMapper.writeValueAsString(user);

        mockMvc.perform(put("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(String.format("User with id %s does not exist", user.id())));

        verify(userService, times(1)).updateUser(user);
    }

    @Test
    public void testUpdateUserNullId() throws Exception {
        User user = new User(null, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        doThrow(new UserNotFoundException(String.format("User with id %s does not exist", user.id())))
                .when(userService).updateUser(user);

        String userJson = objectMapper.writeValueAsString(user);

        mockMvc.perform(put("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(String.format("User with id %s does not exist", user.id())));

        verify(userService, times(1)).updateUser(user);
    }

    @Test
    public void testUpdateUserDuplicateEmail() throws Exception {
        User user = new User(1, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        doThrow(new DuplicateDataException("A user with that email already exists"))
                .when(userService).updateUser(user);

        String userJson = objectMapper.writeValueAsString(user);

        mockMvc.perform(put("/users")
                .content(userJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("A user with that email already exists"));

        verify(userService, times(1)).updateUser(user);
    }

    @Test
    public void testUpdateUserDuplicatePhoneNumber() throws Exception {
        User user = new User(1, "John", "Rogers", "john@gmail.com",
                "1234567894", LocalDate.of(1999, 4, 28), LocalDate.now());

        doThrow(new DuplicateDataException("A user with that phone number already exists"))
                .when(userService).updateUser(user);

        String userJson = objectMapper.writeValueAsString(user);

        mockMvc.perform(put("/users")
                .content(userJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("A user with that phone number already exists"));

        verify(userService, times(1)).updateUser(user);
    }
}
