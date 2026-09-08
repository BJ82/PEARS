package com.rail.app.railreservation.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rail.app.railreservation.booking.dto.*;
import com.rail.app.railreservation.booking.enums.BookingStatus;
import com.rail.app.railreservation.booking.service.BookingService;
import com.rail.app.railreservation.security.config.SecurityConfig;
import com.rail.app.railreservation.security.service.UserDetailsServiceImpl;
import com.rail.app.railreservation.security.util.JwtUtil;
import com.rail.app.railreservation.trainmanagement.enums.JourneyClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(BookingController.class)
@Import(SecurityConfig.class)
class BookingControllerTest {

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private UserDetailsServiceImpl userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    private LocalDate startDate;
    private LocalDate endDate;
    private DateTimeFormatter pattern;

    private BookingRequest bookingRequest;

    private String jwt;

    private int trainNo;

    @BeforeEach
    void setUp() {

        pattern = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        startDate = LocalDate.now();
        endDate = startDate.plusDays(2);

        List<Passenger> passengers= new ArrayList<>();
        passengers.add(new Passenger("First Passenger",24,"M"));
        passengers.add(new Passenger("Second Passenger",25,"F"));
        passengers.add(new Passenger("Third Passenger",26,"F"));
        jwt = "JwtForTest";
        trainNo = 1;
        String from = "stn1";
        String to = "stn5";
        String doj = startDate.plusDays(1).format(pattern);

        bookingRequest = new BookingRequest(trainNo,"TRAIN1",startDate.format(pattern),
                endDate.format(pattern),from,to, JourneyClass.AC1,doj,passengers);

    }

    @Test
    @WithMockUser(username = "bj", roles = {"USER"})
    void testBookTicket() throws Exception {

        ModelMapper mapper = new ModelMapper();

        BookingResponse bookingResponse = mapper.map(bookingRequest,BookingResponse.class);
        bookingResponse.setBookingDateTime(Timestamp.from(Instant.now()));

        BookedPassenger bookedPassenger;

        int noOfPsngr = bookingRequest.getPassengers().size();

        for(int j=0;j<noOfPsngr;j++) {

            bookedPassenger = mapper.map(bookingRequest.getPassengers().get(j), BookedPassenger.class);

            bookedPassenger.setPnr((int)Math.random());
            bookedPassenger.setSeatNo((int)Math.random());
            bookedPassenger.setStatus(BookingStatus.CONFIRMED);

            bookingResponse.getPassengerList().add(bookedPassenger);

        }

        when(bookingService.book(bookingRequest)).thenReturn(bookingResponse);

        mockMvc.perform(post("/api/v1/bookings")
                        .header(HttpHeaders.AUTHORIZATION,"Bearer "+jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(bookingRequest))
                .with(csrf()))
                .andExpect(status().isCreated());

    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testOpenBooking() throws Exception {

        BookingOpenRequest bookingOpenRequest = new BookingOpenRequest(startDate.format(pattern),endDate.format(pattern));
        BookingOpenResponse bookingOpenResponse = new BookingOpenResponse(trainNo,startDate.format(pattern),
                endDate.format(pattern),true);
        when(bookingService.openBooking(trainNo,bookingOpenRequest)).thenReturn(bookingOpenResponse);

        mockMvc.perform(post("/api/v1/trains/{trainNo}/bookings/open",trainNo)
                        .header(HttpHeaders.AUTHORIZATION,"Bearer "+jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(bookingOpenRequest))
                        .with(csrf()))
                .andExpect(status().isCreated());

    }

    @Test
    @WithMockUser(username = "bj", roles = {"USER"})
    void testIsBookingOpen() throws Exception {

        List<BookingOpenDate> bookingOpenDates =
                List.of(new BookingOpenDate(startDate,endDate),
                        new BookingOpenDate(startDate.plusDays(2),
                                             endDate.plusDays(4)));

        BookingOpenInfo bookingOpenInfo = new BookingOpenInfo(trainNo,bookingOpenDates);

        when(bookingService.getBookingOpenInfo(trainNo)).thenReturn(bookingOpenInfo);

        mockMvc.perform(get("/api/v1/trains/{trainNo}/bookings/status",trainNo)
                        .header(HttpHeaders.AUTHORIZATION,"Bearer "+jwt))
                        .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "bj", roles = {"USER"})
    void testCancelTicket() throws Exception {
        int pnrNo = 1;
        when(bookingService.cancelBooking(pnrNo)).thenReturn("Deleted Booking For PnrNo:"+pnrNo);

        mockMvc.perform(delete("/api/v1/bookings/{pnrNo}",pnrNo)
                        .header(HttpHeaders.AUTHORIZATION,"Bearer "+jwt)
                        .with(csrf()))
                .andExpect(status().isOk());


    }
}