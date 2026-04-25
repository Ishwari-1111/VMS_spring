package vms.service;

import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vms.model.Event;
import vms.model.Volunteer;
import vms.repository.EventRepository;
import vms.repository.VolunteerRepository;
import vms.repository.CertificateRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepo;
    private final VolunteerRepository volunteerRepo;
    private final CertificateService certificateService;
    private final CertificateRepository certificateRepository;

    //@Autowired
    public EventService(EventRepository eventRepo, VolunteerRepository volunteerRepo, 
                       CertificateService certificateService, CertificateRepository certificateRepository) {
        this.eventRepo = eventRepo;
        this.volunteerRepo = volunteerRepo;
        this.certificateService = certificateService;
        this.certificateRepository = certificateRepository;
    }

    public Event createEvent(String eventId, String eventName, LocalDate date, LocalDate finishDate) {
        String resolvedEventId = (eventId == null || eventId.trim().isEmpty())
            ? generateNextEventId()
            : eventId.trim();

        if (eventRepo.existsById(resolvedEventId)) {
            throw new IllegalArgumentException("Event already exists: " + resolvedEventId);
        }

        Event event = new Event(resolvedEventId, eventName, date);
        event.setFinishDate(finishDate);
        return eventRepo.save(event);
    }

    private String generateNextEventId() {
        List<Event> events = eventRepo.findAll();
        Pattern pattern = Pattern.compile("^([A-Za-z]+)?(\\d+)$");
        String prefix = "EV";
        int maxSequence = 0;

        for (Event event : events) {
            Matcher matcher = pattern.matcher(event.getEventId());
            if (!matcher.matches()) {
                continue;
            }

            String currentPrefix = matcher.group(1) != null ? matcher.group(1) : "";
            int sequence = Integer.parseInt(matcher.group(2));

            if (sequence > maxSequence) {
                maxSequence = sequence;
                prefix = currentPrefix.isEmpty() ? prefix : currentPrefix;
            }
        }

        return prefix + String.format("%03d", maxSequence + 1);
    }

    public void deleteEvent(String eventId) {
        if (!eventRepo.existsById(eventId)) {
            throw new NoSuchElementException("Event not found: " + eventId);
        }
        eventRepo.deleteById(eventId);
    }

    public List<Event> getAllEvents() {
        publishCertificatesForPastEvents();
        return eventRepo.findAll();
    }

    public Event getEvent(String eventId) {
        return eventRepo.findById(eventId)
            .orElseThrow(() -> new NoSuchElementException("Event not found: " + eventId));
    }

    /**
     * Automatically complete past-due events and generate certificates.
     * This runs when events are fetched and also on a fixed schedule.
     */
    public void publishCertificatesForPastEvents() {
        LocalDate today = LocalDate.now();
        List<Event> pastDueEvents = eventRepo.findByIsCompletedFalseAndDateBefore(today);

        for (Event event : pastDueEvents) {
            event.markAsCompleted(event.getDate());
            eventRepo.save(event);

            if (certificateService != null) {
                certificateService.generateCertificatesForEvent(event.getEventId());
            }
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void runScheduledCertificatePublication() {
        publishCertificatesForPastEvents();
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

    /**
     * Mark event as completed and automatically generate certificates for all volunteers
     */
    public Event completeEvent(String eventId, LocalDate finishDate) {
        Event event = getEvent(eventId);
        
        // Mark event as completed
        event.markAsCompleted(finishDate);
        Event savedEvent = eventRepo.save(event);
        
        // Auto-generate certificates for all volunteers (if CertificateService is available)
        if (certificateService != null) {
            certificateService.generateCertificatesForEvent(eventId);
            System.out.println("✅ EVENT COMPLETED: Certificates auto-generated for event " + eventId);
        }
        
        return savedEvent;
    }

    /**
     * Mark event as incomplete
     */
    public Event markIncomplete(String eventId) {
        Event event = getEvent(eventId);
        event.markAsIncomplete();
        return eventRepo.save(event);
    }
}
