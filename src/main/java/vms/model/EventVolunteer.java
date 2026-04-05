package vms.model;

import jakarta.persistence.*;

@Entity
@Table(name = "event_volunteers")
public class EventVolunteer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "volunteer_id", nullable = false)
    private Volunteer volunteer;

    @Column(name = "hours")
    private int hours = 0;

    protected EventVolunteer() {}

    public EventVolunteer(Event event, Volunteer volunteer) {
        this.event = event;
        this.volunteer = volunteer;
        this.hours = 0;
    }

    public Event getEvent() { return event; }
    public Volunteer getVolunteer() { return volunteer; }
    public int getHours() { return hours; }
    public void addHours(int h) { this.hours += h; }
}