package com.rail.app.railreservation.booking.exception;

public class InvalidDateOfJourneyException extends Exception{

    private final String doj;

    InvalidDateOfJourneyException(String doj,String message){
        super(message);
        this.doj = doj;
    }

    public String getDoj() {
        return doj;
    }

}
