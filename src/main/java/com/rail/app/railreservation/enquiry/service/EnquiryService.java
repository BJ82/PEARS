package com.rail.app.railreservation.enquiry.service;

import com.rail.app.railreservation.booking.dto.BookingRequest;
import com.rail.app.railreservation.booking.entity.Booking;
import com.rail.app.railreservation.booking.service.BookingService;
import com.rail.app.railreservation.booking.service.SeatService;
import com.rail.app.railreservation.enquiry.exception.PnrNoIncorrectException;
import com.rail.app.railreservation.trainmanagement.service.TrainArrivalDateService;
import com.rail.app.railreservation.trainmanagement.service.TrainService;
import com.rail.app.railreservation.util.Utils;
import com.rail.app.railreservation.trainmanagement.entity.Train;
import com.rail.app.railreservation.route.service.RouteInfoService;
import com.rail.app.railreservation.enquiry.dto.PnrEnquiryResponse;
import com.rail.app.railreservation.enquiry.dto.SeatEnquiryRequest;
import com.rail.app.railreservation.enquiry.dto.SeatEnquiryResponse;
import com.rail.app.railreservation.enquiry.dto.TrainEnquiryResponse;
import com.rail.app.railreservation.route.entity.Route;
import com.rail.app.railreservation.enquiry.exception.InvalidSeatEnquiryException;
import com.rail.app.railreservation.enquiry.exception.RouteNotFoundException;
import com.rail.app.railreservation.enquiry.exception.TrainNotFoundException;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnquiryService {

    private static final Logger logger = LogManager.getLogger(EnquiryService.class);
    private static final String INSIDE_ENQUIRY_SERVICE = "Inside EnquiryService Service...";
    private final RouteInfoService routeInfoService;
    private final TrainService trainService;
    private final BookingService bookingService;
    private final TrainArrivalDateService trainArrivalDateService;

    private final SeatService seatService;
    private final ModelMapper mapper;

    public EnquiryService(RouteInfoService routeInfoService, TrainService trainService,
                          BookingService bookingService, TrainArrivalDateService trainArrivalDateService,
                          SeatService seatService, ModelMapper mapper) {
        this.routeInfoService = routeInfoService;
        this.trainService = trainService;
        this.bookingService = bookingService;
        this.trainArrivalDateService = trainArrivalDateService;
        this.seatService = seatService;
        this.mapper = mapper;
    }


    public List<TrainEnquiryResponse> trainEnquiry(String src,String dest) throws TrainNotFoundException {

        logger.info(INSIDE_ENQUIRY_SERVICE);
        logger.info("Searching for trains running between {} and {}",src,dest);


        // Step1: Obtain route ID for given source and destination
        Integer routeID = null;

        if (routeInfoService.getBySrcAndDest(src,dest).isPresent())
            routeID = routeInfoService.getBySrcAndDest(src,dest).get();

        logger.info("Step1: Obtained routeID:{} for source:{} and destination:{}",routeID,src,dest);


        //Step2: Obtain routes which contain routeID as subroute

        List<Integer> parentRouteIds;
        parentRouteIds = new ArrayList<>();
        parentRouteIds.addAll(getParentRoutes(src,dest));

        logger.info("parentRouteIds size:{}",parentRouteIds.size());

        logger.info("Step2: Obtained parent routes which have routeID:{} as subroute",routeID);

        List<TrainEnquiryResponse> trainEnquiryResponses = new ArrayList<>();
        ModelMapper modelMapper = new ModelMapper();

        //Step3: Obtain trains that are running on parentRouteIds
        List<Train> availableTrains = trainService.getTrainByRouteIds(parentRouteIds);


        if(availableTrains.isEmpty())
            throw new TrainNotFoundException("No Train Found Between Stations "+src+" And "+dest,src,dest);

        logger.info("Number Of Available Trains:{}",availableTrains.size());
        availableTrains.forEach(

                                    (train)-> {
                                                TrainEnquiryResponse trainEnquiryResponse = modelMapper.map(train, TrainEnquiryResponse.class);
                                                trainEnquiryResponse.setSrc(src);
                                                trainEnquiryResponse.setDest(dest);
                                                trainEnquiryResponses.add(trainEnquiryResponse);
                                             }
                                );

        logger.info("Step3: Obtained trains that are running on parentRouteIds");

        return trainEnquiryResponses;
    }

    public TrainEnquiryResponse trainEnquiry(Integer trainNo) throws TrainNotFoundException, RouteNotFoundException {

        logger.info(INSIDE_ENQUIRY_SERVICE);
        logger.info("Searching For Train With TrainNo:{}", trainNo);


        Train trn = trainService.getTrainByNo(trainNo).
                orElseThrow(() -> new TrainNotFoundException("Train Not Found For TrainNo: " + trainNo, trainNo));


        Integer routeID = trn.getRouteId();

        Route route= routeInfoService.getByRouteId(routeID).orElseThrow(()->new RouteNotFoundException("Route Not Found For RouteID: "+routeID,routeID));
        List<String> stations = route.getStations();

        TrainEnquiryResponse trainEnquiryResponse = new ModelMapper().map(trn, TrainEnquiryResponse.class);
        trainEnquiryResponse.setSrc(stations.getFirst());
        trainEnquiryResponse.setDest(stations.getLast());

        return trainEnquiryResponse;
    }


    private List<Integer> getParentRoutes(String src, String dest){

        List<Route> routes = routeInfoService.containsSrcOrDest(src,dest);

        return routes.stream().filter(r->{   boolean isTrue = false;
            if(r.getStations().contains(src)){
                if(r.getStations().contains(dest))
                    isTrue = true;
            }
            return isTrue;
        }).map(r->r.getRouteID()).collect(Collectors.toList());

    }

    public SeatEnquiryResponse seatEnquiry(int trainNo,SeatEnquiryRequest seatEnquiryRequest) throws InvalidSeatEnquiryException, TimeTableNotFoundException {

        logger.info(INSIDE_ENQUIRY_SERVICE);
        logger.info("Searching Available Seats In TrainNo:{}",trainNo);

        String src = seatEnquiryRequest.getFrom();
        String dest = seatEnquiryRequest.getTo();

        List<Integer> parentRouteIds;
        parentRouteIds = new ArrayList<>(getParentRoutes(src, dest));

        List<Train> availableTrains = trainService.getTrainByRouteIds(parentRouteIds);


        if(availableTrains.isEmpty())
            throw new InvalidSeatEnquiryException("Invalid Seat Enquiry Because ",
                                                  new TrainNotFoundException("No Train Found Between Stations "+src+" And "+dest,src,dest));

       LocalDate startDate = Utils.toLocalDate(seatEnquiryRequest.getStartDt());

       LocalDate dateOfArrival =  trainArrivalDateService.getArrivalDate(trainNo,src,startDate);

       LocalDate dateOfJourney = Utils.toLocalDate(seatEnquiryRequest.getDoj());

       if(!dateOfArrival.equals(dateOfJourney))
           throw new InvalidSeatEnquiryException("Invalid Seat Enquiry Because ",
                   new TrainNotFoundException("No Train Found For Date Of Journey: "+dateOfJourney.toString()));


        BookingRequest bookingRequest = mapper.map(seatEnquiryRequest,BookingRequest.class);
        bookingRequest.setTrainNo(trainNo);

        SeatEnquiryResponse seatEnquiryResponse = mapper.map(seatEnquiryRequest,SeatEnquiryResponse.class);
        seatEnquiryResponse.setTrainNo(trainNo);

        int seatsAvailable;
        seatsAvailable = seatService.getAvailableSeatNumbers(bookingRequest).size();
        seatEnquiryResponse.setSeatsAvailable(seatsAvailable);

        return seatEnquiryResponse;
    }

    public PnrEnquiryResponse pnrEnquiry(int pnrNo) throws PnrNoIncorrectException {

        Booking booking;
        booking = bookingService.getBookingByPnrNo(pnrNo).
                  orElseThrow(() -> new PnrNoIncorrectException("Invalid Pnr No.Could not find booking corresponding to pnr no:"+pnrNo));


        PnrEnquiryResponse pnrEnquiryResponse;
        pnrEnquiryResponse = mapper.map(booking,PnrEnquiryResponse.class);

        return pnrEnquiryResponse;
    }

    /*public TrainEnquiryResponse trainEnquiry(String trainName){

    }*/

}
