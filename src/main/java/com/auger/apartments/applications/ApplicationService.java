package com.auger.apartments.applications;

import java.util.List;
import java.util.Optional;

public interface ApplicationService {
    Application createApplication(Application application);

    Optional<Application> getApplication(int id);

    List<Application> getAllApplications();

    void updateApplication(Application application);

    void deleteApplication(int id);

    boolean doesExist(Integer id);
}
