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
import vms.controller.EventController;
import vms.model.Event;
import vms.service.EventService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import vms.model.Volunteer;
import java.util.Map;
import vms.security.JwtTokenProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void testGetAllEvents() throws Exception {
        when(eventService.getAllEvents()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetEventById() throws Exception {
        Event mockEvent = new Event("E1", "Sample Event", LocalDate.of(2026, 4, 20));
        when(eventService.getEvent("E1")).thenReturn(mockEvent);
        mockMvc.perform(get("/api/events/E1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(mockEvent)));
    }

    @Test
    void testCreateEvent() throws Exception {
        EventController.CreateEventRequest request = new EventController.CreateEventRequest("E2", "New Event", LocalDate.of(2026, 5, 1));
        Event created = new Event("E2", "New Event", LocalDate.of(2026, 5, 1));
        when(eventService.createEvent(request.eventId(), request.eventName(), request.date()))
                .thenReturn(created);
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(created)));
    }

    @Test
    void testDeleteEvent() throws Exception {
        mockMvc.perform(delete("/api/events/E3"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testEnrollVolunteer() throws Exception {
        mockMvc.perform(post("/api/events/E1/volunteers/V1"))
                .andExpect(status().isOk());
    }

    @Test
    void testUnenrollVolunteer() throws Exception {
        mockMvc.perform(delete("/api/events/E1/volunteers/V1"))
                .andExpect(status().isOk());
    }

    @Test
    void testLogHours() throws Exception {
        EventController.LogHoursRequest req = new EventController.LogHoursRequest(5);
        mockMvc.perform(post("/api/events/E1/volunteers/V1/hours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetStats() throws Exception {
        // Mock service to return Map<Volunteer, Integer>
        Volunteer volunteer = new Volunteer("V1", "Alice");
        Map<Volunteer, Integer> stats = new HashMap<>();
        stats.put(volunteer, 10);
        when(eventService.getEventStats("E1")).thenReturn(stats);
        // Expected JSON after controller maps keys to volunteer names
        Map<String, Integer> expected = new HashMap<>();
        expected.put("Alice", 10);
        mockMvc.perform(get("/api/events/E1/stats"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));
    }

    @ParameterizedTest
    @CsvSource({
            "'', Event Name",    // Blank ID
            "E1, ''"             // Blank Name
    })
    void testCreateEvent_ValidationFailures(String eventId, String eventName) throws Exception {
        // Date is set as valid, but ID or Name will be blank to trigger 400 Bad Request
        EventController.CreateEventRequest request = new EventController.CreateEventRequest(eventId, eventName, LocalDate.of(2026, 5, 1));
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
