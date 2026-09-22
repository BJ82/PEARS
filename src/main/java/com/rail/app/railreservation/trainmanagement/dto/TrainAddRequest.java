package com.rail.app.railreservation.trainmanagement.dto;

import com.rail.app.railreservation.trainmanagement.enums.Day;
import com.rail.app.railreservation.trainmanagement.enums.JourneyClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainAddRequest {

    //private int trainNo;
    private List<String> stations;
    private List<Day> runOnDays;
    private LocalTime deptTime;
    private LocalTime arrvTime;
    private List<JourneyClass> avblJournyClass;
    private String trainName;
    private Set<Seat> seats;
}
