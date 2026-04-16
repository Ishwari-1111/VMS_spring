<<<<<<< HEAD
package vms.controller;
=======

    package vms.controller;
>>>>>>> 7f6aec662ce8fde9d9aba480673f9e725ce081a5
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vms.model.Event;
import vms.service.EventService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


    
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {
private final EventService eventService;
public EventController(EventService eventService) {
this.eventService = eventService;
}
@GetMapping
public List<Event> getAll() {
return eventService.getAllEvents();
}
@GetMapping("/{id}")
public Event getOne(@PathVariable String id) {
return eventService.getEvent(id);
}
@PostMapping
public Event create(@RequestBody @Valid CreateEventRequest req) {
return eventService.createEvent(req.eventId(), req.eventName(), req.date());
}
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable String id) {
eventService.deleteEvent(id);
return ResponseEntity.noContent().build();
}
@PostMapping("/{eventId}/volunteers/{volunteerId}")
public ResponseEntity<Void> enroll(@PathVariable String eventId,

@PathVariable String volunteerId) {
eventService.enrollVolunteer(eventId, volunteerId);
return ResponseEntity.ok().build();
}
@DeleteMapping("/{eventId}/volunteers/{volunteerId}")
public ResponseEntity<Void> unenroll(@PathVariable String eventId,
@PathVariable String volunteerId) {
eventService.unenrollVolunteer(eventId, volunteerId);
return ResponseEntity.ok().build();
}
@PostMapping("/{eventId}/volunteers/{volunteerId}/hours")
public ResponseEntity<Void> logHours(@PathVariable String eventId,
@PathVariable String volunteerId,
@RequestBody LogHoursRequest req) {
eventService.logHours(eventId, volunteerId, req.hours());
return ResponseEntity.ok().build();
}
@GetMapping("/{eventId}/stats")
public Map<String, Integer> getStats(@PathVariable String eventId) {
return eventService.getEventStats(eventId).entrySet().stream()
.collect(java.util.stream.Collectors.toMap(
e -> e.getKey().getName(),
Map.Entry::getValue
));
}
public record CreateEventRequest(String eventId, String eventName, LocalDate date) {}
public record LogHoursRequest(int hours) {}
}
<<<<<<< HEAD
    
=======
>>>>>>> 7f6aec662ce8fde9d9aba480673f9e725ce081a5

