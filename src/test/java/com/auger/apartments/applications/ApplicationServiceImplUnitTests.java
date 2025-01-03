package com.auger.apartments.applications;

import com.auger.apartments.exceptions.ApartmentNotFoundException;
import com.auger.apartments.exceptions.ApplicationNotFoundException;
import com.auger.apartments.exceptions.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.auger.apartments.TestUtils.assertApplicationsAreEqual;
import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceImplUnitTests {

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    ApplicationValidator applicationValidator;

    @InjectMocks
    ApplicationServiceImpl underTest;

    @Test
    public void testCreateApplication() {
        Application application =
                new Application(1, null, true, false, 1, 2);

        doNothing().when(applicationValidator).validateNewApplication(application);
        when(applicationRepository.create(application)).thenReturn(application);

        Application createdApplication = underTest.createApplication(application);

        verify(applicationValidator, times(1)).validateNewApplication(application);
        verify(applicationRepository, times(1)).create(application);
        assertApplicationsAreEqual(application, createdApplication);
    }

    @Test
    public void testCreateApplicationInvalidUser() {
        Application application =
                new Application(1, null, true, false, 0, 2);

        doThrow(new UserNotFoundException(String.format("User with id %s does not exist", application.userId())))
                .when(applicationValidator).validateNewApplication(application);

        assertThatThrownBy(() -> underTest.createApplication(application))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", application.userId()));
        verify(applicationValidator, times(1)).validateNewApplication(application);
        verifyNoInteractions(applicationRepository);
    }

    @Test
    public void testCreateApplicationInvalidApartment() {
        Application application =
                new Application(1, null, true, false, 1, 0);

        doThrow(new ApartmentNotFoundException(
                String.format("Apartment with id %s does not exist", application.apartmentId()))
        ).when(applicationValidator).validateNewApplication(application);

        assertThatThrownBy(() -> underTest.createApplication(application))
                .isInstanceOf(ApartmentNotFoundException.class)
                .hasMessage(String.format("Apartment with id %s does not exist", application.apartmentId()));
        verify(applicationValidator, times(1)).validateNewApplication(application);
        verifyNoInteractions(applicationRepository);
    }

    @Test
    public void testGetApplication() {
        Application application =
                new Application(1, null, true, false, 1, 2);
        int invalidApplicationId = 2;

        when(applicationRepository.findOne(application.id())).thenReturn(Optional.of(application));
        when(applicationRepository.findOne(invalidApplicationId)).thenReturn(Optional.empty());

        Optional<Application> retrievedApartment = underTest.getApplication(application.id());
        assertThat(retrievedApartment).isPresent();
        assertApplicationsAreEqual(application, retrievedApartment.get());
        verify(applicationRepository, times(1)).findOne(application.id());

        Optional<Application> emptyApplication = underTest.getApplication(invalidApplicationId);
        assertThat(emptyApplication).isNotPresent();
        verify(applicationRepository, times(1)).findOne(invalidApplicationId);
    }

    @Test
    public void testGetAllApplications() {
        Application application1 =
                new Application(1, null, true, false, 1, 2);
        Application application2 =
                new Application(2, null, true, false, 2, 3);
        Application application3 =
                new Application(3, null, true, false, 3, 4);

        List<Application> emptyList = List.of();
        List<Application> applicationList = List.of(application1, application2, application3);

        when(applicationRepository.findAll()).thenReturn(emptyList);
        List<Application> noApplications = underTest.getAllApplications();
        assertThat(noApplications.size()).isZero();
        assertThat(noApplications).isEqualTo(emptyList);
        verify(applicationRepository, times(1)).findAll();

        when(applicationRepository.findAll()).thenReturn(applicationList);
        List<Application> multipleApplications = underTest.getAllApplications();
        assertThat(multipleApplications.size()).isEqualTo(applicationList.size());
        assertThat(multipleApplications).isEqualTo(applicationList);
        verify(applicationRepository, times(2)).findAll();
    }

    @Test
    public void testUpdateApplication() {
        Application application =
                new Application(1, null, true, false, 1, 2);

        when(applicationRepository.exists(application.id())).thenReturn(true);
        doNothing().when(applicationRepository).update(application);

        assertThatNoException().isThrownBy(() -> underTest.updateApplication(application));

        verify(applicationRepository, times(1)).exists(application.id());
        verify(applicationRepository, times(1)).update(application);
    }

    @Test
    public void testUpdateApplicationInvalidId() {
        Application application =
                new Application(0, null, true, false, 1, 2);

        when(applicationRepository.exists(application.id())).thenReturn(false);

        assertThatThrownBy(() -> underTest.updateApplication(application))
                .isInstanceOf(ApplicationNotFoundException.class)
                .hasMessage(String.format("Application with id %s does not exist", application.id()));

        verify(applicationRepository, times(1)).exists(application.id());
        verify(applicationRepository, times(0)).update(application);
    }

    @Test
    public void testUpdateApplicationNullId() {
        Application application =
                new Application(null, null, true, false, 1, 2);

        assertThatThrownBy(() -> underTest.updateApplication(application))
                .isInstanceOf(ApplicationNotFoundException.class)
                .hasMessage(String.format("Application with id %s does not exist", application.id()));

        verifyNoInteractions(applicationRepository);
    }

    @Test
    public void testDoesExist() {
        int validApplicationId = 1;
        int invalidApplicationId = 2;

        when(applicationRepository.exists(validApplicationId)).thenReturn(true);
        when(applicationRepository.exists(invalidApplicationId)).thenReturn(false);

        assertThat(underTest.doesExist(validApplicationId)).isTrue();
        verify(applicationRepository, times(1)).exists(validApplicationId);

        assertThat(underTest.doesExist(invalidApplicationId)).isFalse();
        verify(applicationRepository, times(1)).exists(invalidApplicationId);

        assertThat(underTest.doesExist(null)).isFalse();
    }
}
