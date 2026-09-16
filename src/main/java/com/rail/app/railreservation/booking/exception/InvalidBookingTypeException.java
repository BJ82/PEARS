package com.rail.app.railreservation.booking.exception;

public class InvalidBookingTypeException extends Exception {

    public String getBookingType() {
        return bookingType;
    }

    private final String bookingType;

    public InvalidBookingTypeException(String message, String bookingType) {
        super(message);
        this.bookingType = bookingType;
    }
}
