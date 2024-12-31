package com.auger.apartments.apartments;

public interface ApartmentService {
    Apartment createApartment(Apartment apartment);

    boolean doesExist(Integer id);
}
