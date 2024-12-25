package com.auger.apartments.users;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;

    public UserServiceImpl(UserRepository userRepository, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
    }

    @Override
    public User createUser(User user) {
        userValidator.verifyNewUser(user);
        return userRepository.create(user);
    }

    @Override
    public Optional<User> getUser(int id) {
        return userRepository.findOne(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void updateUser(User user) {
        userValidator.verifyExistingUser(user);
        userRepository.update(user);
    }

    @Override
    public boolean doesExist(int id) {
        return userRepository.exists(id);
    }
}
