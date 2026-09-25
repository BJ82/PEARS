package com.rail.app.railreservation.booking.service;

import com.rail.app.railreservation.booking.dto.*;
import com.rail.app.railreservation.booking.entity.Booking;
import com.rail.app.railreservation.booking.entity.BookingOpen;
import com.rail.app.railreservation.booking.exception.BookingCannotOpenException;
import com.rail.app.railreservation.booking.exception.BookingNotOpenException;
import com.rail.app.railreservation.booking.exception.InvalidBookingException;
import com.rail.app.railreservation.booking.exception.TatkalNotOpenException;
import com.rail.app.railreservation.booking.exception.InvalidBookingAttemptException;
import com.rail.app.railreservation.enquiry.exception.PnrNoIncorrectException;
import com.rail.app.railreservation.trainmanagement.enums.JourneyClass;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingService {

    public BookingResponse bookTicket(BookingRequest request)
            throws InvalidBookingAttemptException, TatkalNotOpenException, InvalidBookingException;

    public String cancelBooking(int pnrNo) throws PnrNoIncorrectException;

    public BookingOpenResponse openBooking(int trainNo, BookingOpenRequest request)
            throws BookingCannotOpenException;

    public BookingOpenInfo getBookingOpenInfo(int trainNo);

    public Optional<Boolean> isBookingOpen(BookingRequest request);

    public boolean isTatkalOpen(int trainNo);

    public List<BookingOpen> getBookingOpenInfoByTrainNo(int trainNo);

    public List<Booking> getBookingBySeatNumber(int seatNumber,Booking booking);

    public void trackBookingOpen(int trainNo, BookingOpenRequest request);

    public Optional<Booking> getBookingByPnrNo(int pnrNo);

    public Optional<List<Booking>> getWaitingList(int trainNo,
                                                  JourneyClass jrnyClass, LocalDate strtDt,
                                                  LocalDate endDt);
}

