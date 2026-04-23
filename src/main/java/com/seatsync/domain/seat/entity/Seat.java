package com.seatsync.domain.seat.entity;

import com.seatsync.domain.schedule.entity.Schedule;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(nullable = false, length = 20)
    private String seatNumber;

    @Column(nullable = false, length = 20)
    private String grade;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false, length = 20)
    private String status;

    public void updateStatus(String status) {
        this.status = status;
    }
}