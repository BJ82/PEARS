package com.rail.app.railreservation.booking.dto;


import com.rail.app.railreservation.trainmanagement.enums.JourneyClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {

    private int trainNo;
    private String trainName;
    private String startDt;
    private String endDt;
    private String from;
    private String to;
    private JourneyClass journeyClass;
    private String bookingType;
    private String doj;
    private List<Passenger> passengers= new ArrayList<>();
}
