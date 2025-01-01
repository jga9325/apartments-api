package com.auger.apartments.apartments;

import com.auger.apartments.exceptions.ApartmentNotFoundException;
import com.auger.apartments.exceptions.DuplicateDataException;
import com.auger.apartments.exceptions.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApartmentController.class)
public class ApartmentControllerUnitTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ApartmentService apartmentService;

    @Test
    public void testCreateApartment() throws Exception {
        Apartment apartment = new Apartment(1, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, null);

        when(apartmentService.createApartment(apartment)).thenReturn(apartment);

        String apartmentJson = objectMapper.writeValueAsString(apartment);

        MvcResult result = mockMvc.perform(post("/apartments")
                        .content(apartmentJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        Apartment createdApartment = objectMapper.readValue(responseString, Apartment.class);

        assertApartmentsAreEqual(apartment, createdApartment);
        verify(apartmentService, times(1)).createApartment(apartment);
    }

    @Test
    public void testCreateApartmentInvalidOwner() throws Exception {
        Apartment apartment = new Apartment(1, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 0, null);

        doThrow(new UserNotFoundException(String.format("User with id %s does not exist", apartment.ownerId())))
                .when(apartmentService).createApartment(apartment);

        String apartmentJson = objectMapper.writeValueAsString(apartment);

        mockMvc.perform(post("/apartments")
                .content(apartmentJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(String.format("User with id %s does not exist", apartment.ownerId())));

        verify(apartmentService, times(1)).createApartment(apartment);
    }

    @Test
    public void testCreateApartmentInvalidRenter() throws Exception {
        Apartment apartment = new Apartment(1, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, 0);

        doThrow(new UserNotFoundException(String.format("User with id %s does not exist", apartment.renterId())))
                .when(apartmentService).createApartment(apartment);

        String apartmentJson = objectMapper.writeValueAsString(apartment);

        mockMvc.perform(post("/apartments")
                        .content(apartmentJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(String.format("User with id %s does not exist", apartment.renterId())));

        verify(apartmentService, times(1)).createApartment(apartment);
    }

    @Test
    public void testCreateApartmentDuplicateRenter() throws Exception {
        Apartment apartment = new Apartment(1, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, 2);

        doThrow(new DuplicateDataException(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, apartment.renterId())))
                .when(apartmentService).createApartment(apartment);

        String apartmentJson = objectMapper.writeValueAsString(apartment);

        mockMvc.perform(post("/apartments")
                        .content(apartmentJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, apartment.renterId())));

        verify(apartmentService, times(1)).createApartment(apartment);
    }

    @Test
    public void testGetApartment() throws Exception {
        Apartment apartment = new Apartment(1, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, 2);

        when(apartmentService.getApartment(apartment.id())).thenReturn(Optional.of(apartment));

        MvcResult result = mockMvc.perform(get("/apartments/{id}", apartment.id()))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        Apartment retrievedApartment = objectMapper.readValue(responseString, Apartment.class);

        assertApartmentsAreEqual(apartment, retrievedApartment);
        verify(apartmentService, times(1)).getApartment(apartment.id());
    }

    @Test
    public void testGetApartmentInvalidId() throws Exception {
        int apartmentId = 1;

        when(apartmentService.getApartment(apartmentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/apartments/{id}", apartmentId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(String.format("Apartment with id %s does not exist", apartmentId)));

        verify(apartmentService, times(1)).getApartment(apartmentId);
    }

    @Test
    public void testGetAllApartments() throws Exception {
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

        List<Apartment> apartmentList = List.of(apartment1, apartment2, apartment3);
        when(apartmentService.getAllApartments()).thenReturn(apartmentList);

        MvcResult result = mockMvc.perform(get("/apartments"))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, Apartment.class);
        List<Apartment> allApartments = objectMapper.readValue(responseString, collectionType);

        assertThat(allApartments).isEqualTo(apartmentList);
        verify(apartmentService, times(1)).getAllApartments();
    }

    @Test
    public void testGetAllApartmentsEmptyResponse() throws Exception {
        List<Apartment> emptyList = List.of();
        when(apartmentService.getAllApartments()).thenReturn(emptyList);

        MvcResult result = mockMvc.perform(get("/apartments"))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, Apartment.class);
        List<Apartment> emptyListResponse = objectMapper.readValue(responseString, collectionType);

        assertThat(emptyListResponse).isEqualTo(emptyList);
        verify(apartmentService, times(1)).getAllApartments();
    }

    @Test
    public void testUpdateApartment() throws Exception {
        Apartment apartment = new Apartment(1, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, null);

        doNothing().when(apartmentService).updateApartment(apartment);

        String apartmentJson = objectMapper.writeValueAsString(apartment);

        mockMvc.perform(put("/apartments")
                        .content(apartmentJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(apartmentService, times(1)).updateApartment(apartment);
    }

    @Test
    public void testUpdateApartmentInvalidId() throws Exception {
        Apartment apartment = new Apartment(1, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, null);

        doThrow(new ApartmentNotFoundException(String.format("Apartment with id %s does not exist", apartment.id())))
                .when(apartmentService).updateApartment(apartment);

        String apartmentJson = objectMapper.writeValueAsString(apartment);

        mockMvc.perform(put("/apartments")
                        .content(apartmentJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(String.format("Apartment with id %s does not exist", apartment.id())));

        verify(apartmentService, times(1)).updateApartment(apartment);
    }

    @Test
    public void testUpdateApartmentInvalidOwner() throws Exception {
        Apartment apartment = new Apartment(1, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, null);

        doThrow(new UserNotFoundException(String.format("User with id %s does not exist", apartment.ownerId())))
                .when(apartmentService).updateApartment(apartment);

        String apartmentJson = objectMapper.writeValueAsString(apartment);

        mockMvc.perform(put("/apartments")
                        .content(apartmentJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(String.format("User with id %s does not exist", apartment.ownerId())));

        verify(apartmentService, times(1)).updateApartment(apartment);
    }

    @Test
    public void testUpdateApartmentInvalidRenter() throws Exception {
        Apartment apartment = new Apartment(1, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, null);

        doThrow(new UserNotFoundException(String.format("User with id %s does not exist", apartment.renterId())))
                .when(apartmentService).updateApartment(apartment);

        String apartmentJson = objectMapper.writeValueAsString(apartment);

        mockMvc.perform(put("/apartments")
                        .content(apartmentJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(String.format("User with id %s does not exist", apartment.renterId())));

        verify(apartmentService, times(1)).updateApartment(apartment);
    }

    @Test
    public void testUpdateApartmentDuplicateRenter() throws Exception {
        Apartment apartment = new Apartment(1, "Main Street Condo",
                "A spacious condo with brand new appliances and great views!", 2,
                1, "NY", "New York", 800, 608900,
                null, true, 1, null);

        doThrow(new DuplicateDataException(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, apartment.renterId())))
                .when(apartmentService).updateApartment(apartment);

        String apartmentJson = objectMapper.writeValueAsString(apartment);

        mockMvc.perform(put("/apartments")
                        .content(apartmentJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string(String.format("""
                    A user with id %s is renting a different apartment.
                    A user can only rent one apartment at a time.
                    """, apartment.renterId())));

        verify(apartmentService, times(1)).updateApartment(apartment);
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
