package com.seatsync.domain.reservation.service;

import com.seatsync.domain.reservation.entity.Reservation;
import com.seatsync.domain.reservation.repository.ReservationRepository;
import com.seatsync.domain.seat.entity.Seat;
import com.seatsync.domain.seat.repository.SeatRepository;
import com.seatsync.domain.user.entity.User;
import com.seatsync.domain.user.repository.UserRepository;
import com.seatsync.global.exception.CustomException;
import com.seatsync.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final long PENDING_TTL = 5;
    private static final String SEAT_PENDING_KEY = "seat:pending:";

    @Transactional
    public void holdSeat(Long seatId, String email) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        if (!seat.getStatus().equals("AVAILABLE")) {
            throw new CustomException(ErrorCode.SEAT_NOT_AVAILABLE);
        }

        String key = SEAT_PENDING_KEY + seatId;

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, email, PENDING_TTL, TimeUnit.MINUTES);

        if (Boolean.FALSE.equals(success)) {
            throw new CustomException(ErrorCode.SEAT_ALREADY_HELD);
        }

        seat.updateStatus("PENDING");

        // DB에 PENDING 상태로 예약 저장
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Reservation reservation = Reservation.builder()
                .user(user)
                .seat(seat)
                .status("PENDING")
                .expiresAt(LocalDateTime.now().plusMinutes(PENDING_TTL))
                .build();

        reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation confirmReservation(Long seatId, String email) {
        String key = SEAT_PENDING_KEY + seatId;
        String pendingEmail = redisTemplate.opsForValue().get(key);

        if (pendingEmail == null) {
            throw new CustomException(ErrorCode.RESERVATION_EXPIRED);
        }

        if (!pendingEmail.equals(email)) {
            throw new CustomException(ErrorCode.RESERVATION_UNAUTHORIZED);
        }

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        redisTemplate.delete(key);
        seat.updateStatus("RESERVED");

        // 기존 PENDING 예약 찾아서 CONFIRMED로 업데이트
        Reservation reservation = reservationRepository
                .findPendingReservation(seat.getId(), user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.updateStatus("CONFIRMED");
        reservation.clearExpiresAt();

        return reservation;
    }

    @Transactional
    public void cancelReservation(Long reservationId, String email) {
        Reservation reservation = reservationRepository.findByIdWithSeatAndUser(reservationId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getEmail().equals(email)) {
            throw new CustomException(ErrorCode.RESERVATION_UNAUTHORIZED);
        }

        reservation.getSeat().updateStatus("AVAILABLE");
        reservation.updateStatus("CANCELLED");
    }

    @Transactional(readOnly = true)
    public List<Reservation> getMyReservations(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return reservationRepository.findByUserId(user.getId());
    }

    public long getRemainingTime(Long seatId, String email) {
        String key = SEAT_PENDING_KEY + seatId;
        Long remainingSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        if (remainingSeconds == null || remainingSeconds < 0) {
            throw new CustomException(ErrorCode.RESERVATION_EXPIRED);
        }

        return remainingSeconds;
    }
}