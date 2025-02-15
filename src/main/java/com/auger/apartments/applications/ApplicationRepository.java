package com.auger.apartments.applications;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository {
    Application create(Application application);

    Optional<Application> findOne(int id);

    List<Application> findAll();

    void update(Application application);

    void delete(int id);

    boolean exists(int id);
}
