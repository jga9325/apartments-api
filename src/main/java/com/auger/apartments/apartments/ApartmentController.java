package com.auger.apartments.apartments;

import com.auger.apartments.exceptions.ApartmentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/apartments")
public class ApartmentController {

    private static final Logger logger = LoggerFactory.getLogger(ApartmentController.class);
    private final ApartmentService apartmentService;

    public ApartmentController(ApartmentService apartmentService) {
        this.apartmentService = apartmentService;
    }

    @PostMapping
    public ResponseEntity<Apartment> createApartment(@RequestBody Apartment apartment) {
        logger.info("Creating an apartment");
        Apartment createdApartment = apartmentService.createApartment(apartment);
        logger.info("Apartment created successfully");
        return new ResponseEntity<>(createdApartment, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Apartment> getApartment(@PathVariable int id) {
        logger.info("Retrieving an apartment");
        Optional<Apartment> apartment = apartmentService.getApartment(id);
        if (apartment.isPresent()) {
            logger.info("Apartment retrieved successfully");
            return new ResponseEntity<>(apartment.get(), HttpStatus.OK);
        } else {
            throw new ApartmentNotFoundException(String.format("Apartment with id %s does not exist", id));
        }
    }

    @GetMapping
    public ResponseEntity<List<Apartment>> getAllApartments() {
        logger.info("Retrieving all apartments");
        List<Apartment> allApartments = apartmentService.getAllApartments();
        logger.info("Apartments retrieved successfully");
        return new ResponseEntity<>(allApartments, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<Void> updateApartment(@RequestBody Apartment apartment) {
        logger.info("Updating an apartment");
        apartmentService.updateApartment(apartment);
        logger.info("Apartment updated successfully");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApartment(@PathVariable int id) {
        logger.info("Deleting an apartment");
        apartmentService.deleteApartment(id);
        logger.info("Apartment deleted successfully");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
