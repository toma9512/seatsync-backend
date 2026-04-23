package com.seatsync.domain.schedule.controller;

import com.seatsync.domain.schedule.entity.Schedule;
import com.seatsync.domain.schedule.service.ScheduleService;
import com.seatsync.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public ApiResponse<ScheduleResponse> createSchedule(@RequestBody ScheduleRequest request) {
        Schedule schedule = scheduleService.createSchedule(
                request.eventId(), request.startTime(), request.totalSeats()
        );
        return ApiResponse.success(ScheduleResponse.from(schedule));
    }

    @GetMapping("/events/{eventId}")
    public ApiResponse<List<ScheduleResponse>> getSchedulesByEvent(@PathVariable Long eventId) {
        List<Schedule> schedules = scheduleService.getSchedulesByEvent(eventId);
        return ApiResponse.success(schedules.stream().map(ScheduleResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<ScheduleResponse> getSchedule(@PathVariable Long id) {
        return ApiResponse.success(ScheduleResponse.from(scheduleService.getSchedule(id)));
    }

    public record ScheduleRequest(
            Long eventId,
            LocalDateTime startTime,
            int totalSeats
    ) {}

    public record ScheduleResponse(
            Long id,
            Long eventId,
            String eventTitle,
            LocalDateTime startTime,
            int totalSeats,
            int remainingSeats
    ) {
        public static ScheduleResponse from(Schedule schedule) {
            return new ScheduleResponse(
                    schedule.getId(),
                    schedule.getEvent().getId(),
                    schedule.getEvent().getTitle(),
                    schedule.getStartTime(),
                    schedule.getTotalSeats(),
                    schedule.getRemainingSeats()
            );
        }
    }
}