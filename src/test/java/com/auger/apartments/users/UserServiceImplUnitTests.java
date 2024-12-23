package com.auger.apartments.users;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplUnitTests {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserServiceImpl underTest;

    @Test
    public void testCreateUser() {

    }
}
