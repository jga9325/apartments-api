package com.auger.apartments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

/**
 * Base test class for controller integration tests.
 * Provides a connection to a Postgres database in a Testcontainer, a TestRestTemplate bean,
 * a JDBCTemplate bean, and a web environment running on a random port.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    protected TestRestTemplate testRestTemplate;

}
