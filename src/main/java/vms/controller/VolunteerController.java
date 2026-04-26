package vms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vms.model.Volunteer;
import vms.service.VolunteerService;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/volunteers")
@CrossOrigin(origins = "*")
public class VolunteerController {
    
    private final VolunteerService volunteerService;
    
    public VolunteerController(VolunteerService volunteerService) {
        this.volunteerService = volunteerService;
    }
    
    // Get all volunteers
    @GetMapping
    public List<Volunteer> getAll() {
        return volunteerService.getAllVolunteers();
    }
    
    // Get volunteer by ID
    @GetMapping("/{id}")
    public ResponseEntity<Volunteer> getOne(@PathVariable String id) {
        Optional<Volunteer> volunteer = volunteerService.getVolunteer(id);
        return volunteer.isPresent() ? ResponseEntity.ok(volunteer.get()) 
                                     : ResponseEntity.notFound().build();
    }

    // Add volunteer using request params (kept for compatibility with existing tests/clients)
    @PostMapping
    public ResponseEntity<?> addVolunteer(@RequestParam("id") String id,
                                          @RequestParam("name") String name) {
        try {
            Volunteer createdVolunteer = volunteerService.addVolunteer(id, name);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVolunteer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new java.util.HashMap<String, String>() {{
                    put("error", e.getMessage());
                }}
            );
        }
    }
    
    // Sign up endpoint (with JSON body)
    @PostMapping(value = "/signup", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> signup(@RequestBody Volunteer volunteer) {
        try {
            Volunteer createdVolunteer = volunteerService.addVolunteer(
                volunteer.getVolunteerId(), 
                volunteer.getName(),
                volunteer.getEmail()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVolunteer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new java.util.HashMap<String, String>() {{
                    put("error", e.getMessage());
                }}
            );
        }
    }
    
    // Remove volunteer
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeVolunteer(@PathVariable String id) {
        if (volunteerService.removeVolunteer(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // Get volunteer count
    @GetMapping("/count")
    public ResponseEntity<Long> getVolunteerCount() {
        return ResponseEntity.ok(volunteerService.getVolunteerCount());
    }
}
