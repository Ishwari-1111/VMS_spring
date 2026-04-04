package vms.model;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "volunteers")
public class Volunteer {
    @Id
    private String volunteerId;

    private String name;

    @ManyToMany(mappedBy = "volunteers")
    private Set<Event> enrolledEvents = new HashSet<>();

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

    public void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("name cannot be empty");
        this.name = name.trim();
    }

    public boolean volunteerForEvent(Event event) {
        if (event == null) return false;
        boolean added = event.addVolunteer(this);
        if (added) enrolledEvents.add(event);
        return added;
    }

    public boolean unvolunteerFromEvent(Event event) {
        if (event == null) return false;
        boolean removed = event.removeVolunteer(this);
        if (removed) enrolledEvents.remove(event);
        return removed;
    }

    public void logHours(Event event, int hours) {
        if (!enrolledEvents.contains(event))
            throw new IllegalStateException("Volunteer not enrolled in event");
        event.logHours(this, hours);
    }
    public Set<Event> getEnrolledEvents() {
        return Collections.unmodifiableSet(enrolledEvents);
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
        return "Volunteer{id='" + volunteerId + "', name='" + name
             + "', events=" + enrolledEvents.size() + '}';
    }
    
}
