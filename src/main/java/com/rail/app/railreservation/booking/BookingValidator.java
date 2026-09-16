package com.rail.app.railreservation.booking;

import com.rail.app.railreservation.booking.dto.BookingOpenRequest;
import com.rail.app.railreservation.booking.dto.BookingRequest;
import com.rail.app.railreservation.booking.exception.BookingNotOpenException;
import com.rail.app.railreservation.booking.exception.InvalidBookingAttemptException;
import com.rail.app.railreservation.booking.exception.InvalidBookingTypeException;
import com.rail.app.railreservation.booking.exception.InvalidDateOfJourneyException;
import com.rail.app.railreservation.booking.repository.BookingOpenRepository;
import com.rail.app.railreservation.enquiry.exception.TrainNotFoundException;
import com.rail.app.railreservation.route.entity.Route;
import com.rail.app.railreservation.route.exception.InvalidJourneyRouteException;
import com.rail.app.railreservation.route.service.RouteService;
import com.rail.app.railreservation.trainmanagement.entity.Train;
import com.rail.app.railreservation.trainmanagement.service.TrainArrivalDateService;
import com.rail.app.railreservation.trainmanagement.service.TrainService;
import com.rail.app.railreservation.util.Utils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class BookingValidator {

    private final TrainService trainService;

    private final RouteService routeService;

    private final BookingOpenRepository bookingOpenRepo;

    private final TrainArrivalDateService trainArrivalDateService;

    public BookingValidator(TrainService trainService, RouteService routeService, BookingOpenRepository bookingOpenRepo, TrainArrivalDateService trainArrivalDateService) {
        this.trainService = trainService;
        this.routeService = routeService;
        this.bookingOpenRepo = bookingOpenRepo;
        this.trainArrivalDateService = trainArrivalDateService;
    }

    public void validate(BookingRequest request) throws InvalidBookingAttemptException {

        Exception cause = null;
        if(!isValidTrainNo(request.getTrainNo())){
            cause = new TrainNotFoundException("Train Not Found For TrainNo: ", request.getTrainNo());
        }
        else if(!isValidDOJ(request.getDoj())){
            cause = new InvalidDateOfJourneyException("Date Of Journey Not Equal To Train Arrival Date",request.getDoj());
        }
        else if(!isBookingOpen(request)){
            cause = new BookingNotOpenException("Booking Not Yet Opened For TrainNo: "+request.getTrainNo());
        }
        else if(!isValidRoute(request.getRoute())){
            cause = new InvalidJourneyRouteException("Invalid Source:"+request.getFrom()+ "And Destination:"+request.getTo());
        }
        else if(!isValidBookingType(request.getBookingType())){
            cause = new InvalidBookingTypeException("Invalid Booking Type",request.getBookingType());
        }
        else return;

        throw new InvalidBookingAttemptException("Invalid Booking Attempt",cause);

    }

    public static void validate(BookingOpenRequest request){}

    private boolean isValidTrainNo(int trainNo){

        boolean isValidTrainNo = false;
        Train trn = trainService.getTrainByNo(trainNo).orElse(null);
        if(trn != null)
            isValidTrainNo = true;

        return isValidTrainNo;

    }

    private boolean isValidRoute(String from,String to,Train trn){

        boolean isRouteValid = false;

        Optional<Route> routeOpt = routeService.getRouteById(trn.getRouteId());

        if(routeOpt.isPresent()){

            Route route = routeOpt.get();

            List<String> stns = route.getStations();

            if(routeService.checkIfRouteContains(from,to,route)) {

                if(stns.indexOf(from) < stns.indexOf(to)) {

                    isRouteValid = true;
                }
            }

        }

        return isRouteValid;

    }

    private boolean isBookingOpen(BookingRequest request){

        Optional<Boolean> isBookingOpenAsOptional = bookingOpenRepo.isBookingOpen(request.getTrainNo(),
                Utils.toLocalDate(request.getStartDt()),
                Utils.toLocalDate(request.getEndDt()));

        return isBookingOpenAsOptional.get();
    }

    private boolean isValidDOJ(BookingRequest request){

        boolean isValidDOJ = false;

        String pssngrJournyStartStn = request.getFrom();

        LocalDate trainStartDateFrmSource = Utils.toLocalDate(request.getStartDt());

        LocalDate dateOfArrival =  trainArrivalDateService.getArrivalDate(request.getTrainNo(),
                pssngrJournyStartStn,trainStartDateFrmSource);

        LocalDate dateOfJourney = Utils.toLocalDate(request.getDoj());

        if(dateOfArrival.equals(dateOfJourney))
            isValidDOJ = true;

        return isValidDOJ;
    }

    private boolean isValidBookingType(BookingRequest request){

        return true;
    }

}
