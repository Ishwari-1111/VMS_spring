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
import vms.model.Event;
import vms.model.User;
import vms.repository.EventRepository;
import vms.repository.UserRepository;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // enables Mockito
class VolunteerServiceTest {
    @Mock
    private VolunteerRepository VolunteerRepository; // mock database

    @InjectMocks
    private VolunteerService volunteerService; // class we are testing

    @Test
    void testGetVolunteerbyId() {
        Volunteer mockVolunteer = new Volunteer("A100", "Arshia");
        when(VolunteerRepository.findById("A100")).thenReturn(Optional.of(mockVolunteer));
        Optional<Volunteer> result = volunteerService.getVolunteer("A100");
        volunteerService.getVolunteer("A100");
        assertNotNull(result.get());
        assertEquals("A100", result.get().getVolunteerId());
        assertEquals("Arshia", result.get().getName());
    }

    @Test
    void addVolunteer() {
        Volunteer mockVolunteer = new Volunteer("A100", "Arshia");
        when(VolunteerRepository.save(mockVolunteer)).thenReturn(mockVolunteer);
        volunteerService.addVolunteer("A100", "Arshia");
        verify(VolunteerRepository, times(1)).save(mockVolunteer);

    }

    @Test
    void testRemoveVolunteer_success() {
        when(VolunteerRepository.existsById("A100")).thenReturn(true);

        boolean result = volunteerService.removeVolunteer("A100");

        assertTrue(result);
        verify(VolunteerRepository, times(1)).deleteById("A100");
    }

    @Test
    void testRemoveVolunteer_notFound() {
        when(VolunteerRepository.existsById("A100")).thenReturn(false);

        boolean result = volunteerService.removeVolunteer("A100");

        assertFalse(result);
        verify(VolunteerRepository, never()).deleteById("A100");
    }

    @Test
    void testGetAllVolunteers() {
        when(VolunteerRepository.findAll()).thenReturn(Collections.emptyList());
        List<Volunteer> result = volunteerService.getAllVolunteers();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetVolunteerCount() {
        when(VolunteerRepository.count()).thenReturn(5L);
        long count = volunteerService.getVolunteerCount();
        assertEquals(5L, count);
    }

    @Test
    void testDeleteVolunteer_success() {
        when(VolunteerRepository.existsById("A100")).thenReturn(true);
        volunteerService.deleteVolunteer("A100");
        verify(VolunteerRepository, times(1)).deleteById("A100");
    }

    @Test
    void testDeleteVolunteer_notFound() {
        when(VolunteerRepository.existsById("A200")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> volunteerService.deleteVolunteer("A200"));
        verify(VolunteerRepository, never()).deleteById(anyString());
    }

    @Test
    void testAddVolunteer_duplicate() {
        when(VolunteerRepository.existsById("A100")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> volunteerService.addVolunteer("A100", "Arshia"));
        verify(VolunteerRepository, never()).save(any());
    }

    @ParameterizedTest
    @CsvSource({
            "A100, Arshia", // duplicate ID 1
            "A101, Bob",    // duplicate ID 2
            "A102, Charlie" // duplicate ID 3
    })
    void testAddVolunteer_ParameterizedDuplicate(String id, String name) {
        // Parameterized negative testing loop, testing multiple inputs bounded to fail
        when(VolunteerRepository.existsById(id)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> volunteerService.addVolunteer(id, name));
        verify(VolunteerRepository, never()).save(any());
    }
}
