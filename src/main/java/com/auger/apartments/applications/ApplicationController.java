package com.auger.apartments.applications;

import com.auger.apartments.exceptions.ApplicationNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Application> createApplication(@RequestBody Application application) {
        logger.info("Creating an application");
        Application createdApplication = applicationService.createApplication(application);
        logger.info("Application created successfully");
        return new ResponseEntity<>(createdApplication, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplication(@PathVariable int id) {
        logger.info("Retrieving an application");
        Optional<Application> application = applicationService.getApplication(id);
        if (application.isPresent()) {
            logger.info("Application retrieved successfully");
            return new ResponseEntity<>(application.get(), HttpStatus.OK);
        } else {
            throw new ApplicationNotFoundException(String.format("Application with id %s does not exist", id));
        }
    }

    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        logger.info("Retrieving all applications");
        List<Application> allApplications = applicationService.getAllApplications();
        logger.info("Applications retrieved successfully");
        return new ResponseEntity<>(allApplications, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<Void> updateApplication(@RequestBody Application application) {
        logger.info("Updating an application");
        applicationService.updateApplication(application);
        logger.info("Application updated successfully");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable int id) {
        logger.info("Deleting an application");
        applicationService.deleteApplication(id);
        logger.info("Application deleted successfully");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
