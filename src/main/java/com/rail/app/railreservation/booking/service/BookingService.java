package com.rail.app.railreservation.booking.service;

import com.rail.app.railreservation.booking.dto.*;
import com.rail.app.railreservation.booking.entity.Booking;
import com.rail.app.railreservation.booking.entity.BookingOpen;
import com.rail.app.railreservation.booking.enums.BookingStatus;
import com.rail.app.railreservation.booking.exception.BookingCannotOpenException;
import com.rail.app.railreservation.booking.exception.BookingNotOpenException;
import com.rail.app.railreservation.booking.exception.InvalidBookingException;
import com.rail.app.railreservation.booking.repository.BookingOpenRepository;
import com.rail.app.railreservation.enquiry.exception.PnrNoIncorrectException;
import com.rail.app.railreservation.enquiry.exception.TrainNotFoundException;
import com.rail.app.railreservation.route.entity.Route;
import com.rail.app.railreservation.route.service.RouteInfoService;
import com.rail.app.railreservation.trainmanagement.entity.Train;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableNotFoundException;
import com.rail.app.railreservation.trainmanagement.service.TrainArrivalDateService;
import com.rail.app.railreservation.trainmanagement.service.TrainService;
import com.rail.app.railreservation.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class BookingService {

    private static final Logger logger = LogManager.getLogger(BookingService.class);

    private static final String INSIDE_BOOKING_SERVICE = "Inside Booking Service...";

    private final Set<Integer> seatNumbers;

    private final List<Integer> pnrs;

    private final TrainService trainService;

    private final RouteInfoService routeInfoService;

    private final BookingInfoTrackerService bookingInfoTrackerService;

    private final BookingService bookingService;

    private final BookingOpenRepository bookingOpenRepo;

    private final TrainArrivalDateService trainArrivalDateService;

    private final SeatService seatService;
    private final ModelMapper mapper;

    public BookingService(TrainService trainService,
                          RouteInfoService routeInfoService,
                          BookingInfoTrackerService bookingInfoTrackerService,
                          BookingService bookingService, BookingOpenRepository bookingOpenRepo,
                          TrainArrivalDateService trainArrivalDateService,
                          SeatService seatService,
                          ModelMapper mapper) {

        this.trainService = trainService;
        this.routeInfoService = routeInfoService;
        this.bookingInfoTrackerService = bookingInfoTrackerService;
        this.bookingService = bookingService;
        this.bookingOpenRepo = bookingOpenRepo;
        this.trainArrivalDateService = trainArrivalDateService;
        this.seatService = seatService;
        this.mapper = mapper;
        this.seatNumbers = Collections.synchronizedSet(new LinkedHashSet<>());
        this.pnrs = Collections.synchronizedList(new ArrayList<>());
    }


    public BookingResponse book(BookingRequest request) throws InvalidBookingException, BookingNotOpenException, TimeTableNotFoundException {

        logger.info(INSIDE_BOOKING_SERVICE);

        //Check if Train No is Valid
        Train trn = trainService.getTrainByNo(request.getTrainNo())
                .orElseThrow(() -> new InvalidBookingException("Booking Not Allowed On Non Existent Train"));

        //Check if Route is valid
        isValidRoute(request.getFrom(), request.getTo(), trn)
                .orElseThrow(() -> new InvalidBookingException("TrainNo:" + request.getTrainNo() + " Not Running " + "Between " +
                                                                request.getFrom() + "And " + request.getTo()));
        //Check If Booking Is Allowed
        bookingService.isBookingOpen(request).orElseThrow(()->new BookingNotOpenException("Booking Not Yet Open For TrainNo:"+request.getTrainNo()+" For Dates "+request.getStartDt()+" And "+request.getEndDt()
                                                                      )
                                          );

        //Check if DOJ is Valid
        String pssngrJournyStartStn = request.getFrom();

        LocalDate trainStartDateFrmSource = Utils.toLocalDate(request.getStartDt());

        LocalDate dateOfArrival =  trainArrivalDateService.getArrivalDate(request.getTrainNo(),
                pssngrJournyStartStn,trainStartDateFrmSource);

        LocalDate dateOfJourney = Utils.toLocalDate(request.getDoj());

        if(!dateOfArrival.equals(dateOfJourney))
            throw new InvalidBookingException("Invalid Booking Because ",
                    new TrainNotFoundException("No Train Found For Date Of Journey: "+dateOfJourney.toString()));




        logger.info("Processing Ticket Booking For TrainNo:{}, StartDate:{}, EndDate:{}",
                     request.getTrainNo(),request.getStartDt(),request.getEndDt());


        seatNumbers.clear();
        seatNumbers.addAll(seatService.getAvailableSeatNumbers(request));

        pnrs.clear();

        int seatCount = seatService.getCountOfConfirmedSeats(request);
        int seatNumber = 0;
        BookingStatus bookingStatus = BookingStatus.CONFIRMED;

        int lastSeatNumber = 0;
        int i = 0;
        for (Passenger psngr : request.getPassengers()) {


            if(i < seatNumbers.size()){

                seatNumber = new ArrayList<>(seatNumbers).get(i);
                lastSeatNumber = seatNumber;
                bookingStatus = BookingStatus.CONFIRMED;
                seatCount++;
            }
            else {

                seatNumber = 0;
                bookingStatus = BookingStatus.WAITING;
            }


            int pnrNo = bookingInfoTrackerService.trackBooking(psngr,request,bookingStatus,seatNumber);

            pnrs.add(i,pnrNo);

            i++;

        }

        if(lastSeatNumber != 0) {

            seatService.trackLastSeatNo(request,lastSeatNumber);

        }


        seatService.trackCountOfSeats(request,seatCount);

        BookingResponse bookingResponse = mapper.map(request, BookingResponse.class);
        bookingResponse.setBookingDateTime(Timestamp.from(Instant.now()));

        BookedPassenger bookedPassenger;

        int noOfPsngr = request.getPassengers().size();

        for(int j=0;j<noOfPsngr;j++){

            bookedPassenger = mapper.map(request.getPassengers().get(j),BookedPassenger.class);

            if(j < seatNumbers.size()){

                seatNumber = new ArrayList<>(seatNumbers).get(j);
                bookingStatus = BookingStatus.CONFIRMED;
            }
            else {

                seatNumber = 0;
                bookingStatus = BookingStatus.WAITING;
            }

            bookedPassenger.setPnr(pnrs.get(j));
            bookedPassenger.setSeatNo(seatNumber);
            bookedPassenger.setStatus(bookingStatus);

            bookingResponse.getPassengerList().add(bookedPassenger);

        }

        logger.info("Completed Ticket Booking For TrainNo:{}, StartDate:{}, EndDate:{}",
                             request.getTrainNo(),request.getStartDt(),request.getEndDt());
        return bookingResponse;

    }

    public String cancelBooking(int pnrNo) throws PnrNoIncorrectException{

        Booking bookingToCancel = bookingInfoTrackerService.getBookingByPnrNo(pnrNo)
                .orElseThrow(()->new PnrNoIncorrectException("Check PNR No:"+pnrNo+",As booking Could Not Be Found"));

        logger.info("Processing Request To Cancel Booking For PnrNo:{}",pnrNo);

        if(bookingToCancel.getBookingStatus().equals(BookingStatus.CONFIRMED)){

            int seatNo = bookingToCancel.getSeatNo();

            List<Booking> waitingList = bookingInfoTrackerService.getWaitingList(bookingToCancel.getTrainNo(),bookingToCancel.getJourneyClass(),
                    bookingToCancel.getStartDt(),bookingToCancel.getEndDt()).orElse(new ArrayList<>());

            waitingList = waitingList.stream().sorted((b1,b2)->Integer.compare(b1.getPnr(), b2.getPnr())).toList();

            List<Booking> allBookings = bookingInfoTrackerService.getBookingBySeatNumber(seatNo,bookingToCancel).orElse(new ArrayList<>());

            int pnrToRemove = bookingToCancel.getPnr(); //Exclude bookingToCancel since it will be deleted

            allBookings = allBookings.stream().
                    filter((booking -> booking.getPnr() != pnrToRemove)).toList();

            Booking bookingToConfirm = null;

            for(Booking bookingWithStatusWait:waitingList){

                if(allBookings.isEmpty() || routeInfoService.isRouteCompatible(bookingWithStatusWait,allBookings)){
                    bookingToConfirm = bookingWithStatusWait;
                    break;
                }
            }

            if(bookingToConfirm != null){

                bookingInfoTrackerService.changeBookingToConfirm(bookingToConfirm.getPnr(),seatNo);
                logger.info("Changed Booking Status For PnrNo:{},From Waiting To Confirmed",bookingToConfirm.getPnr());
            }

        }

        bookingInfoTrackerService.deleteBookingByPnrNo(pnrNo);

        logger.info("Booking Cancelled For PnrNo:{}",pnrNo);

        return "Deleted Booking For PnrNo:"+pnrNo;
    }

    public BookingOpenResponse openBooking(int trainNo,BookingOpenRequest request)
            throws BookingCannotOpenException{

        logger.info(INSIDE_BOOKING_SERVICE);

        LocalDate startDt = Utils.toLocalDate(request.getStartDt());

        if(startDt.isBefore(LocalDate.now()))
            throw new BookingCannotOpenException("Booking Open Date Cannot Be In Past.");

        trainService.getTrainByNo(trainNo)
                .orElseThrow(() -> new BookingCannotOpenException("Not Allowed To Open Booking On Non Existent Train"));

        logger.info("Processing To Open Booking For TrainNo:{}, StartDate:{}, EndDate{}",
                trainNo,request.getStartDt(),request.getEndDt());


        bookingService.addBookingOpenInfo(trainNo,request);

        seatService.initialize(trainNo,request);

        logger.info("Booking Opened For TrainNo:{}, StartDate:{}, EndDate:{}",
                trainNo,request.getStartDt(),request.getEndDt());

        return new BookingOpenResponse(trainNo,request.getStartDt(),
                request.getEndDt(),true);
    }

    public BookingOpenInfo getBookingOpenInfo(int trainNo){

        List<BookingOpen> bookingOpens;
        bookingOpens = bookingService.getBookingOpenInfoByTrainNo(trainNo);

        List<BookingOpenDate> bookingOpenDates = new ArrayList<>();

        for(BookingOpen bookingOpen:bookingOpens){
            bookingOpenDates.add(mapper.map(bookingOpen,BookingOpenDate.class));
        }

        return new BookingOpenInfo(trainNo,bookingOpenDates);
    }

    public void addBookingOpenInfo(int trainNo, BookingOpenRequest request){

        bookingOpenRepo.save(new BookingOpen(trainNo, Utils.toLocalDate(request.getStartDt()),
                        Utils.toLocalDate(request.getEndDt()),true,
                        Timestamp.from(Instant.now())
                )
        );

    }

    public Optional<Boolean> isBookingOpen(BookingRequest request){

        Optional<Boolean> isBookingOpenAsOptional = bookingOpenRepo.isBookingOpen(request.getTrainNo(),Utils.toLocalDate(request.getStartDt()),
                Utils.toLocalDate(request.getEndDt()));

        if(isBookingOpenAsOptional.get() == true)
            return isBookingOpenAsOptional;

        return Optional.empty();
    }

    public List<BookingOpen> getBookingOpenInfoByTrainNo(int trainNo){

        return bookingOpenRepo.findByTrainNo(trainNo);
    }


    protected Optional<Boolean> isValidRoute(String jurnyStartStn,String jurnyEndStn,Train trn){

            boolean isRouteValid = false;

            Optional<Route> routeOpt = routeInfoService.getByRouteId(trn.getRouteId());

            if(routeOpt.isPresent()){

                Route route = routeOpt.get();

                List<String> stns = route.getStations();

                if(routeInfoService.checkIfRouteContains(jurnyStartStn,jurnyEndStn,route)) {

                    if(stns.indexOf(jurnyStartStn) < stns.indexOf(jurnyEndStn)) {

                        isRouteValid = true;
                    }
                }

            }

        if(isRouteValid == false)
            return Optional.empty();


        return Optional.of(isRouteValid);
    }


}
