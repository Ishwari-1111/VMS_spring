package vms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vms.model.Certificate;
import vms.model.Event;
import vms.model.Volunteer;
import vms.repository.CertificateRepository;
import vms.repository.EventRepository;
import vms.repository.VolunteerRepository;
import vms.service.CertificateService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private VolunteerRepository volunteerRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CertificateService certificateService;

    @Test
    void testGetAllCertificates_returnsRepositoryData() {
        when(certificateRepository.findAll()).thenReturn(Collections.emptyList());

        assertNotNull(certificateService.getAllCertificates());
        assertTrue(certificateService.getAllCertificates().isEmpty());
        verify(certificateRepository, times(2)).findAll();
    }

    @Test
    void testGenerateCertificateForVolunteer_success() {
        Volunteer volunteer = new Volunteer("V100", "Alice");
        Event event = new Event("E100", "Cleanup Drive", LocalDate.now());

        when(volunteerRepository.findById("V100")).thenReturn(Optional.of(volunteer));
        when(eventRepository.findById("E100")).thenReturn(Optional.of(event));
        when(certificateRepository.existsByVolunteer_VolunteerIdAndEvent_EventId("V100", "E100")).thenReturn(false);
        when(certificateRepository.save(any(Certificate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Certificate result = certificateService.generateCertificateForVolunteer("V100", "E100");

        assertNotNull(result);
        assertEquals("GENERATED", result.getStatus());
        assertEquals("V100", result.getVolunteer().getVolunteerId());
        assertEquals("E100", result.getEvent().getEventId());
        verify(certificateRepository, times(1)).save(any(Certificate.class));
    }

    @Test
    void testGenerateCertificateForVolunteer_duplicateCertificate_throws() {
        Volunteer volunteer = new Volunteer("V200", "Bob");
        Event event = new Event("E200", "Plantation", LocalDate.now());

        when(volunteerRepository.findById("V200")).thenReturn(Optional.of(volunteer));
        when(eventRepository.findById("E200")).thenReturn(Optional.of(event));
        when(certificateRepository.existsByVolunteer_VolunteerIdAndEvent_EventId("V200", "E200")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> certificateService.generateCertificateForVolunteer("V200", "E200")
        );

        assertTrue(ex.getMessage().contains("Certificate already exists"));
        verify(certificateRepository, never()).save(any(Certificate.class));
    }

    @Test
    void testVerifyCertificate_returnsTrueWhenCodeExists() {
        when(certificateRepository.findByCertificateCode("CERT-ABC")).thenReturn(Optional.of(mock(Certificate.class)));

        boolean result = certificateService.verifyCertificate("CERT-ABC");

        assertTrue(result);
        verify(certificateRepository, times(1)).findByCertificateCode("CERT-ABC");
    }
}
