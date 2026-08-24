package com.rail.app.railreservation.booking.service;

import com.rail.app.railreservation.route.service.RouteInfoService;
import com.rail.app.railreservation.trainmanagement.service.TrainArrivalDateService;
import com.rail.app.railreservation.trainmanagement.service.TrainService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;

public class BookingServiceForTest extends BookingService {

    public BookingServiceForTest(TrainService trainService, RouteInfoService routeInfoService,
                                 SeatInfoTrackerService seatInfoTrackerService,
                                 BookingInfoTrackerService bookingInfoTrackerService,
                                 BookingOpenInfoService bookingOpenInfoService,
                                 TrainArrivalDateService trainArrivalDateService, SeatNoService seatNoService,
                                 ModelMapper mapper) {

        super(trainService,routeInfoService,seatInfoTrackerService,bookingInfoTrackerService,
                bookingOpenInfoService,trainArrivalDateService,seatNoService,mapper);

    }
}
