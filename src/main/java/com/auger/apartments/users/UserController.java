package com.auger.apartments.users;

import com.auger.apartments.exceptions.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        logger.info("Creating a user");
        User createdUser = userService.createUser(user);
        logger.info("User created successfully");
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable int id) {
        logger.info("Retrieving a user");
        Optional<User> user = userService.getUser(id);
        if (user.isPresent()) {
            logger.info("User retrieved successfully");
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        } else {
            throw new UserNotFoundException(String.format("User with id %s does not exist", id));
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Retrieving all users");
        List<User> allUsers = userService.getAllUsers();
        logger.info("Users retrieved successfully");
        return new ResponseEntity<>(allUsers, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<Void> updateUser(@RequestBody User user) {
        logger.info("Updating a user");
        userService.updateUser(user);
        logger.info("User updated successfully");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        logger.info("Deleting a user");
        userService.deleteUser(id);
        logger.info("User deleted successfully");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
