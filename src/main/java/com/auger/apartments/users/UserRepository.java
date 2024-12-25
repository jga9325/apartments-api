package com.auger.apartments.users;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User create(User user);

    Optional<User> findOne(int id);

    List<User> findAll();

    void update(User user);

    boolean exists(int id);
}
