package vms.dto;

import java.time.LocalDateTime;

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
            cert.getIssuedDate(),
            cert.getStatus()
        );
    }
}
