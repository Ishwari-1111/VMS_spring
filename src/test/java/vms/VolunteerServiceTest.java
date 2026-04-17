package vms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

}
