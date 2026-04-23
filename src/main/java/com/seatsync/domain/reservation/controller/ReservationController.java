package com.seatsync.domain.reservation.controller;

import com.seatsync.domain.reservation.entity.Reservation;
import com.seatsync.domain.reservation.service.ReservationService;
import com.seatsync.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 좌석 선점
    @PostMapping("/hold/{seatId}")
    public ApiResponse<Void> holdSeat(@PathVariable Long seatId,
                                      @AuthenticationPrincipal String email) {
        reservationService.holdSeat(seatId, email);
        return ApiResponse.success("좌석이 선점되었습니다. 5분 안에 예약을 확정해주세요.", null);
    }

    // 예약 확정
    @PostMapping("/confirm/{seatId}")
    public ApiResponse<ReservationResponse> confirmReservation(@PathVariable Long seatId,
                                                               @AuthenticationPrincipal String email) {
        Reservation reservation = reservationService.confirmReservation(seatId, email);
        return ApiResponse.success(ReservationResponse.from(reservation));
    }

    // 예약 취소
    @PostMapping("/cancel/{reservationId}")
    public ApiResponse<Void> cancelReservation(@PathVariable Long reservationId,
                                               @AuthenticationPrincipal String email) {
        reservationService.cancelReservation(reservationId, email);
        return ApiResponse.success("예약이 취소되었습니다.", null);
    }

    // 내 예약 목록
    @GetMapping("/my")
    public ApiResponse<List<ReservationResponse>> getMyReservations(
            @AuthenticationPrincipal String email) {
        List<Reservation> reservations = reservationService.getMyReservations(email);
        return ApiResponse.success(reservations.stream().map(ReservationResponse::from).toList());
    }

    public record ReservationResponse(
            Long id,
            Long seatId,
            String seatNumber,
            String grade,
            int price,
            String status,
            LocalDateTime reservedAt
    ) {
        public static ReservationResponse from(Reservation reservation) {
            return new ReservationResponse(
                    reservation.getId(),
                    reservation.getSeat().getId(),
                    reservation.getSeat().getSeatNumber(),
                    reservation.getSeat().getGrade(),
                    reservation.getSeat().getPrice(),
                    reservation.getStatus(),
                    reservation.getReservedAt()
            );
        }
    }
}