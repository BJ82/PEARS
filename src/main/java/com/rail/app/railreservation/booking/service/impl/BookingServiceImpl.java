package com.rail.app.railreservation.booking.service.impl;

import com.rail.app.railreservation.booking.dto.*;
import com.rail.app.railreservation.booking.entity.Booking;
import com.rail.app.railreservation.booking.entity.BookingOpen;
import com.rail.app.railreservation.booking.enums.BookingStatus;
import com.rail.app.railreservation.booking.exception.BookingCannotOpenException;
import com.rail.app.railreservation.booking.exception.BookingNotOpenException;
import com.rail.app.railreservation.booking.exception.InvalidBookingException;
import com.rail.app.railreservation.booking.exception.TatkalNotOpenException;
import com.rail.app.railreservation.booking.repository.BookingOpenRepository;
import com.rail.app.railreservation.booking.repository.BookingRepository;
import com.rail.app.railreservation.booking.service.BookingService;
import com.rail.app.railreservation.booking.service.SeatService;
import com.rail.app.railreservation.enquiry.exception.PnrNoIncorrectException;
import com.rail.app.railreservation.enquiry.exception.TrainNotFoundException;
import com.rail.app.railreservation.route.entity.Route;
import com.rail.app.railreservation.route.service.RouteService;
import com.rail.app.railreservation.trainmanagement.entity.Train;
import com.rail.app.railreservation.trainmanagement.enums.Berth;
import com.rail.app.railreservation.trainmanagement.enums.JourneyClass;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableNotFoundException;
import com.rail.app.railreservation.trainmanagement.service.TrainArrivalDateService;
import com.rail.app.railreservation.trainmanagement.service.TrainService;
import com.rail.app.railreservation.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LogManager.getLogger(BookingServiceImpl.class);

    private static final String INSIDE_BOOKING_SERVICE = "Inside Booking Service...";

    private final Set<Integer> seatNumbers;

    private final List<Integer> pnrs;

    private final TrainService trainService;

    private final RouteService routeService;

    private final BookingRepository bookingRepo;

    private final BookingOpenRepository bookingOpenRepo;

    private final TrainArrivalDateService trainArrivalDateService;

    private final SeatService seatService;
    private final ModelMapper mapper;

    @Value("${tatkal.start.time}")
    private String tatkalStartTime;

    @Value("${tatkal.end.time}")
    private String tatkalEndTime;


    public BookingServiceImpl(TrainService trainService,
                          RouteService routeService,
                          BookingRepository bookingRepo, BookingOpenRepository bookingOpenRepo,
                          TrainArrivalDateService trainArrivalDateService,
                          SeatService seatService,
                          ModelMapper mapper) {

        this.trainService = trainService;
        this.routeService = routeService;
        this.bookingRepo = bookingRepo;
        this.bookingOpenRepo = bookingOpenRepo;
        this.trainArrivalDateService = trainArrivalDateService;
        this.seatService = seatService;
        this.mapper = mapper;
        this.seatNumbers = Collections.synchronizedSet(new LinkedHashSet<>());
        this.pnrs = Collections.synchronizedList(new ArrayList<>());
    }

    public BookingResponse bookTicket(BookingRequest request) throws InvalidBookingException, BookingNotOpenException, TimeTableNotFoundException, TatkalNotOpenException {

        BookingResponse response = null;

        String bookingType = request.getBookingType();

        response = switch (bookingType) {
            case "general" -> book(request);
            case "tatkal" -> bookTatkal(request);
            case "ladies" -> bookLadies(request);
            case "senior" -> bookSeniorCitizen(request);
            case "child" -> bookChild(request);
            default -> throw new InvalidBookingException("Booking Category Type Is Invalid");
        };

        return response;
    }

    private BookingResponse bookTatkal(BookingRequest request)
            throws InvalidBookingException, TimeTableNotFoundException, BookingNotOpenException, TatkalNotOpenException {

        BookingResponse response = null;

        LocalDate doj = Utils.toLocalDate(request.getDoj());

        if(isTatkalOpen(request.getTrainNo())){

            response = book(request);
        }
        else{
            throw new TatkalNotOpenException("Tatkal Booking Not Yet Started!");
        }

      return response;
    }

    public boolean isTatkalOpen(int trainNo){

        boolean isTatkalOpen = false;

        logger.info("TATKAL START TIME: {}",tatkalStartTime);
        LocalTime tatkalStart = Utils.toLocalTime(tatkalStartTime);
        logger.info("TATKAL END TIME: {}",tatkalEndTime);
        LocalTime tatkalEnd  = Utils.toLocalTime(tatkalEndTime);

        LocalDate trainOriginStartDt = getTrainOriginStartDate(trainNo);
        LocalDate trainOriginStartDtMinusOneDay = trainOriginStartDt.minusDays(1);

        LocalTime now = LocalTime.now();
        if(LocalDate.now().equals(trainOriginStartDtMinusOneDay)){
            if(now.equals(tatkalStart) || (now.isAfter(tatkalStart) && now.isBefore(tatkalEnd))){
                isTatkalOpen = true;
            }
        }

        return isTatkalOpen;
    }

    private LocalDate getTrainOriginStartDate(int trainNo){

        LocalDate startDt = null;
        for(BookingOpen bookingOpen:bookingOpenRepo.findByTrainNo(trainNo)){

            if(LocalDate.now().isBefore(bookingOpen.getStartDt())){
                startDt =  bookingOpen.getStartDt();
                break;
            }
        }

        return startDt;
    }

    private BookingResponse bookLadies(BookingRequest request)
            throws InvalidBookingException, TimeTableNotFoundException, BookingNotOpenException{

        for(Passenger p:request.getPassengers()){

            if(!"F".equals(p.getSex()))
                throw new InvalidBookingException("Only Female Passengers Allowed On Ladies Quota");
        }

        return book(request);
    }

    private BookingResponse bookSeniorCitizen(BookingRequest request)
            throws InvalidBookingException, TimeTableNotFoundException, BookingNotOpenException{

        for(Passenger p:request.getPassengers()){

            if(p.getAge() < 60)
                throw new InvalidBookingException("Age Should Be Greater Than 60 On Senior Citizen Quota");
        }
        return book(request);
    }

    private BookingResponse bookChild(BookingRequest request)
            throws InvalidBookingException, TimeTableNotFoundException, BookingNotOpenException{

        for(Passenger p:request.getPassengers()){

            if(p.getAge() > 5)
                throw new InvalidBookingException("Age Should Be Less Or Equal To 5 On Child Quota");
        }

        return book(request);
    }

    private BookingResponse book(BookingRequest request) throws InvalidBookingException, BookingNotOpenException, TimeTableNotFoundException {

        logger.info(INSIDE_BOOKING_SERVICE);

        //Check if Train No is Valid
        Train trn = trainService.getTrainByNo(request.getTrainNo())
                .orElseThrow(() -> new InvalidBookingException("Booking Not Allowed On Non Existent Train"));

        //Check if Route is valid
        isValidRoute(request.getFrom(), request.getTo(), trn)
                .orElseThrow(() -> new InvalidBookingException("TrainNo:" + request.getTrainNo() + " Not Running " + "Between " +
                        request.getFrom() + "And " + request.getTo()));
        //Check If Booking Is Allowed
        isBookingOpen(request).orElseThrow(()->new BookingNotOpenException("Booking Not Yet Open For TrainNo:"+request.getTrainNo()+" For Dates "+request.getStartDt()+" And "+request.getEndDt()
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
        int lastSeatNumber = 0;
        int i = 0;

        Berth berth;
        List<Berth> berths = new ArrayList<>();
        BookingStatus bookingStatus;

        for (Passenger psngr : request.getPassengers()) {

            berth = Berth.UNASSIGNED;
            seatNumber = 0;
            bookingStatus = BookingStatus.WAITING;

            if(i < seatNumbers.size()){

                seatNumber = new ArrayList<>(seatNumbers).get(i);
                lastSeatNumber = seatNumber;
                bookingStatus = BookingStatus.CONFIRMED;
                berths.add(getBerth(request.getTrainNo(),seatNumber,request.getJourneyClass()));
                berth = berths.get(i);
                seatCount++;
            }


            Booking bkng =  bookingRepo.save(new Booking(psngr.getName(), psngr.getAge(), psngr.getSex(),
                                                        request.getTrainNo(), Utils.toLocalDate(request.getStartDt()),
                                                        Utils.toLocalDate(request.getEndDt()),
                                                        request.getFrom(),request.getTo(), Utils.toLocalDate(request.getDoj()),
                                                        request.getBookingType(),request.getJourneyClass(), bookingStatus, Timestamp.from(Instant.now()),
                                                        seatNumber,berth));


            pnrs.add(i,bkng.getPnr());


            i++;

        }

        if(lastSeatNumber != 0) {

            seatService.trackLastSeatNo(request,lastSeatNumber);

        }


        seatService.trackCountOfSeats(request,seatCount);

        logger.info("Completed Ticket Booking For TrainNo:{}, StartDate:{}, EndDate:{}",
                request.getTrainNo(),request.getStartDt(),request.getEndDt());

        return getBookingResponse(request,seatNumbers,pnrs,berths);

    }

    private BookingResponse getBookingResponse(BookingRequest request,Set<Integer> seatNumbers,List<Integer> pnrs,List<Berth> berths){

        BookingResponse bookingResponse = mapper.map(request, BookingResponse.class);

        List<BookedPassenger> bookedPassengers = toBookedPassenger(request.getPassengers(),seatNumbers,pnrs,berths);

        bookingResponse.getPassengerList().addAll(bookedPassengers);
        bookingResponse.setBookingDateTime(Timestamp.from(Instant.now()));

        return bookingResponse;
    }

    private List<BookedPassenger> toBookedPassenger(List<Passenger> passengers,Set<Integer> seatNumbers,List<Integer> pnrs,List<Berth> berths){

        int seatNumber = 0;
        BookingStatus bookingStatus;

        List<BookedPassenger> bookedPassengers = new ArrayList<>();

        Berth berth;
        BookedPassenger bookedPassenger;
        int noOfPsngr = passengers.size();

        for(int j=0;j<noOfPsngr;j++) {

            seatNumber = 0;
            berth = Berth.UNASSIGNED;
            bookingStatus = BookingStatus.WAITING;

            if (j < seatNumbers.size()) {

                seatNumber = new ArrayList<>(seatNumbers).get(j);
                bookingStatus = BookingStatus.CONFIRMED;
                berth = berths.get(j);
            }

            bookedPassenger = mapper.map(passengers.get(j), BookedPassenger.class);
            bookedPassenger.setPnr(pnrs.get(j));
            bookedPassenger.setSeatNo(seatNumber);
            bookedPassenger.setStatus(bookingStatus);
            bookedPassenger.setBerth(berth);

        }

        return bookedPassengers;
    }

    private Berth getBerth(int trainNo,int seatNo,JourneyClass journeyClass) {

        Train trn = trainService.getTrainByNo(trainNo).get();
        Berth berth = trn.getSeats().stream().filter((s)->s.getSeatNo() == seatNo
                                                     && s.getJourneyClass().equals(journeyClass)
                                                  ).findFirst().get().getBerth();
         return berth;

    }
    public String cancelBooking(int pnrNo) throws PnrNoIncorrectException{

        Booking bookingToCancel = getBookingByPnrNo(pnrNo)
                .orElseThrow(()->new PnrNoIncorrectException("Check PNR No:"+pnrNo+",As booking Could Not Be Found"));

        logger.info("Processing Request To Cancel Booking For PnrNo:{}",pnrNo);

        if(bookingToCancel.getBookingStatus().equals(BookingStatus.CONFIRMED)){

            int seatNo = bookingToCancel.getSeatNo();

            List<Booking> waitingList = getWaitingList(bookingToCancel.getTrainNo(),bookingToCancel.getJourneyClass(),
                    bookingToCancel.getStartDt(),bookingToCancel.getEndDt()).orElse(new ArrayList<>());

            waitingList = waitingList.stream().
                    sorted((b1,b2)->Integer.compare(b1.getPnr(), b2.getPnr())).toList();

            List<Booking> allBookings = Optional.of(getBookingBySeatNumber(seatNo,bookingToCancel)).orElse(new ArrayList<>());

            int pnrToRemove = bookingToCancel.getPnr(); //Exclude bookingToCancel since it will be deleted

            allBookings = allBookings.stream().
                    filter((booking -> booking.getPnr() != pnrToRemove)).toList();

            Booking bookingToConfirm = null;

            for(Booking bookingWithStatusWait:waitingList){

                if(allBookings.isEmpty() || routeService.isRouteCompatible(bookingWithStatusWait,allBookings)){
                    bookingToConfirm = bookingWithStatusWait;
                    break;
                }
            }

            if(bookingToConfirm != null){

                bookingRepo.updateBooking(bookingToConfirm.getPnr(),seatNo,BookingStatus.CONFIRMED);
                logger.info("Changed Booking Status For PnrNo:{},From Waiting To Confirmed",bookingToConfirm.getPnr());
            }

        }

        deleteByPnrNo(pnrNo);

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


        addBookingOpenInfo(trainNo,request);

        seatService.initSeatNoTracker(trainNo,request);

        seatService.initSeatCount(trainNo,request);

        logger.info("Booking Opened For TrainNo:{}, StartDate:{}, EndDate:{}",
                trainNo,request.getStartDt(),request.getEndDt());

        return new BookingOpenResponse(trainNo,request.getStartDt(),
                request.getEndDt(),true);
    }

    public BookingOpenInfo getBookingOpenInfo(int trainNo){

        List<BookingOpen> bookingOpens;
        bookingOpens = getBookingOpenInfoByTrainNo(trainNo);

        List<BookingOpenDate> bookingOpenDates = new ArrayList<>();

        for(BookingOpen bookingOpen:bookingOpens){
            bookingOpenDates.add(mapper.map(bookingOpen,BookingOpenDate.class));
        }

        return new BookingOpenInfo(trainNo,bookingOpenDates);
    }

    private void addBookingOpenInfo(int trainNo, BookingOpenRequest request){

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

            Optional<Route> routeOpt = routeService.getRouteById(trn.getRouteId());

            if(routeOpt.isPresent()){

                Route route = routeOpt.get();

                List<String> stns = route.getStations();

                if(routeService.checkIfRouteContains(jurnyStartStn,jurnyEndStn,route)) {

                    if(stns.indexOf(jurnyStartStn) < stns.indexOf(jurnyEndStn)) {

                        isRouteValid = true;
                    }
                }

            }

        if(isRouteValid == false)
            return Optional.empty();


        return Optional.of(isRouteValid);
    }

    public List<Booking> getBookingBySeatNumber(int seatNumber,Booking booking){

        return bookingRepo.findBySeatNo(seatNumber, booking.getTrainNo(),
                booking.getJourneyClass(),
                booking.getStartDt(),
                booking.getEndDt());

    }

    public void trackBookingOpen(int trainNo, BookingOpenRequest request){

        bookingOpenRepo.save(new BookingOpen(trainNo,Utils.toLocalDate(request.getStartDt()),
                        Utils.toLocalDate(request.getEndDt()),true,
                        Timestamp.from(Instant.now())
                )
        );

    }

    public Optional<Booking> getBookingByPnrNo(int pnrNo){

        return bookingRepo.findById(pnrNo);
    }

    private void deleteByPnrNo(int pnrNo){

        bookingRepo.deleteById(pnrNo);
    }

    public Optional<List<Booking>> getWaitingList(int trainNo,
                                                  JourneyClass jrnyClass, LocalDate strtDt,
                                                  LocalDate endDt){

        return bookingRepo.findByBookingStatus(BookingStatus.WAITING,trainNo,jrnyClass,strtDt,endDt);

    }

}
