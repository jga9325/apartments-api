package com.auger.apartments;

import com.auger.apartments.apartments.ApartmentRepository;
import com.auger.apartments.apartments.ApartmentService;
import com.auger.apartments.applications.ApplicationService;
import com.auger.apartments.users.UserRepository;
import com.auger.apartments.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base test class for all integration tests that don't test a controller class.
 * Provides a connection to a Postgres database in a Testcontainer, a JDBCTemplate bean,
 * and beans for common services and repositories.
 */
@SpringBootTest
public abstract class IntegrationTest extends BaseIntegrationTest {

    @Autowired
    protected UserService userService;

    @Autowired
    protected ApartmentService apartmentService;

    @Autowired
    protected ApplicationService applicationService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ApartmentRepository apartmentRepository;

}
