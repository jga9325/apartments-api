package com.auger.apartments.apartments;

import com.auger.apartments.exceptions.ApartmentNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/apartments")
public class ApartmentController {

    private final ApartmentService apartmentService;

    public ApartmentController(ApartmentService apartmentService) {
        this.apartmentService = apartmentService;
    }

    @PostMapping
    public ResponseEntity<Apartment> createApartment(@RequestBody Apartment apartment) {
        Apartment createdApartment = apartmentService.createApartment(apartment);
        return new ResponseEntity<>(createdApartment, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Apartment> getApartment(@PathVariable int id) {
        Optional<Apartment> apartment = apartmentService.getApartment(id);
        if (apartment.isPresent()) {
            return new ResponseEntity<>(apartment.get(), HttpStatus.OK);
        } else {
            throw new ApartmentNotFoundException(String.format("Apartment with id %s does not exist", id));
        }
    }

    @GetMapping
    public ResponseEntity<List<Apartment>> getAllApartments() {
        List<Apartment> allApartments = apartmentService.getAllApartments();
        return new ResponseEntity<>(allApartments, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<Void> updateApartment(@RequestBody Apartment apartment) {
        apartmentService.updateApartment(apartment);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApartment(@PathVariable int id) {
        apartmentService.deleteApartment(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
