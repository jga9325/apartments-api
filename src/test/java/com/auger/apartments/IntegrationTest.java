package com.auger.apartments;

import com.auger.apartments.apartments.ApartmentRepository;
import com.auger.apartments.apartments.ApartmentService;
import com.auger.apartments.applications.ApplicationService;
import com.auger.apartments.users.UserRepository;
import com.auger.apartments.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class IntegrationTest extends BaseIntegrationTest {

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
