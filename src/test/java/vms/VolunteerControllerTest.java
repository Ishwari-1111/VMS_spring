package vms;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vms.controller.VolunteerController;
import vms.model.Volunteer;
import vms.service.VolunteerService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import vms.security.JwtTokenProvider;

@WebMvcTest(VolunteerController.class)
@AutoConfigureMockMvc(addFilters = false)
class VolunteerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VolunteerService volunteerService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testGetAllVolunteers() throws Exception {
        when(volunteerService.getAllVolunteers()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/volunteers"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetVolunteerById_Found() throws Exception {
        Volunteer volunteer = new Volunteer("V1", "Alice");
        when(volunteerService.getVolunteer("V1")).thenReturn(java.util.Optional.of(volunteer));
        mockMvc.perform(get("/api/volunteers/V1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(volunteer)));
    }

    @Test
    void testGetVolunteerById_NotFound() throws Exception {
        when(volunteerService.getVolunteer("V2")).thenReturn(java.util.Optional.empty());
        mockMvc.perform(get("/api/volunteers/V2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddVolunteer_Success() throws Exception {
        Volunteer volunteer = new Volunteer("V3", "Bob");
        when(volunteerService.addVolunteer("V3", "Bob", null)).thenReturn(volunteer);
        mockMvc.perform(post("/api/volunteers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(volunteer)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(volunteer)));
    }

    @Test
    void testAddVolunteer_BadRequest() throws Exception {
        when(volunteerService.addVolunteer(anyString(), anyString(), nullable(String.class)))
                .thenThrow(new IllegalArgumentException("Volunteer exists"));
        
        Volunteer v = new Volunteer("V4", "Carol");
        mockMvc.perform(post("/api/volunteers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(v)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRemoveVolunteer_Found() throws Exception {
        when(volunteerService.removeVolunteer("V5")).thenReturn(true);
        mockMvc.perform(delete("/api/volunteers/V5"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testRemoveVolunteer_NotFound() throws Exception {
        when(volunteerService.removeVolunteer("V6")).thenReturn(false);
        mockMvc.perform(delete("/api/volunteers/V6"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetVolunteerCount() throws Exception {
        when(volunteerService.getVolunteerCount()).thenReturn(42L);
        mockMvc.perform(get("/api/volunteers/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }

    @ParameterizedTest
    @CsvSource({
            "V10, ''",   // valid ID, blank name
            "'', Bob",   // blank ID, valid name
            "'', ''"     // both blank
    })
    void testAddVolunteer_ValidationFailures(String id, String name) throws Exception {
        when(volunteerService.addVolunteer(anyString(), anyString(), nullable(String.class)))
                .thenThrow(new IllegalArgumentException("Invalid input"));

        Map<String, String> volunteerMap = new HashMap<>();
        volunteerMap.put("volunteerId", id);
        volunteerMap.put("name", name);

        mockMvc.perform(post("/api/volunteers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(volunteerMap)))
                .andExpect(status().isBadRequest());
    }
}
