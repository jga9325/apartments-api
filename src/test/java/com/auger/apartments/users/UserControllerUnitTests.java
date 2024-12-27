package com.auger.apartments.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerUnitTests {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void testCreateUser() throws Exception {
        User user = new User(1, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());

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

    // Test correct responses for when a DuplicateDataException is thrown for email and phone number

    @Test
    public void testGetUser() {
        User user = new User(1, "John", "john@gmail.com", "1234567894",
                LocalDate.of(1999, 4, 28), LocalDate.now());

        when(userService.getUser(user.id())).thenReturn(Optional.of(user));

//        MvcResult result = mockMvc.perform(get("/users/{id}", user.id()
//                .)
    }

    private void assertUsersAreEqual(User u1, User u2) {
        assertThat(u1.name()).isEqualTo(u2.name());
        assertThat(u1.email()).isEqualTo(u2.email());
        assertThat(u1.phoneNumber()).isEqualTo(u2.phoneNumber());
        assertThat(u1.birthDate()).isEqualTo(u2.birthDate());
        assertThat(u1.dateJoined()).isEqualTo(u2.dateJoined());
    }

}
