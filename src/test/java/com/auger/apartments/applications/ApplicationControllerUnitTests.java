package com.auger.apartments.applications;

import com.auger.apartments.exceptions.ApplicationNotFoundException;
import com.auger.apartments.exceptions.UserNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static com.auger.apartments.TestUtils.assertApplicationsAreEqual;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.in;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
public class ApplicationControllerUnitTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ApplicationService applicationService;

    @Test
    public void testCreateApplication() throws Exception {
        Application application =
                new Application(null, null, true, false, 1, 2);

        when(applicationService.createApplication(application)).thenReturn(application);

        String applicationJson = objectMapper.writeValueAsString(application);

        MvcResult result = mockMvc.perform(post("/applications")
                .content(applicationJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        Application createdApplication = objectMapper.readValue(responseString, Application.class);

        assertApplicationsAreEqual(application, createdApplication);
        verify(applicationService, times(1)).createApplication(application);
    }

    @Test
    public void testCreateApplicationInvalidUser() throws Exception {
        Application application =
                new Application(null, null, true, false, 0, 2);

        doThrow(new UserNotFoundException(String.format("User with id %s does not exist", application.userId())))
                .when(applicationService).createApplication(application);

        String applicationJson = objectMapper.writeValueAsString(application);

        mockMvc.perform(post("/applications")
                        .content(applicationJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(String.format("User with id %s does not exist", application.userId())));

        verify(applicationService, times(1)).createApplication(application);
    }

    @Test
    public void testCreateApplicationInvalidApartment() throws Exception {
        Application application =
                new Application(null, null, true, false, 1, 0);

        doThrow(new UserNotFoundException(String.format("User with id %s does not exist", application.apartmentId())))
                .when(applicationService).createApplication(application);

        String applicationJson = objectMapper.writeValueAsString(application);

        mockMvc.perform(post("/applications")
                        .content(applicationJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(
                        String.format("User with id %s does not exist", application.apartmentId())
                ));

        verify(applicationService, times(1)).createApplication(application);
    }

    @Test
    public void testGetApplication() throws Exception {
        Application application =
                new Application(1, null, true, false, 1, 2);

        when(applicationService.getApplication(application.id())).thenReturn(Optional.of(application));

        MvcResult result = mockMvc.perform(get("/applications/{id}", application.id()))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        Application retrievedApplication = objectMapper.readValue(responseString, Application.class);

        assertApplicationsAreEqual(application, retrievedApplication);
        verify(applicationService, times(1)).getApplication(application.id());
    }

    @Test
    public void testGetApplicationInvalidId() throws Exception {
        int invalidApplicationId = 1;

        when(applicationService.getApplication(invalidApplicationId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/applications/{id}", invalidApplicationId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(
                        String.format("Application with id %s does not exist", invalidApplicationId)
                ));

        verify(applicationService, times(1)).getApplication(invalidApplicationId);
    }

    @Test
    public void testGetAllApplications() throws Exception {
        Application application1 =
                new Application(1, null, true, false, 1, 2);
        Application application2 =
                new Application(2, null, true, false, 2, 3);
        Application application3 =
                new Application(3, null, true, false, 3, 4);

        List<Application> applicationList = List.of(application1, application2, application3);
        when(applicationService.getAllApplications()).thenReturn(applicationList);

        MvcResult result = mockMvc.perform(get("/applications"))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        CollectionType collectionType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, Application.class);
        List<Application> allApplications = objectMapper.readValue(responseString, collectionType);

        assertThat(allApplications).isEqualTo(applicationList);
        verify(applicationService, times(1)).getAllApplications();
    }

    @Test
    public void testGetAllApplicationsEmptyResponse() throws Exception {
        List<Application> empytList = List.of();
        when(applicationService.getAllApplications()).thenReturn(empytList);

        MvcResult result = mockMvc.perform(get("/applications"))
                .andExpect(status().isOk())
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        CollectionType collectionType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, Application.class);
        List<Application> emptyListResponse = objectMapper.readValue(responseString, collectionType);

        assertThat(emptyListResponse).isEqualTo(empytList);
        verify(applicationService, times(1)).getAllApplications();
    }

    @Test
    public void testUpdateApplication() throws Exception {
        Application application =
                new Application(1, null, true, false, 1, 2);

        doNothing().when(applicationService).updateApplication(application);

        String applicationJson = objectMapper.writeValueAsString(application);

        mockMvc.perform(put("/applications")
                .content(applicationJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(applicationService, times(1)).updateApplication(application);
    }

    @Test
    public void testUpdateApplicationInvalidId() throws Exception {
        Application application =
                new Application(0, null, true, false, 1, 2);

        doThrow(new ApplicationNotFoundException(
                String.format("Application with id %s does not exist", application.id())
        )).when(applicationService).updateApplication(application);

        String applicationJson = objectMapper.writeValueAsString(application);

        mockMvc.perform(put("/applications")
                        .content(applicationJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(String.format("Application with id %s does not exist", application.id())));

        verify(applicationService, times(1)).updateApplication(application);
    }

    @Test
    public void testUpdateApplicationNullId() throws Exception {
        Application application =
                new Application(null, null, true, false, 1, 2);

        doThrow(new ApplicationNotFoundException(
                String.format("Application with id %s does not exist", application.id())
        )).when(applicationService).updateApplication(application);

        String applicationJson = objectMapper.writeValueAsString(application);

        mockMvc.perform(put("/applications")
                        .content(applicationJson)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(String.format("Application with id %s does not exist", application.id())));

        verify(applicationService, times(1)).updateApplication(application);
    }

    @Test
    public void testDeleteApplication() throws Exception {
        int validApplicationId = 1;

        doNothing().when(applicationService).deleteApplication(validApplicationId);

        mockMvc.perform(delete("/applications/{id}", validApplicationId))
                .andExpect(status().isNoContent());

        verify(applicationService, times(1)).deleteApplication(validApplicationId);
    }

    @Test
    public void testDeleteApplicationInvalidId() throws Exception {
        int invalidApplicationId = 2;

        doThrow(new ApplicationNotFoundException(
                String.format("Application with id %s does not exist", invalidApplicationId)
        )).when(applicationService).deleteApplication(invalidApplicationId);

        mockMvc.perform(delete("/applications/{id}", invalidApplicationId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(
                        String.format("Application with id %s does not exist", invalidApplicationId)
                ));

        verify(applicationService, times(1)).deleteApplication(invalidApplicationId);
    }
}
