package vms.repository;

import vms.model.Certificate;
//import vms.model.Event;
//import vms.model.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, String> {
    
    // Get all certificates for a specific volunteer (using volunteer.volunteerId)
    List<Certificate> findByVolunteer_VolunteerId(String volunteerId);
    
    // Get all certificates for a specific event
    List<Certificate> findByEvent_EventId(String eventId);
    
    // Get certificate for a specific volunteer and event
    Optional<Certificate> findByVolunteer_VolunteerIdAndEvent_EventId(String volunteerId, String eventId);
    
    // Check if certificate exists for volunteer and event
    boolean existsByVolunteer_VolunteerIdAndEvent_EventId(String volunteerId, String eventId);
    
    // Get certificate by certificate code
    Optional<Certificate> findByCertificateCode(String certificateCode);
    
    // Count certificates for an event
    long countByEvent_EventId(String eventId);
}
