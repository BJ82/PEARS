package com.rail.app.railreservation.booking.controller;

import com.rail.app.railreservation.booking.dto.*;
import com.rail.app.railreservation.booking.exception.BookingCannotOpenException;
import com.rail.app.railreservation.booking.exception.BookingNotOpenException;
import com.rail.app.railreservation.booking.exception.InvalidBookingException;
import com.rail.app.railreservation.booking.exception.TatkalNotOpenException;
import com.rail.app.railreservation.booking.exception.InvalidBookingAttemptException;
import com.rail.app.railreservation.booking.service.BookingService;
import com.rail.app.railreservation.booking.validator.ValidationSequence;
import com.rail.app.railreservation.enquiry.exception.PnrNoIncorrectException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@Validated
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
    @PostMapping("/booking")
    public ResponseEntity<BookingResponse> bookTicket(@Valid @RequestBody BookingRequest bookingRequest) throws InvalidBookingAttemptException {

        logger.info(INSIDE_BOOKING_CONTROLLER);
        logger.info("Processing Request For Ticket Booking");

        BookingResponse bookingResponse= bookingService.bookTicket(bookingRequest);

        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(bookingResponse.getTrainNo())
                .toUri();

       return ResponseEntity.created(location).body(bookingResponse);
    }

    //Ideally should be idempotent.
    //Use put or patch
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("trains/{trainNo}/booking/")
    public ResponseEntity<BookingOpenResponse> openBooking(@PathVariable("trainNo") @Positive int trainNo, @Validated(ValidationSequence.class) @RequestBody BookingOpenRequest bookingOpenRequest) throws BookingCannotOpenException {

        logger.info(INSIDE_BOOKING_CONTROLLER);
        logger.info("Processing Request To Open Booking");

        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(trainNo)
                .toUri();

        return ResponseEntity.created(location).body(bookingService.openBooking(trainNo,bookingOpenRequest));
    }

    @GetMapping("trains/{trainNo}/bookings/status")
    public ResponseEntity<BookingOpenInfo> isBookingOpen(@PathVariable("trainNo") @Positive int trainNo){

        return ResponseEntity.ok(bookingService.getBookingOpenInfo(trainNo));
    }

    @DeleteMapping("/bookings/{pnrNo}")
    public ResponseEntity<String> cancelTicket(@PathVariable("pnrNo") @Positive int pnrNo) throws PnrNoIncorrectException {

        return ResponseEntity.ok(bookingService.cancelBooking(pnrNo));

    }
}
