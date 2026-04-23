package com.seatsync.domain.seat.service;

import com.seatsync.domain.schedule.entity.Schedule;
import com.seatsync.domain.schedule.repository.ScheduleRepository;
import com.seatsync.domain.seat.entity.Seat;
import com.seatsync.domain.seat.repository.SeatRepository;
import com.seatsync.global.exception.CustomException;
import com.seatsync.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final ScheduleRepository scheduleRepository;

    @Transactional
    public List<Seat> createSeats(Long scheduleId, String grade, int price, int count) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Seat seat = Seat.builder()
                    .schedule(schedule)
                    .seatNumber(grade + "-" + i)
                    .grade(grade)
                    .price(price)
                    .status("AVAILABLE")
                    .build();
            seats.add(seat);
        }

        return seatRepository.saveAll(seats);
    }

    @Transactional(readOnly = true)
    public List<Seat> getSeatsBySchedule(Long scheduleId) {
        return seatRepository.findByScheduleId(scheduleId);
    }

    @Transactional(readOnly = true)
    public Seat getSeat(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));
    }
}