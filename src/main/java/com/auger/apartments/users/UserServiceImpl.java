package com.auger.apartments.users;

import com.auger.apartments.exceptions.UserNotFoundException;
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
        userValidator.validateNewUser(user);
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
        if (doesExist(user.id())) {
            userValidator.validateExistingUser(user);
            userRepository.update(user);
        } else {
            throw new UserNotFoundException(String.format("User with id %s does not exist", user.id()));
        }
    }

    @Override
    public void deleteUser(int id) {
        if (doesExist(id)) {
            userValidator.validateUserDeletion(id);
            userRepository.delete(id);
        } else {
            throw new UserNotFoundException(String.format("User with id %s does not exist", id));
        }
    }

    @Override
    public boolean doesExist(Integer id) {
        if (id == null) {
            return false;
        }
        return userRepository.exists(id);
    }
}
