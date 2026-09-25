package com.rail.app.railreservation.booking.exception;

public class InvalidBookingAttemptException extends Exception{

    public InvalidBookingAttemptException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidBookingAttemptException(Throwable cause) {
        super(cause);
    }
}
