package com.auger.apartments.apartments;

import com.auger.apartments.exceptions.ApartmentNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApartmentServiceImpl implements ApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final ApartmentValidator apartmentValidator;

    public ApartmentServiceImpl(ApartmentRepository apartmentRepository, ApartmentValidator apartmentValidator) {
        this.apartmentRepository = apartmentRepository;
        this.apartmentValidator = apartmentValidator;
    }

    @Override
    public Apartment createApartment(Apartment apartment) {
        apartmentValidator.validateNewApartment(apartment);
        return apartmentRepository.create(apartment);
    }

    @Override
    public Optional<Apartment> getApartment(int id) {
        return apartmentRepository.findOne(id);
    }

    @Override
    public List<Apartment> getAllApartments() {
        return apartmentRepository.findAll();
    }

    @Override
    public void updateApartment(Apartment apartment) {
        if (doesExist(apartment.id())) {
            apartmentValidator.validateExistingApartment(apartment);
            apartmentRepository.update(apartment);
        } else {
            throw new ApartmentNotFoundException(String.format("Apartment with id %s does not exist", apartment.id()));
        }
    }

    @Override
    public boolean doesExist(Integer id) {
        if (id == null) {
            return false;
        }
        return apartmentRepository.exists(id);
    }
}
