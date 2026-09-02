package com.rail.app.railreservation.booking.service;

import com.rail.app.railreservation.booking.repository.BookingOpenRepository;
import com.rail.app.railreservation.booking.repository.BookingRepository;
import com.rail.app.railreservation.route.service.RouteService;
import com.rail.app.railreservation.trainmanagement.service.TrainArrivalDateService;
import com.rail.app.railreservation.trainmanagement.service.TrainService;
import org.modelmapper.ModelMapper;

public class BookingServiceForTest extends BookingService {

    public BookingServiceForTest(TrainService trainService, RouteService routeService,
                                 BookingService bookingService, BookingRepository bookingRepo,BookingOpenRepository bookingOpenRepo,
                                 TrainArrivalDateService trainArrivalDateService,
                                 SeatService seatService,
                                 ModelMapper mapper) {

        super(trainService, routeService,
                bookingService,bookingRepo,bookingOpenRepo,trainArrivalDateService, seatService,mapper);

    }
}
