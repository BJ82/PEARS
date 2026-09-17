package com.rail.app.railreservation.booking.exception;

public class UnableToVerifyDateOfJourneyException extends Exception {

    private String dateOfJourney;

    @Override
    public String toString() {
        return "UnableToVerifyDateOfJourneyException{" +
                "dateOfJourney='" + dateOfJourney + '\'' +
                '}';
    }

    public UnableToVerifyDateOfJourneyException(String message, Throwable cause, String dateOfJourney) {
        super(message, cause);
        this.dateOfJourney = dateOfJourney;
    }
}
