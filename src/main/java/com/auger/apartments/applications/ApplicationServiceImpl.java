package com.auger.apartments.applications;

import com.auger.apartments.exceptions.ApplicationNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationValidator applicationValidator;

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                  ApplicationValidator applicationValidator) {
        this.applicationRepository = applicationRepository;
        this.applicationValidator = applicationValidator;
    }

    @Override
    public Application createApplication(Application application) {
        applicationValidator.validateNewApplication(application);
        return applicationRepository.create(application);
    }

    @Override
    public Optional<Application> getApplication(int id) {
        return applicationRepository.findOne(id);
    }

    @Override
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    @Override
    public void updateApplication(Application application) {
        if (doesExist(application.id())) {
            applicationRepository.update(application);
        } else {
            throw new ApplicationNotFoundException(
                    String.format("Application with id %s does not exist", application.id())
            );
        }
    }

    @Override
    public void deleteApplication(int id) {
        if (doesExist(id)) {
            applicationRepository.delete(id);
        } else {
            throw new ApplicationNotFoundException(
                    String.format("Application with id %s does not exist", id)
            );
        }
    }

    @Override
    public boolean doesExist(Integer id) {
        if (id == null) {
            return false;
        }
        return applicationRepository.exists(id);
    }
}
