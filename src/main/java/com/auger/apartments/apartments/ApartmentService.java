package com.auger.apartments.apartments;

import java.util.List;
import java.util.Optional;

public interface ApartmentService {
    Apartment createApartment(Apartment apartment);

    Optional<Apartment> getApartment(int id);

    List<Apartment> getAllApartments();

    void updateApartment(Apartment apartment);

    void deleteApartment(int id);

    boolean doesExist(Integer id);
}
