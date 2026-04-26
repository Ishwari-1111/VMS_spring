package vms.service;

import vms.model.Certificate;
import vms.model.Event;
import vms.model.Volunteer;
import vms.repository.CertificateRepository;
import vms.repository.EventRepository;
import vms.repository.VolunteerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CertificateService {
    
    @Autowired
    private CertificateRepository certificateRepository;
    
    @Autowired
    private VolunteerRepository volunteerRepository;
    
    @Autowired
    private EventRepository eventRepository;

    /**
     * Get all certificates (admin view)
     */
    public List<Certificate> getAllCertificates() {
        return certificateRepository.findAll();
    }
    
    /**
     * Generate certificates for all volunteers who participated in an event
     * This is the AUTOMATED process that happens when an event is completed
     */
    public List<Certificate> generateCertificatesForEvent(String eventId) {
        // Get the event
        Optional<Event> event = eventRepository.findById(eventId);
        if (!event.isPresent()) {
            throw new IllegalArgumentException("Event not found: " + eventId);
        }
        
        Event eventObj = event.get();
        List<Certificate> generatedCertificates = new java.util.ArrayList<>();
        
        // Get all volunteers for this event
        List<Volunteer> eventVolunteers = volunteerRepository.findVolunteersByEventId(eventId);
        
        // Generate certificate for each volunteer if not already generated
        for (Volunteer volunteer : eventVolunteers) {
            // Check if certificate already exists
            if (!certificateRepository.existsByVolunteer_VolunteerIdAndEvent_EventId(
                    volunteer.getVolunteerId(), eventId)) {
                
                // Create and save certificate
                Certificate certificate = new Certificate(volunteer, eventObj);
                Certificate savedCertificate = certificateRepository.save(certificate);
                generatedCertificates.add(savedCertificate);
                
                System.out.println("✅ Certificate generated for " + volunteer.getName() 
                    + " for event " + eventObj.getEventName());
            }
        }
        
        return generatedCertificates;
    }
    
    /**
     * Generate certificate for a specific volunteer for a specific event
     */
    public Certificate generateCertificateForVolunteer(String volunteerId, String eventId) {
        // Check if volunteer exists
        Optional<Volunteer> volunteer = volunteerRepository.findById(volunteerId);
        if (!volunteer.isPresent()) {
            throw new IllegalArgumentException("Volunteer not found: " + volunteerId);
        }
        
        // Check if event exists
        Optional<Event> event = eventRepository.findById(eventId);
        if (!event.isPresent()) {
            throw new IllegalArgumentException("Event not found: " + eventId);
        }
        
        // Check if certificate already exists
        if (certificateRepository.existsByVolunteer_VolunteerIdAndEvent_EventId(volunteerId, eventId)) {
            throw new IllegalArgumentException("Certificate already exists for this volunteer and event");
        }
        
        // Create and save certificate
        Certificate certificate = new Certificate(volunteer.get(), event.get());
        Certificate savedCertificate = certificateRepository.save(certificate);
        
        System.out.println("✅ Certificate generated for " + volunteer.get().getName() 
            + " for event " + event.get().getEventName());
        
        return savedCertificate;
    }
    
    /**
     * Get all certificates for a volunteer
     */
    public List<Certificate> getCertificatesByVolunteer(String volunteerId) {
        // Check if volunteer exists
        if (!volunteerRepository.existsById(volunteerId)) {
            throw new IllegalArgumentException("Volunteer not found: " + volunteerId);
        }
        return certificateRepository.findByVolunteer_VolunteerId(volunteerId);
    }
    
    /**
     * Get all certificates generated for an event
     */
    public List<Certificate> getCertificatesByEvent(String eventId) {
        // Check if event exists
        if (!eventRepository.existsById(eventId)) {
            throw new IllegalArgumentException("Event not found: " + eventId);
        }
        return certificateRepository.findByEvent_EventId(eventId);
    }
    
    /**
     * Get a specific certificate
     */
    public Certificate getCertificateById(String certificateId) {
        return certificateRepository.findById(certificateId)
            .orElseThrow(() -> new IllegalArgumentException("Certificate not found: " + certificateId));
    }
    
    /**
     * Get certificate by certificate code (for verification)
     */
    public Certificate getCertificateByCode(String certificateCode) {
        return certificateRepository.findByCertificateCode(certificateCode)
            .orElseThrow(() -> new IllegalArgumentException("Certificate not found with code: " + certificateCode));
    }
    
    /**
     * Get specific certificate for a volunteer-event pair
     */
    public Certificate getCertificateForVolunteerEvent(String volunteerId, String eventId) {
        return certificateRepository.findByVolunteer_VolunteerIdAndEvent_EventId(volunteerId, eventId)
            .orElseThrow(() -> new IllegalArgumentException("Certificate not found for volunteer " 
                + volunteerId + " and event " + eventId));
    }
    
    /**
     * Get count of certificates generated for an event
     */
    public long getCertificateCountForEvent(String eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new IllegalArgumentException("Event not found: " + eventId);
        }
        return certificateRepository.countByEvent_EventId(eventId);
    }
    
    /**
     * Verify certificate authenticity by code
     */
    public boolean verifyCertificate(String certificateCode) {
        return certificateRepository.findByCertificateCode(certificateCode).isPresent();
    }
}
