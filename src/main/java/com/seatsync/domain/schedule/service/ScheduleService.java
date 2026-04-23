package com.seatsync.domain.schedule.service;

import com.seatsync.domain.event.entity.Event;
import com.seatsync.domain.event.repository.EventRepository;
import com.seatsync.domain.schedule.entity.Schedule;
import com.seatsync.domain.schedule.repository.ScheduleRepository;
import com.seatsync.global.exception.CustomException;
import com.seatsync.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final EventRepository eventRepository;

    @Transactional
    public Schedule createSchedule(Long eventId, LocalDateTime startTime, int totalSeats) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        Schedule schedule = Schedule.builder()
                .event(event)
                .startTime(startTime)
                .totalSeats(totalSeats)
                .remainingSeats(totalSeats)
                .build();

        return scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByEvent(Long eventId) {
        return scheduleRepository.findByEventId(eventId);
    }

    @Transactional(readOnly = true)
    public Schedule getSchedule(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
    }
}