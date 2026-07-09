package com.stayhub.backend.Module.Booking.Event;

import com.stayhub.backend.Module.Booking.Model.Booking;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BookingCreatedEvent extends ApplicationEvent {
    private final Booking booking;

    public BookingCreatedEvent(Object source, Booking booking) {
        super(source);
        this.booking = booking;
    }
}
