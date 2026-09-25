package com.rail.app.railreservation.booking.service;

import com.rail.app.railreservation.booking.dto.BookingOpenRequest;
import com.rail.app.railreservation.booking.dto.BookingRequest;

import java.util.List;
import java.util.Set;

public interface SeatService {

    public Set<Integer> getAvailableSeatNumbers(BookingRequest request);

    public void trackLastSeatNo(BookingRequest request,int lastGivenSeatNo);
    public int getLastAllocatedSeatNo(BookingRequest request);

    public List<Integer> getConfirmedSeatNumbers(String startFrom, String endAt, BookingRequest request);

    public void trackCountOfSeats(BookingRequest request,int noOfConfirmedSeats);
    public int getCountOfConfirmedSeats(BookingRequest request);

    public void initSeatNoTracker(int trainNo, BookingOpenRequest request);
    public void initSeatCount(int trainNo, BookingOpenRequest request);

}

