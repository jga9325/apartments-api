package com.auger.apartments.apartments;

import com.auger.apartments.exceptions.DuplicateDataException;
import com.auger.apartments.exceptions.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApartmentServiceImplUnitTests {

    @Mock
    ApartmentRepository apartmentRepository;

    @Mock
    ApartmentValidator apartmentValidator;

    @InjectMocks
    ApartmentServiceImpl underTest;

    @Test
    public void testCreateApartment() {
        Apartment apartment = new Apartment(1, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, null);

        doNothing().when(apartmentValidator).validateNewApartment(apartment);
        when(apartmentRepository.create(apartment)).thenReturn(apartment);

        Apartment createdApartment = underTest.createApartment(apartment);

        verify(apartmentValidator, times(1)).validateNewApartment(apartment);
        verify(apartmentRepository, times(1)).create(apartment);
        assertApartmentsAreEqual(apartment, createdApartment);
    }

    @Test
    public void testCreateApartmentInvalidOwner() {
        Apartment apartment = new Apartment(1, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, null);

        doThrow(new UserNotFoundException(String.format("User with id %s does not exist", apartment.ownerId())))
                .when(apartmentValidator).validateNewApartment(apartment);

        assertThatThrownBy(() -> underTest.createApartment(apartment))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", apartment.ownerId()));
        verify(apartmentValidator, times(1)).validateNewApartment(apartment);
        verifyNoInteractions(apartmentRepository);
    }

    @Test
    public void testCreateApartmentInvalidRenter() {
        Apartment apartment = new Apartment(1, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, 0);

        doThrow(new UserNotFoundException(String.format("User with id %s does not exist", apartment.renterId())))
                .when(apartmentValidator).validateNewApartment(apartment);

        assertThatThrownBy(() -> underTest.createApartment(apartment))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(String.format("User with id %s does not exist", apartment.renterId()));
        verify(apartmentValidator, times(1)).validateNewApartment(apartment);
        verifyNoInteractions(apartmentRepository);
    }

    @Test
    public void testCreateApartmentDuplicateRenter() {
        Apartment apartment = new Apartment(1, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, 2);

        doThrow(new DuplicateDataException(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, apartment.renterId())))
                .when(apartmentValidator).validateNewApartment(apartment);

        assertThatThrownBy(() -> underTest.createApartment(apartment))
                .isInstanceOf(DuplicateDataException.class)
                .hasMessage(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, apartment.renterId()));
        verify(apartmentValidator, times(1)).validateNewApartment(apartment);
        verifyNoInteractions(apartmentRepository);
    }

    @Test
    public void testGetApartment() {
        Apartment apartment = new Apartment(1, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, null);
        int nonExistingApartmentId = 2;

        when(apartmentRepository.findOne(apartment.id())).thenReturn(Optional.of(apartment));
        when(apartmentRepository.findOne(nonExistingApartmentId)).thenReturn(Optional.empty());

        Optional<Apartment> retrievedApartment = underTest.getApartment(apartment.id());
        assertThat(retrievedApartment).isPresent();
        assertApartmentsAreEqual(apartment, retrievedApartment.get());
        verify(apartmentRepository, times(1)).findOne(apartment.id());

        Optional<Apartment> emptyApartment = underTest.getApartment(nonExistingApartmentId);
        assertThat(emptyApartment).isNotPresent();
        verify(apartmentRepository, times(1)).findOne(nonExistingApartmentId);
    }

    @Test
    public void testGetAllApartments() {
        Apartment apartment1 = new Apartment(null, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, null);
        Apartment apartment2 = new Apartment(null, "Comfy Studio",
                "Studio space in downtown Manhattan. Great location", 0,
                1, "NY", "New York", 400, 280000,
                null, true, 2, null);
        Apartment apartment3 = new Apartment(null, "Beach Stay",
                "Secluded home, perfect for a quiet and relaxing getaway.", 2,
                2, "HI", "Honolulu", 400, 280000,
                null, true, 3, null);

        List<Apartment> emptyList = List.of();
        List<Apartment> apartmentList = List.of(apartment1, apartment2, apartment3);

        when(apartmentRepository.findAll()).thenReturn(emptyList);
        List<Apartment> noApartments = underTest.getAllApartments();
        assertThat(noApartments.size()).isZero();
        assertThat(noApartments).isEqualTo(emptyList);
        verify(apartmentRepository, times(1)).findAll();

        when(apartmentRepository.findAll()).thenReturn(apartmentList);
        List<Apartment> multipleApartments = underTest.getAllApartments();
        assertThat(multipleApartments.size()).isEqualTo(apartmentList.size());
        assertThat(multipleApartments).isEqualTo(apartmentList);
        verify(apartmentRepository, times(2)).findAll();
    }

    @Test
    public void testUpdateApartment() {

    }

    @Test
    public void testUpdateApartmentInvalidOwner() {

    }

    @Test
    public void testUpdateApartmentInvalidRenter() {

    }

    @Test
    public void testUpdateApartmentDuplicateRenter() {

    }

    @Test
    public void testUpdateApartmentInvalidId() {

    }

    private void assertApartmentsAreEqual(Apartment a1, Apartment a2) {
        assertThat(a1.id()).isEqualTo(a2.id());
        assertThat(a1.title()).isEqualTo(a2.title());
        assertThat(a1.description()).isEqualTo(a2.description());
        assertThat(a1.numberOfBedrooms()).isEqualTo(a2.numberOfBedrooms());
        assertThat(a1.numberOfBathrooms()).isEqualTo(a2.numberOfBathrooms());
        assertThat(a1.state()).isEqualTo(a2.state());
        assertThat(a1.city()).isEqualTo(a2.city());
        assertThat(a1.squareFeet()).isEqualTo(a2.squareFeet());
        assertThat(a1.monthlyRent()).isEqualTo(a2.monthlyRent());
        assertThat(a1.dateListed()).isEqualTo(a2.dateListed());
        assertThat(a1.available()).isEqualTo(a2.available());
        assertThat(a1.ownerId()).isEqualTo(a2.ownerId());
        assertThat(a1.renterId()).isEqualTo(a2.renterId());
    }
}
