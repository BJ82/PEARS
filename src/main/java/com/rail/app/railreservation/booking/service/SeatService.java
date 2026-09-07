package com.rail.app.railreservation.booking.service;

import com.rail.app.railreservation.booking.dto.BookingOpenRequest;
import com.rail.app.railreservation.booking.dto.BookingRequest;
import com.rail.app.railreservation.booking.entity.Booking;
import com.rail.app.railreservation.booking.entity.SeatCount;
import com.rail.app.railreservation.booking.entity.SeatNoTracker;
import com.rail.app.railreservation.route.entity.Route;
import com.rail.app.railreservation.trainmanagement.entity.Train;
import com.rail.app.railreservation.trainmanagement.enums.JourneyClass;
import com.rail.app.railreservation.util.Utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public interface SeatService {

    public Set<Integer> getAvailableSeatNumbers(BookingRequest request);

    public void trackLastSeatNo(BookingRequest request,int lastGivenSeatNo);
    public int getLastAllocatedSeatNo(BookingRequest request);

    public List<Integer> getSeatNumbers(String startFrom, String endAt, BookingRequest request);

    public void trackCountOfSeats(BookingRequest request,int noOfConfirmedSeats);
    public int getCountOfConfirmedSeats(BookingRequest request);

    public void initSeatNoTracker(int trainNo, BookingOpenRequest request);
    public void initSeatCount(int trainNo, BookingOpenRequest request);

}

