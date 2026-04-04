package vms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @Column(name = "event_id")
    @NotBlank(message = "eventId cannot be empty")
    private String eventId;

    @Column(name = "event_name", nullable = false)
    @NotBlank(message = "eventName cannot be empty")
    private String eventName;

    @NotNull
    private LocalDate date;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "volunteer_hours",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "volunteer_id")
    )
    @MapKeyJoinColumn(name = "volunteer_id")
    @ElementCollection
    @Column(name = "hours")
    private final Map<Volunteer, Integer> volunteers = new HashMap<>();

    // Required by JPA
    protected Event() {}

    public Event(String eventId, String eventName, LocalDate date) {
        if (eventId == null || eventId.trim().isEmpty() ||
            eventName == null || eventName.trim().isEmpty() || date == null) {
            throw new IllegalArgumentException("Invalid event parameters");
        }
        this.eventId = eventId;
        this.eventName = eventName.trim();
        this.date = date;
    }

    // ── All existing methods unchanged below ──

    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }

    public void setEventName(String eventName) {
        if (eventName == null || eventName.trim().isEmpty()) {
            throw new IllegalArgumentException("eventName cannot be empty");
        }
        this.eventName = eventName.trim();
    }

    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("date cannot be null");
        this.date = date;
    }

    public Map<Volunteer, Integer> getVolunteerHours() {
        return Collections.unmodifiableMap(volunteers);
    }

    public boolean addVolunteer(Volunteer volunteer) {
        if (volunteer == null) return false;
        return volunteers.putIfAbsent(volunteer, 0) == null;
    }

    public boolean removeVolunteer(Volunteer volunteer) {
        if (volunteer == null) return false;
        return volunteers.remove(volunteer) != null;
    }

    public void logHours(Volunteer volunteer, int hours) {
        if (volunteer == null || hours <= 0) {
            throw new IllegalArgumentException("Invalid volunteer or hours");
        }
        Integer current = volunteers.get(volunteer);
        if (current == null) throw new IllegalStateException("Volunteer not enrolled");
        volunteers.put(volunteer, current + hours);
    }

    public int getNumberOfVolunteers() { return volunteers.size(); }

    public int getTotalHours() {
        return volunteers.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getHoursForVolunteer(Volunteer volunteer) {
        return volunteers.getOrDefault(volunteer, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return eventId.equals(((Event) o).eventId);
    }

    @Override
    public int hashCode() { return eventId.hashCode(); }

    @Override
    public String toString() {
        return "Event{id='" + eventId + "', name='" + eventName +
               "', date=" + date + ", volunteers=" + volunteers.size() + '}';
    }
}