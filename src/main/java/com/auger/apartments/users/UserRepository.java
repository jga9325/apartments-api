package com.auger.apartments.users;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    int create(User user);

    Optional<User> findOne(int id);

    List<User> findAll();

    int update(User user);

    boolean exists(int id);
}
