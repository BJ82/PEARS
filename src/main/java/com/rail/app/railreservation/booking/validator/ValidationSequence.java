package com.rail.app.railreservation.booking.validator;

import jakarta.validation.GroupSequence;

@GroupSequence({FirstStep.class, SecondStep.class})
public interface ValidationSequence {

}
