package com.seatsync.domain.reservation.entity;

import com.seatsync.domain.seat.entity.Seat;
import com.seatsync.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime reservedAt;

    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        this.reservedAt = LocalDateTime.now();
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public void clearExpiresAt() {
        this.expiresAt = null;
    }
}