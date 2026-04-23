package com.seatsync.domain.seat.controller;

import com.seatsync.domain.seat.entity.Seat;
import com.seatsync.domain.seat.service.SeatService;
import com.seatsync.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping
    public ApiResponse<List<SeatResponse>> createSeats(@RequestBody SeatRequest request) {
        List<Seat> seats = seatService.createSeats(
                request.scheduleId(), request.grade(), request.price(), request.count()
        );
        return ApiResponse.success(seats.stream().map(SeatResponse::from).toList());
    }

    @GetMapping("/schedules/{scheduleId}")
    public ApiResponse<List<SeatResponse>> getSeatsBySchedule(@PathVariable Long scheduleId) {
        List<Seat> seats = seatService.getSeatsBySchedule(scheduleId);
        return ApiResponse.success(seats.stream().map(SeatResponse::from).toList());
    }

    public record SeatRequest(
            Long scheduleId,
            String grade,
            int price,
            int count
    ) {}

    public record SeatResponse(
            Long id,
            String seatNumber,
            String grade,
            int price,
            String status
    ) {
        public static SeatResponse from(Seat seat) {
            return new SeatResponse(
                    seat.getId(),
                    seat.getSeatNumber(),
                    seat.getGrade(),
                    seat.getPrice(),
                    seat.getStatus()
            );
        }
    }
}