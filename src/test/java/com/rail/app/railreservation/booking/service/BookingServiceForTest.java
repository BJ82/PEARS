package com.rail.app.railreservation.booking.service;

import com.rail.app.railreservation.booking.repository.BookingOpenRepository;
import com.rail.app.railreservation.route.service.RouteInfoService;
import com.rail.app.railreservation.trainmanagement.service.TrainArrivalDateService;
import com.rail.app.railreservation.trainmanagement.service.TrainService;
import org.modelmapper.ModelMapper;

public class BookingServiceForTest extends BookingService {

    public BookingServiceForTest(TrainService trainService, RouteInfoService routeInfoService,
                                 BookingInfoTrackerService bookingInfoTrackerService,
                                 BookingService bookingService, BookingOpenRepository bookingOpenRepo,
                                 TrainArrivalDateService trainArrivalDateService,
                                 SeatService seatService,
                                 ModelMapper mapper) {

        super(trainService,routeInfoService,bookingInfoTrackerService,
                bookingService,bookingOpenRepo,trainArrivalDateService, seatService,mapper);

    }
}
