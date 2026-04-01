package com.stayhub.backend.Module.Booking.Model;

import com.stayhub.backend.Module.Property.Model.Room;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "num_guests")
    @Builder.Default
    private Integer numGuests = 1;

    @Column(name = "price_at_booking", nullable = false, precision = 15, scale = 2)
    private BigDecimal priceAtBooking;
}
