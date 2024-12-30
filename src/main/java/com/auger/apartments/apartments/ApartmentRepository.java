package com.auger.apartments.apartments;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ApartmentRepository {
    Apartment createApartment(Apartment apartment);

    Optional<Apartment> findOne(Integer id);

    List<Apartment> findAll();

    void updateApartment(Apartment apartment);

    boolean exists(int id);
}
