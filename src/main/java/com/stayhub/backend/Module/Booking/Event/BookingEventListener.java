package com.stayhub.backend.Module.Booking.Event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BookingEventListener {

    @EventListener
    public void handleBookingCreatedEvent(BookingCreatedEvent event) {
        log.info("Received BookingCreatedEvent for booking code: {}. Observer Pattern implemented successfully.", 
                 event.getBooking().getBookingCode());
    }
}
