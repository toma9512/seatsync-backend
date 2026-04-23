package com.seatsync.domain.schedule.repository;

import com.seatsync.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    @Query("SELECT s FROM Schedule s JOIN FETCH s.event WHERE s.event.id = :eventId")
    List<Schedule> findByEventId(@Param("eventId") Long eventId);
}