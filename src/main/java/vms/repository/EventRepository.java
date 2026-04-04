package vms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vms.model.Event;

public interface EventRepository extends JpaRepository<Event, String> {
    // findById(id), findAll(), save(), deleteById() all come for free
}