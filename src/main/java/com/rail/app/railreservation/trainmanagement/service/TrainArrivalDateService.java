package com.rail.app.railreservation.trainmanagement.service;

import com.rail.app.railreservation.trainmanagement.dto.TimeTableEnquiryResponse;
import com.rail.app.railreservation.trainmanagement.entity.Timing;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableNotFoundException;
import com.rail.app.railreservation.util.Utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TrainArrivalDateService {

    public LocalDate getArrivalDate(int trainNo, String stn, LocalDate startDate) throws TimeTableNotFoundException;

}
