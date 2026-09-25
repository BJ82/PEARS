package com.rail.app.railreservation.booking.exception;

import java.io.IOException;

public class BookingNotOpenException extends IOException {
    @Override
    public String toString() {
        return "BookingNotOpenException{}";
    }

    public BookingNotOpenException(String message) {
        super(message);
    }
}
