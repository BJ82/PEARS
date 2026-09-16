package com.rail.app.railreservation.route.exception;

public class InvalidJourneyRouteException extends Exception{

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    private final String  from;
    private final String  to;

    public InvalidJourneyRouteException(String message,String from,String to) {
        super(message);
        this.from = from;
        this.to = to;
    }
}
