package vms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vms.model.Volunteer;
import vms.repository.VolunteerRepository;
import vms.service.VolunteerService;
import vms.service.EventService;
import vms.model.Event;
import vms.model.User;
import vms.repository.EventRepository;
import vms.repository.UserRepository;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // enables Mockito
class EventServiceTest {
    @Mock
    private EventRepository eventRepository;

    @Mock
    private VolunteerRepository volunteerRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void testGetEvent_success() {
        Event mockEvent = new Event("E100", "Event 1", LocalDate.of(2026, 4, 17));

        when(eventRepository.findById("E100"))
                .thenReturn(Optional.of(mockEvent));

        Event result = eventService.getEvent("E100");

        assertNotNull(result);
        assertEquals("E100", result.getEventId());

        verify(eventRepository).findById("E100");
    }

    @Test
    void testGetEvent_notFound() {
        when(eventRepository.findById("E100"))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> eventService.getEvent("E100"));

        verify(eventRepository).findById("E100");
    }

    @Test
    void testCreateEvent_success() {
        Event newEvent = new Event("E100", "Event 1", LocalDate.of(2026, 4, 17));
        LocalDate finishDate = LocalDate.of(2026, 4, 18);

        when(eventRepository.existsById("E100")).thenReturn(false);
        when(eventRepository.save(newEvent)).thenReturn(newEvent);
        Event result = eventService.createEvent("E100", "Event 1", LocalDate.of(2026, 4, 17), finishDate);

        assertNotNull(result);
        assertEquals("E100", result.getEventId());

        verify(eventRepository).existsById("E100");
        verify(eventRepository).save(newEvent);
    }

    @Test
    void testCreateEvent_alreadyExists() {
        Event existingEvent = new Event("E100", "Event 1", LocalDate.of(2026, 4, 17));
        LocalDate finishDate = LocalDate.of(2026, 4, 18);

        when(eventRepository.existsById("E100")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> eventService.createEvent("E100", "Event 1", LocalDate.of(2026, 4, 17), finishDate));

        verify(eventRepository).existsById("E100");
        verify(eventRepository, never()).save(any());
    }

    @Test
    void testDeleteEvent_success() {
        when(eventRepository.existsById("E100")).thenReturn(true);

        eventService.deleteEvent("E100");

        verify(eventRepository).existsById("E100");
        verify(eventRepository).deleteById("E100");
    }

    @Test
    void testDeleteEvent_notFound() {
        when(eventRepository.existsById("E100")).thenReturn(false);

        assertThrows(NoSuchElementException.class,
                () -> eventService.deleteEvent("E100"));

        verify(eventRepository).existsById("E100");
        verify(eventRepository, never()).deleteById(any());
    }

    @ParameterizedTest
    @CsvSource({
            "E100, V1, Alice", // Event 100, Volunteer 1
            "E101, V2, Bob",   // Event 101, Volunteer 2
            "E102, V3, Charlie"// Event 102, Volunteer 3
    })
    void testEnrollVolunteer_ParameterizedSuccess(String eventId, String volunteerId, String volunteerName) {
        // Here we test positive service execution over multiple input data combinations
        Event mockEvent = new Event(eventId, "Sample Event", LocalDate.of(2026, 4, 17));
        Volunteer mockVol = new Volunteer(volunteerId, volunteerName);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(mockEvent));
        when(volunteerRepository.findById(volunteerId)).thenReturn(Optional.of(mockVol));

        boolean result = eventService.enrollVolunteer(eventId, volunteerId);

        assertTrue(result);
        verify(eventRepository).save(mockEvent); // Ensure save is called
    }
}
