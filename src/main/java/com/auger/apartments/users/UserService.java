package com.auger.apartments.users;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(User user);

    Optional<User> getUser(int id);

    List<User> getAllUsers();

    void updateUser(User user);

    boolean doesExist(Integer id);
}
