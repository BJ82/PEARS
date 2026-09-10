package com.rail.app.railreservation.booking.exception;

import java.io.IOException;

public class TatkalNotOpenException extends IOException {
    public TatkalNotOpenException(String message) {
        super(message);
    }
}
