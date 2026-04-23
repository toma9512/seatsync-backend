package com.seatsync.domain.reservation.service;

import com.seatsync.domain.reservation.entity.Reservation;
import com.seatsync.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;

    @Scheduled(fixedDelay = 30000) // 30초마다 실행
    @Transactional
    public void cancelExpiredReservations() {
        List<Reservation> expired =
                reservationRepository.findExpiredPendingReservations(LocalDateTime.now());

        for (Reservation reservation : expired) {
            reservation.getSeat().updateStatus("AVAILABLE");
            reservation.updateStatus("CANCELLED");
            log.info("만료된 예약 취소: reservationId={}", reservation.getId());
        }
    }
}