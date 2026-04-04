package vms.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import vms.model.Event;
import vms.model.Volunteer;
import vms.repository.EventRepository;
import vms.repository.VolunteerRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepo;
    private final VolunteerRepository volunteerRepo;

    public EventService(EventRepository eventRepo, VolunteerRepository volunteerRepo) {
        this.eventRepo = eventRepo;
        this.volunteerRepo = volunteerRepo;
    }

    public Event createEvent(String eventId, String eventName, LocalDate date) {
        if (eventRepo.existsById(eventId)) {
            throw new IllegalArgumentException("Event already exists: " + eventId);
        }
        return eventRepo.save(new Event(eventId, eventName, date));
    }

    public void deleteEvent(String eventId) {
        if (!eventRepo.existsById(eventId)) {
            throw new NoSuchElementException("Event not found: " + eventId);
        }
        eventRepo.deleteById(eventId);
    }

    public List<Event> getAllEvents() {
        return eventRepo.findAll();
    }

    public Event getEvent(String eventId) {
        return eventRepo.findById(eventId)
            .orElseThrow(() -> new NoSuchElementException("Event not found: " + eventId));
    }

    public boolean enrollVolunteer(String eventId, String volunteerId) {
        Event event = getEvent(eventId);
        Volunteer volunteer = volunteerRepo.findById(volunteerId)
            .orElseThrow(() -> new NoSuchElementException("Volunteer not found: " + volunteerId));
        boolean added = event.addVolunteer(volunteer);
        eventRepo.save(event);
        return added;
    }

    public boolean unenrollVolunteer(String eventId, String volunteerId) {
        Event event = getEvent(eventId);
        Volunteer volunteer = volunteerRepo.findById(volunteerId)
            .orElseThrow(() -> new NoSuchElementException("Volunteer not found: " + volunteerId));
        boolean removed = event.removeVolunteer(volunteer);
        eventRepo.save(event);
        return removed;
    }

    public void logHours(String eventId, String volunteerId, int hours) {
        Event event = getEvent(eventId);
        Volunteer volunteer = volunteerRepo.findById(volunteerId)
            .orElseThrow(() -> new NoSuchElementException("Volunteer not found: " + volunteerId));
        event.logHours(volunteer, hours);
        eventRepo.save(event);
    }

    public Map<Volunteer, Integer> getEventStats(String eventId) {
        return getEvent(eventId).getVolunteerHours();
    }
}