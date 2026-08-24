package com.rail.app.railreservation.booking.service;

import com.rail.app.railreservation.booking.dto.*;
import com.rail.app.railreservation.booking.entity.Booking;
import com.rail.app.railreservation.booking.enums.BookingStatus;
import com.rail.app.railreservation.booking.exception.BookingCannotOpenException;
import com.rail.app.railreservation.booking.exception.BookingNotOpenException;
import com.rail.app.railreservation.booking.exception.InvalidBookingException;
import com.rail.app.railreservation.enquiry.exception.PnrNoIncorrectException;
import com.rail.app.railreservation.route.entity.Route;
import com.rail.app.railreservation.route.service.RouteInfoService;
import com.rail.app.railreservation.trainmanagement.entity.Train;
import com.rail.app.railreservation.trainmanagement.enums.JourneyClass;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableNotFoundException;
import com.rail.app.railreservation.trainmanagement.service.TrainArrivalDateService;
import com.rail.app.railreservation.trainmanagement.service.TrainService;
import com.rail.app.railreservation.util.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalMatchers.not;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private BookingServiceForTest bookingServiceUnderTest;

    @Mock private TrainService trainService;

    @Mock private RouteInfoService routeInfoService;

    @Mock private SeatInfoTrackerService seatInfoTrackerService;

    @Mock private BookingInfoTrackerService bookingInfoTrackerService;

    @Mock private BookingOpenInfoService bookingOpenInfoService;

    @Mock private TrainArrivalDateService trainArrivalDateService;

    @Mock private SeatNoService seatNoService;
    private ModelMapper mapper;

    private LocalDate startDate;
    private LocalDate endDate;
    private DateTimeFormatter pattern;

    private BookingRequest bookingRequest;

    private Route route;
    private Train train;

    @BeforeEach
    void setUp() {

        pattern = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        startDate = LocalDate.now();
        endDate = startDate.plusDays(2);

        mapper = new ModelMapper();

        bookingServiceUnderTest = new BookingServiceForTest(trainService,routeInfoService,
                seatInfoTrackerService,bookingInfoTrackerService,bookingOpenInfoService,trainArrivalDateService,
                seatNoService,mapper);



        List<Passenger> passengers= new ArrayList<>();
        passengers.add(new Passenger("First Passenger",24,"M"));
        passengers.add(new Passenger("Second Passenger",25,"F"));
        passengers.add(new Passenger("Third Passenger",26,"F"));


        String from = "stn1";
        String to = "stn5";
        String doj = startDate.plusDays(1).format(pattern);

        bookingRequest = new BookingRequest(1,"TRAIN1",startDate.format(pattern),
                endDate.format(pattern),from,to, JourneyClass.AC1,doj,passengers);

        route = new Route();
        route.setRouteID(1);
        route.setStations(List.of("stn1","stn2","stn3","stn4","stn5"));

        train = new Train();
        train.setRouteId(1);

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testInvalidBookingExceptionCausedByNonExistentTrain() throws InvalidBookingException, TimeTableNotFoundException, BookingNotOpenException {


        //when
        when(trainService.getTrainByNo(1)).thenReturn(Optional.empty());

        //then
        assertThrows(InvalidBookingException.class,()-> bookingServiceUnderTest.book(bookingRequest));

    }

    @Test
    void testInvalidBookingExceptionCausedByIncorrectTrainNo() throws InvalidBookingException, TimeTableNotFoundException, BookingNotOpenException {


        //given

        Route route = new Route();
        route.setRouteID(1);
        route.setStations(List.of("stn1","stn2","stn3","stn4","stn5"));

        Train train = new Train();
        train.setRouteId(1);


        //when
        when(trainService.getTrainByNo(1)).thenReturn(Optional.of(train));

        when(routeInfoService.getByRouteId(1)).thenReturn(Optional.of(route));

        when(routeInfoService.checkIfRouteContains(bookingRequest.getFrom(),
                bookingRequest.getTo(),route)).thenReturn(false);

        //then
        assertThrows(InvalidBookingException.class,()-> bookingServiceUnderTest.book(bookingRequest));


    }

    @Test
    void testBookingNotOpenException() throws InvalidBookingException, TimeTableNotFoundException, BookingNotOpenException {


        //when
        when(trainService.getTrainByNo(1)).thenReturn(Optional.of(train));

        when(routeInfoService.getByRouteId(1)).thenReturn(Optional.of(route));

        when(routeInfoService.checkIfRouteContains(bookingRequest.getFrom(),
                bookingRequest.getTo(),route)).thenReturn(true);

        when(bookingOpenInfoService.isBookingOpen(bookingRequest)).thenReturn(Optional.empty());

        //then
        assertThrows(BookingNotOpenException.class,()-> bookingServiceUnderTest.book(bookingRequest));

    }

    @Test
    void testWhenTrainArrivalDateIsNotEqualToJourneyDate() throws TimeTableNotFoundException {

        //given

        when(trainService.getTrainByNo(1)).thenReturn(Optional.of(train));

        when(routeInfoService.getByRouteId(1)).thenReturn(Optional.of(route));

        when(routeInfoService.checkIfRouteContains(bookingRequest.getFrom(),
                bookingRequest.getTo(),route)).thenReturn(true);

        when(bookingOpenInfoService.isBookingOpen(bookingRequest)).thenReturn(Optional.of(true));

        when(trainArrivalDateService.getArrivalDate(bookingRequest.getTrainNo(),bookingRequest.getFrom(),
                Utils.toLocalDate(bookingRequest.getStartDt())))
                .thenReturn(LocalDate.now());

        //then
        assertThrows(InvalidBookingException.class,()->bookingServiceUnderTest.book(bookingRequest));

    }

    @Test
    void testBookingConfirmed() throws TimeTableNotFoundException, InvalidBookingException, BookingNotOpenException {

        //given

        when(trainService.getTrainByNo(1)).thenReturn(Optional.of(train));

        when(routeInfoService.getByRouteId(1)).thenReturn(Optional.of(route));

        when(routeInfoService.checkIfRouteContains(bookingRequest.getFrom(),
                bookingRequest.getTo(),route)).thenReturn(true);

        when(bookingOpenInfoService.isBookingOpen(bookingRequest)).thenReturn(Optional.of(true));

        when(trainArrivalDateService.getArrivalDate(bookingRequest.getTrainNo(),bookingRequest.getFrom(),
                Utils.toLocalDate(bookingRequest.getStartDt())))
                .thenReturn(startDate.plusDays(1));



        when(seatNoService.getAvailableSeatNumbers(bookingRequest)).thenReturn(Set.of(2,3,4));

        when(seatInfoTrackerService.getCountOfConfirmedSeats(bookingRequest)).thenReturn(1);

        when(bookingInfoTrackerService.trackBooking(any(Passenger.class),any(BookingRequest.class),
                any(BookingStatus.class),anyInt())).thenReturn(2);

        doNothing().when(seatInfoTrackerService)
                .trackLastSeatNo(any(BookingRequest.class),anyInt());

        doNothing().when(seatInfoTrackerService)
                .trackCountOfSeats(bookingRequest,4);

        BookingResponse bookingResponse = bookingServiceUnderTest.book(bookingRequest);

        //then

        for(BookedPassenger passenger:bookingResponse.getPassengerList()){

            assert(passenger.getStatus().equals(BookingStatus.CONFIRMED));
            assert(passenger.getSeatNo() > 0);
        }


    }

    @Test
    void testBookingWaiting() throws InvalidBookingException, TimeTableNotFoundException, BookingNotOpenException {

        //given

        when(trainService.getTrainByNo(1)).thenReturn(Optional.of(train));

        when(routeInfoService.getByRouteId(1)).thenReturn(Optional.of(route));

        when(routeInfoService.checkIfRouteContains(bookingRequest.getFrom(),
                bookingRequest.getTo(),route)).thenReturn(true);

        when(bookingOpenInfoService.isBookingOpen(bookingRequest)).thenReturn(Optional.of(true));

        when(trainArrivalDateService.getArrivalDate(bookingRequest.getTrainNo(),bookingRequest.getFrom(),
                Utils.toLocalDate(bookingRequest.getStartDt())))
                .thenReturn(startDate.plusDays(1));



        when(seatNoService.getAvailableSeatNumbers(bookingRequest)).thenReturn(Set.of(3,4));

        when(seatInfoTrackerService.getCountOfConfirmedSeats(bookingRequest)).thenReturn(2);

        when(bookingInfoTrackerService.trackBooking(any(Passenger.class),any(BookingRequest.class),
                any(BookingStatus.class),anyInt())).thenReturn(2);

        doNothing().when(seatInfoTrackerService)
                .trackLastSeatNo(any(BookingRequest.class),anyInt());

        doNothing().when(seatInfoTrackerService)
                .trackCountOfSeats(bookingRequest,4);

        BookingResponse bookingResponse = bookingServiceUnderTest.book(bookingRequest);

        //then

        for(BookedPassenger passenger:bookingResponse.getPassengerList()){

            if(passenger.getStatus().equals(BookingStatus.CONFIRMED))
                continue;

            assert(passenger.getStatus().equals(BookingStatus.WAITING));
            assert(passenger.getSeatNo() == 0);

        }
    }

    @Test
    void testWaitingTicketCancellation() throws PnrNoIncorrectException {

        //given
        Booking bookingToCancel = new Booking("First Passenger",24,"M",1,
                startDate,endDate,"stn1","stn5",startDate.plusDays(1),JourneyClass.AC1,
                BookingStatus.WAITING, Timestamp.from(Instant.now()),0);

        int pnrNo = 1;

        when(bookingInfoTrackerService.getBookingByPnrNo(pnrNo)).thenReturn(Optional.of(bookingToCancel));

        doNothing().when(bookingInfoTrackerService)
                .deleteBookingByPnrNo(pnrNo);


        String expected = "Deleted Booking For PnrNo:1";

        //then

        String actual = bookingServiceUnderTest.cancelBooking(pnrNo);
        assertEquals(expected,actual);


    }

    @Test
    void testConfirmedTicketCancellation() throws PnrNoIncorrectException{

        //given

        List<Booking> allBookings = new ArrayList<>();

        Booking bookingToCancel = new Booking("First Passenger",24,"M",1,
                startDate,endDate,"stn1","stn3",startDate.plusDays(1),JourneyClass.AC1,
                BookingStatus.CONFIRMED, Timestamp.from(Instant.now()),2);
        bookingToCancel.setPnr(3);

        allBookings.add(bookingToCancel);

        Booking bookingWithSharedSeatNo = new Booking("Third Passenger",27,"M",1,
                startDate,endDate,"stn3","stn5",startDate.plusDays(1),JourneyClass.AC1,
                BookingStatus.CONFIRMED, Timestamp.from(Instant.now()),2);
        bookingWithSharedSeatNo.setPnr(4);

        allBookings.add(bookingWithSharedSeatNo);

        int seatNo = bookingToCancel.getSeatNo();

        List<Booking> waitingList = createWaitingList();

        Booking booking2 = null;
        for(Booking b:waitingList){

            if(b.getPnr() == 2){
                booking2 = b;
                break;
            }
        }

        //then
        when(bookingInfoTrackerService.getBookingByPnrNo(bookingToCancel.getPnr())).thenReturn(Optional.of(bookingToCancel));

        when(bookingInfoTrackerService.getWaitingList(bookingToCancel.getTrainNo(),bookingToCancel.getJourneyClass(),
                bookingToCancel.getStartDt(),bookingToCancel.getEndDt())).thenReturn(Optional.of(waitingList));

        when(bookingInfoTrackerService.getBookingBySeatNumber(seatNo,bookingToCancel)).thenReturn(Optional.of(allBookings));

        when(routeInfoService.isRouteCompatible(eq(booking2),any(List.class))).thenReturn(true);

        when(routeInfoService.isRouteCompatible(not(eq(booking2)),any(List.class))).thenReturn(false);

        doNothing().when(bookingInfoTrackerService)
                .changeBookingToConfirm(booking2.getPnr(),seatNo);

        doNothing().when(bookingInfoTrackerService)
                .deleteBookingByPnrNo(bookingToCancel.getPnr());

        //when
        bookingServiceUnderTest.cancelBooking(bookingToCancel.getPnr());

        //verify
        ArgumentCaptor<Integer> pnrArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(bookingInfoTrackerService).changeBookingToConfirm(pnrArgumentCaptor.capture(),eq(2));

        assertEquals(2,pnrArgumentCaptor.getValue());
    }

    private List<Booking> createWaitingList(){

        List<Booking> waitingList = new ArrayList<>();

        Booking booking1 =  new Booking("First Passenger",24,"M",1,
                startDate,endDate,"stn1","stn5",startDate.plusDays(1),JourneyClass.AC1,
                BookingStatus.WAITING, Timestamp.from(Instant.now()),0);

        booking1.setPnr(1);
        waitingList.add(booking1);

        Booking booking2 =  new Booking("Second Passenger",24,"F",1,
                startDate,endDate,"stn1","stn2",startDate.plusDays(1),JourneyClass.AC1,
                BookingStatus.WAITING, Timestamp.from(Instant.now()),0);

        booking2.setPnr(2);
        waitingList.add(booking2);

        return waitingList;

    }

    @Test
    void testBookingOpen() throws BookingCannotOpenException {

        //given
        int trainNo=1;
        String startDt = startDate.format(pattern).toString();
        String endDt = endDate.format(pattern).toString();

        BookingOpenRequest bookingOpenRequest = new BookingOpenRequest(startDt,endDt);

        //then
        when(trainService.getTrainByNo(trainNo)).thenReturn(Optional.of(new Train()));

        doNothing().when(bookingOpenInfoService)
                .addBookingOpenInfo(trainNo,bookingOpenRequest);

        doNothing().when(seatInfoTrackerService)
                        .initSeatInfoTracker(trainNo,bookingOpenRequest);

        //when
        BookingOpenResponse bookingOpenResponse = bookingServiceUnderTest.openBooking(trainNo,bookingOpenRequest);


        //Verify TrainNo
        ArgumentCaptor<Integer> trainNoCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(bookingOpenInfoService).addBookingOpenInfo(trainNoCaptor.capture(),eq(bookingOpenRequest));
        assertEquals(trainNo,trainNoCaptor.getValue());

        verify(seatInfoTrackerService).initSeatInfoTracker(trainNoCaptor.capture(),eq(bookingOpenRequest));
        assertEquals(trainNo,trainNoCaptor.getValue());


        //Verify BookingOpenRequest
        ArgumentCaptor<BookingOpenRequest> bookingOpenRequestCaptor = ArgumentCaptor.forClass(BookingOpenRequest.class);

        verify(bookingOpenInfoService).addBookingOpenInfo(eq(trainNo),bookingOpenRequestCaptor.capture());
        assertEquals(bookingOpenRequest,bookingOpenRequestCaptor.getValue());

        verify(seatInfoTrackerService).initSeatInfoTracker(eq(trainNo),bookingOpenRequestCaptor.capture());
        assertEquals(bookingOpenRequest,bookingOpenRequestCaptor.getValue());


        assertEquals(true,bookingOpenResponse.isBookingOpen());
        assertEquals(trainNo,bookingOpenResponse.getTrainNo());
        assertEquals(startDt,bookingOpenResponse.getStartDt());
        assertEquals(endDt,bookingOpenResponse.getEndDt());
    }

    @Test
    void testGetBookingOpenInfo() {
    }
}