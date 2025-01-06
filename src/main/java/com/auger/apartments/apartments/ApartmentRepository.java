package com.auger.apartments.apartments;

import java.util.List;
import java.util.Optional;

public interface ApartmentRepository {
    Apartment create(Apartment apartment);

    Optional<Apartment> findOne(Integer id);

    List<Apartment> findAll();

    void update(Apartment apartment);

    void delete(int id);

    boolean exists(int id);
}
