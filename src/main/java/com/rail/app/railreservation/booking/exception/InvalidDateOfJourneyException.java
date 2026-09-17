package com.rail.app.railreservation.booking.exception;

public class InvalidDateOfJourneyException extends Exception{

    private final String doj;

    public InvalidDateOfJourneyException(String message, Throwable cause, String doj) {
        super(message, cause);
        this.doj = doj;
    }

    public InvalidDateOfJourneyException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return "InvalidDateOfJourneyException{" +
                "doj='" + doj + '\'' +
                '}';
    }

     public InvalidDateOfJourneyException(String doj, String message){
        super(message);
        this.doj = doj;
    }

    public String getDoj() {
        return doj;
    }

}
