package com.seatsync.domain.reservation.repository;

import com.seatsync.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r JOIN FETCH r.seat JOIN FETCH r.user WHERE r.user.id = :userId AND r.status != 'CANCELLED'")
    List<Reservation> findByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.seat JOIN FETCH r.user WHERE r.id = :id")
    Optional<Reservation> findByIdWithSeatAndUser(@Param("id") Long id);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.seat WHERE r.status = 'PENDING' AND r.expiresAt < :now")
    List<Reservation> findExpiredPendingReservations(@Param("now") LocalDateTime now);

    @Query("SELECT r FROM Reservation r WHERE r.seat.id = :seatId AND r.user.id = :userId AND r.status = 'PENDING'")
    Optional<Reservation> findPendingReservation(@Param("seatId") Long seatId, @Param("userId") Long userId);
}