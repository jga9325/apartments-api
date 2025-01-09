package com.auger.apartments.applications;

import com.auger.apartments.apartments.ApartmentService;
import com.auger.apartments.exceptions.ApartmentNotFoundException;
import com.auger.apartments.exceptions.UserNotFoundException;
import com.auger.apartments.users.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Provides methods to validate that an application meets database and business constraints before
 * executing database operations such as creating, updating, and deleting applications.
 */
@Component
public class ApplicationValidator {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationValidator.class);
    private final UserService userService;
    private final ApartmentService apartmentService;

    public ApplicationValidator(UserService userService, ApartmentService apartmentService) {
        this.userService = userService;
        this.apartmentService = apartmentService;
    }

    public void validateNewApplication(Application application) {
        logger.info("Validating new application");
        verifyUserExists(application.userId());
        verifyApartmentExists(application.apartmentId());
        logger.info("Validation complete");
    }

    public void verifyUserExists(int userId) {
        if (!userService.doesExist(userId)) {
            throw new UserNotFoundException(String.format("User with id %s does not exist", userId));
        }
    }

    public void verifyApartmentExists(int apartmentId) {
        if (!apartmentService.doesExist(apartmentId)) {
            throw new ApartmentNotFoundException(String.format("Apartment with id %s does not exist", apartmentId));
        }
    }
}
