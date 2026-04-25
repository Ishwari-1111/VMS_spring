package vms.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * StandardizedDTO for Certificate API responses
 * Ensures consistent structure across all certificate endpoints
 */
public record CertificateResponse(
    String certificateId,
    String certificateCode,
    String volunteerId,
    String volunteerName,
    String eventId,
    String eventName,
    LocalDate completionDate,
    LocalDateTime issuedDate,
    String status
) {
    
    /**
     * Create response from Certificate model
     */
    public static CertificateResponse fromCertificate(vms.model.Certificate cert) {
        return new CertificateResponse(
            cert.getCertificateId(),
            cert.getCertificateCode(),
            cert.getVolunteer().getVolunteerId(),
            cert.getVolunteer().getName(),
            cert.getEvent().getEventId(),
            cert.getEvent().getEventName(),
            cert.getEvent().getFinishDate() != null ? cert.getEvent().getFinishDate() : cert.getEvent().getDate(),
            cert.getIssuedDate(),
            cert.getStatus()
        );
    }
}
