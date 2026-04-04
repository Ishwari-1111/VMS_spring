package vms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vms.model.Volunteer;

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
}
