package vms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vms.model.Volunteer;
import java.util.List;

@Repository
public interface VolunteerRepository
        extends JpaRepository<Volunteer, String> {

    // JpaRepository already gives you:
    // save(volunteer)        replaces addVolunteer()
    // deleteById(id)         replaces removeVolunteer()
    // findById(id)           replaces getVolunteerById()
    // findAll()              replaces getAllVolunteers()
    // existsById(id)         replaces containsVolunteer()
    // count()                replaces getVolunteerCount()
    
    // Find all volunteers enrolled in a specific event (through EventVolunteer join table)
    @Query("SELECT DISTINCT ev.volunteer FROM EventVolunteer ev WHERE ev.event.eventId = :eventId")
    List<Volunteer> findVolunteersByEventId(@Param("eventId") String eventId);
}
