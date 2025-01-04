package com.auger.apartments.applications;

import com.auger.apartments.exceptions.ApplicationNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Application> createApplication(@RequestBody Application application) {
        Application createdApplication = applicationService.createApplication(application);
        return new ResponseEntity<>(createdApplication, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplication(@PathVariable int id) {
        Optional<Application> application = applicationService.getApplication(id);
        if (application.isPresent()) {
            return new ResponseEntity<>(application.get(), HttpStatus.OK);
        } else {
            throw new ApplicationNotFoundException(String.format("Application with id %s does not exist", id));
        }
    }

    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        List<Application> allApplications = applicationService.getAllApplications();
        return new ResponseEntity<>(allApplications, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<Void> updateApplication(@RequestBody Application application) {
        applicationService.updateApplication(application);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
