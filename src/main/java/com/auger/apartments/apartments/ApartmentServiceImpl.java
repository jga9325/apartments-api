package com.auger.apartments.apartments;

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
    public boolean doesExist(Integer id) {
        if (id == null) {
            return false;
        }
        return apartmentRepository.exists(id);
    }
}
