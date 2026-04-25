package vms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vms.model.Event;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, String> {
    // findById(id), findAll(), save(), deleteById() all come for free

    List<Event> findByIsCompletedFalseAndDateBefore(LocalDate date);
}
