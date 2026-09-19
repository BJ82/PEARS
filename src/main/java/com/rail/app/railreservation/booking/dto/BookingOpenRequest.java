package com.rail.app.railreservation.booking.dto;

import com.rail.app.railreservation.util.Utils;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingOpenRequest {

    @NotBlank(message = "Please provide valid start date")
    @Future(message = "Booking open start date should be greater than present date")
    private String startDt;

    @NotBlank(message = "Please provide valid end date")
    private String endDt;

    @AssertTrue(message = "Booking open start date cannot be after end date")
    public boolean isStartDateBeforeEndDate(){

        LocalDate startDate = Utils.toLocalDate(startDt);
        LocalDate endDate = Utils.toLocalDate(endDt);

        return startDate.isBefore(endDate);

    }


}
