package com.rail.app.railreservation.booking.dto;


import com.rail.app.railreservation.trainmanagement.enums.JourneyClass;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingRequest {

    @Positive(message="TrainNo Cannot Be Negative")
    private int trainNo;

    @NotBlank(message = "Please provide valid train name")
    private String trainName;

    @NotBlank(message = "Please provide valid train start date")
    private String startDt;

    @NotBlank(message = "Please provide valid train end date")
    private String endDt;

    @NotBlank(message = "Please provide valid source station")
    private String from;

    @NotBlank(message = "Please provide valid destination station")
    private String to;

    @NotNull(message = "Please provide valid journey class")
    private JourneyClass journeyClass;

    @NotBlank(message = "Please provide valid booking type")
    private String bookingType;

    @NotBlank(message = "Please provide valid journey date")
    private String doj;

    @Valid
    @NotEmpty(message = "Passenger list cannot be empty")
    private List<Passenger> passengers= new ArrayList<>();
}
