package vms.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
//import java.util.*;

@Entity
@Table(name = "volunteers")
public class Volunteer {
    @Id
    private String volunteerId;

    @Column(nullable = false)
    private String name;
    
    @Column(unique = true)
    @Email(message = "Email should be valid")
    private String email;

    // Note: EventVolunteer table manages the many-to-many relationship with Event
    // No need for @Transient enrolledEvents - use repository queries instead

    public Volunteer() {}

    public Volunteer(String volunteerId, String name) {
        if (volunteerId == null || volunteerId.trim().isEmpty())
            throw new IllegalArgumentException("volunteerId cannot be empty");
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("name cannot be empty");
        this.volunteerId = volunteerId;
        this.name = name.trim();
    }
    public String getVolunteerId() { return volunteerId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    
    public void setVolunteerId(String volunteerId) {
        this.volunteerId = volunteerId;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("name cannot be empty");
        this.name = name.trim();
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    // Note: Event enrollment is managed via EventVolunteer entity and VolunteerRepository.findVolunteersByEventId()
    // These methods below are kept for API compatibility but enrollment is actually managed in EventService
    
    public boolean volunteerForEvent(Event event) {
        if (event == null) return false;
        return event.addVolunteer(this);
    }

    public boolean unvolunteerFromEvent(Event event) {
        if (event == null) return false;
        return event.removeVolunteer(this);
    }

    public void logHours(Event event, int hours) {
        if (event == null)
            throw new IllegalStateException("Event cannot be null");
        event.logHours(this, hours);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return volunteerId.equals(((Volunteer) o).volunteerId);
    }

    @Override public int hashCode() { return volunteerId.hashCode(); }

    @Override
    public String toString() {
        return "Volunteer{id='" + volunteerId + "', name='" + name + "', email='" + email + "'}";
    }
    
}
