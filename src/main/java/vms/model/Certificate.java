package vms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @Column(name = "certificate_id")
    private String certificateId;

    @ManyToOne
    @JoinColumn(name = "volunteer_id", nullable = false)
    private Volunteer volunteer;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "certificate_code", unique = true, nullable = false)
    private String certificateCode;

    @Column(name = "issued_date", nullable = false)
    private LocalDateTime issuedDate;

    @Column(name = "status")
    private String status; // "GENERATED", "ISSUED"

    protected Certificate() {}

    public Certificate(Volunteer volunteer, Event event) {
        if (volunteer == null || event == null) {
            throw new IllegalArgumentException("Volunteer and Event cannot be null");
        }
        this.certificateId = UUID.randomUUID().toString();
        this.volunteer = volunteer;
        this.event = event;
        this.certificateCode = generateCertificateCode();
        this.issuedDate = LocalDateTime.now();
        this.status = "GENERATED";
    }

    private String generateCertificateCode() {
        // Format: CERT-VOLID-EVENTID-TIMESTAMP
        return "CERT-" + volunteer.getVolunteerId().substring(0, Math.min(3, volunteer.getVolunteerId().length()))
                + "-" + event.getEventId().substring(0, Math.min(3, event.getEventId().length()))
                + "-" + System.currentTimeMillis();
    }

    // Getters
    public String getCertificateId() { return certificateId; }
    public Volunteer getVolunteer() { return volunteer; }
    public Event getEvent() { return event; }
    public String getCertificateCode() { return certificateCode; }
    public LocalDateTime getIssuedDate() { return issuedDate; }
    public String getStatus() { return status; }

    // Setters
    public void setStatus(String status) {
        if (status == null || status.trim().isEmpty())
            throw new IllegalArgumentException("Status cannot be empty");
        this.status = status.trim();
    }

    @Override
    public String toString() {
        return "Certificate{" +
                "certificateId='" + certificateId + '\'' +
                ", volunteer=" + volunteer.getName() +
                ", event=" + event.getEventName() +
                ", certificateCode='" + certificateCode + '\'' +
                ", issuedDate=" + issuedDate +
                ", status='" + status + '\'' +
                '}';
    }
}
