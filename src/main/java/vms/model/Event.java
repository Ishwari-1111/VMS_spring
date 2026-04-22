package vms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.*;

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

    @Column(name = "finish_date")
    private LocalDate finishDate;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventVolunteer> eventVolunteers = new ArrayList<>();

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

    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }

    public void setEventName(String eventName) {
        if (eventName == null || eventName.trim().isEmpty())
            throw new IllegalArgumentException("eventName cannot be empty");
        this.eventName = eventName.trim();
    }

    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("date cannot be null");
        this.date = date;
    }

    public LocalDate getFinishDate() { return finishDate; }

    public void setFinishDate(LocalDate finishDate) {
        this.finishDate = finishDate;
    }

    public Boolean isCompleted() { return isCompleted; }

    public void markAsCompleted(LocalDate finishDate) {
        if (finishDate == null) throw new IllegalArgumentException("finishDate cannot be null");
        this.finishDate = finishDate;
        this.isCompleted = true;
    }

    public void markAsIncomplete() {
        this.isCompleted = false;
        this.finishDate = null;
    }

    public boolean addVolunteer(Volunteer volunteer) {
        if (volunteer == null) return false;
        boolean exists = eventVolunteers.stream()
            .anyMatch(ev -> ev.getVolunteer().equals(volunteer));
        if (exists) return false;
        eventVolunteers.add(new EventVolunteer(this, volunteer));
        return true;
    }

    public boolean removeVolunteer(Volunteer volunteer) {
        if (volunteer == null) return false;
        return eventVolunteers.removeIf(
            ev -> ev.getVolunteer().equals(volunteer));
    }

    public void logHours(Volunteer volunteer, int hours) {
        if (volunteer == null || hours <= 0)
            throw new IllegalArgumentException("Invalid volunteer or hours");
        EventVolunteer ev = eventVolunteers.stream()
            .filter(e -> e.getVolunteer().equals(volunteer))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Volunteer not enrolled"));
        ev.addHours(hours);
    }

    public Map<Volunteer, Integer> getVolunteerHours() {
        Map<Volunteer, Integer> map = new HashMap<>();
        for (EventVolunteer ev : eventVolunteers)
            map.put(ev.getVolunteer(), ev.getHours());
        return Collections.unmodifiableMap(map);
    }

    public int getNumberOfVolunteers() { return eventVolunteers.size(); }

    public int getTotalHours() {
        return eventVolunteers.stream().mapToInt(EventVolunteer::getHours).sum();
    }

    public int getHoursForVolunteer(Volunteer volunteer) {
        return eventVolunteers.stream()
            .filter(ev -> ev.getVolunteer().equals(volunteer))
            .mapToInt(EventVolunteer::getHours)
            .findFirst()
            .orElse(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return eventId.equals(((Event) o).eventId);
    }

    @Override public int hashCode() { return eventId.hashCode(); }

    @Override
    public String toString() {
        return "Event{id='" + eventId + "', name='" + eventName +
               "', date=" + date + ", finishDate=" + finishDate + 
               ", isCompleted=" + isCompleted + ", volunteers=" + eventVolunteers.size() + '}';
    }
}