package com.rail.app.railreservation.booking.controller;

import com.rail.app.railreservation.booking.dto.*;
import com.rail.app.railreservation.booking.exception.BookingCannotOpenException;
import com.rail.app.railreservation.booking.exception.BookingNotOpenException;
import com.rail.app.railreservation.booking.exception.InvalidBookingException;
import com.rail.app.railreservation.booking.service.BookingService;
import com.rail.app.railreservation.enquiry.exception.PnrNoIncorrectException;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("api/v1")
public class BookingController {

    private static final Logger logger = LogManager.getLogger(BookingController.class);

    private static final String INSIDE_BOOKING_CONTROLLER = "Inside Booking Controller...";

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    //Ideally should be idempotent.
    //Use put or patch
    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> bookTicket(@RequestBody BookingRequest bookingRequest) throws InvalidBookingException, BookingNotOpenException, TimeTableNotFoundException {

        logger.info(INSIDE_BOOKING_CONTROLLER);
        logger.info("Processing Request For Ticket Booking");

        BookingResponse bookingResponse= bookingService.book(bookingRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(bookingResponse.getTrainNo())
                .toUri();

       return ResponseEntity.created(location).body(bookingResponse);
    }

    //Ideally should be idempotent.
    //Use put or patch
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("trains/{trainNo}/bookings/open")
    public ResponseEntity<BookingOpenResponse> openBooking(@PathVariable("trainNo") int trainNo,@RequestBody BookingOpenRequest bookingOpenRequest) throws BookingCannotOpenException {

        logger.info(INSIDE_BOOKING_CONTROLLER);
        logger.info("Processing Request To Open Booking");

        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(trainNo)
                .toUri();

        return ResponseEntity.created(location).body(bookingService.openBooking(trainNo,bookingOpenRequest));
    }

    @GetMapping("trains/{trainNo}/bookings/status")
    public ResponseEntity<BookingOpenInfo> isBookingOpen(@PathVariable("trainNo") int trainNo){

        return ResponseEntity.ok(bookingService.getBookingOpenInfo(trainNo));
    }

    @DeleteMapping("/bookings/{pnrNo}")
    public ResponseEntity<String> cancelTicket(@PathVariable("pnrNo") int pnrNo) throws PnrNoIncorrectException {

        return ResponseEntity.ok(bookingService.cancelBooking(pnrNo));

    }
}
