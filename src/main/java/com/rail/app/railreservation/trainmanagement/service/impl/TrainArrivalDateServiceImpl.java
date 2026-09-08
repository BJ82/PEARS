package com.rail.app.railreservation.trainmanagement.service.impl;

import com.rail.app.railreservation.trainmanagement.dto.TimeTableEnquiryResponse;
import com.rail.app.railreservation.trainmanagement.entity.Timing;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableNotFoundException;
import com.rail.app.railreservation.trainmanagement.service.TimeTableService;
import com.rail.app.railreservation.trainmanagement.service.TrainArrivalDateService;
import com.rail.app.railreservation.util.Utils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class TrainArrivalDateServiceImpl implements TrainArrivalDateService {

    private final TimeTableService timeTableService;

    public TrainArrivalDateServiceImpl(TimeTableService timeTableService) {
        this.timeTableService = timeTableService;
    }


    public LocalDate getArrivalDate(int trainNo, String stn, LocalDate startDate) throws TimeTableNotFoundException {

        TimeTableEnquiryResponse timeTableEnquiryResponse;
        timeTableEnquiryResponse = timeTableService.getTimeTable(trainNo);

        List<Timing> trainTimings;
        trainTimings = timeTableEnquiryResponse.getTrainTimings();

        long minutes = 0;

        LocalTime prevDeptTime = Utils.toLocalTime(trainTimings.get(0).getDeptTime());

        LocalTime arrivalTime, departureTime;

        Duration between1,between2;

        LocalDate arrivalDate = startDate;

        for(Timing timing:trainTimings){

            arrivalTime = Utils.toLocalTime(timing.getArrvTime());
            between1 = Duration.between(prevDeptTime,arrivalTime);

            minutes = between1.toMinutes();

            if( minutes < 0){

                arrivalDate = arrivalDate.plusDays(1);
            }


            if(timing.getStation().equals(stn))
                break;


            departureTime = Utils.toLocalTime(timing.getDeptTime());
            between2 = Duration.between(arrivalTime,departureTime);

            minutes = between2.toMinutes();

            if( minutes < 0){

                arrivalDate = arrivalDate.plusDays(1);
            }

            prevDeptTime = departureTime;
        }

        return arrivalDate;
    }

}
