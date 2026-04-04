package vms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vms.model.Volunteer;
import vms.repository.VolunteerRepository;

import java.util.List;
import java.util.Optional;

@Service
public class VolunteerService {
    @Autowired
    private VolunteerRepository volunteerRepository;

    public Volunteer addVolunteer(String id, String name) {
        if (volunteerRepository.existsById(id)) {
            throw new IllegalArgumentException(
                "Volunteer with id " + id + " already exists");
        }
        return volunteerRepository.save(new Volunteer(id, name));
    }

    public Optional<Volunteer> getVolunteer(String id) {
        return volunteerRepository.findById(id);
    }

    public List<Volunteer> getAllVolunteers() {
        return volunteerRepository.findAll();
    }

    public boolean removeVolunteer(String id) {
        if (!volunteerRepository.existsById(id)) return false;
        volunteerRepository.deleteById(id);
        return true;
    }

    public long getVolunteerCount() {
        return volunteerRepository.count();
    }
}
