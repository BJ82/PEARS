package com.rail.app.railreservation.booking.service;

import com.rail.app.railreservation.booking.dto.BookingRequest;
import com.rail.app.railreservation.booking.entity.Booking;
import com.rail.app.railreservation.route.entity.Route;
import com.rail.app.railreservation.route.service.RouteInfoService;
import com.rail.app.railreservation.trainmanagement.entity.Train;
import com.rail.app.railreservation.trainmanagement.service.TrainService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SeatNoService {

    private final SeatInfoTrackerService seatInfoTrackerService;

    private final BookingInfoTrackerService bookingInfoTrackerService;

    private final RouteInfoService routeInfoService;

    private final TrainService trainService;

    private final int totalNoOfSeats;

    public SeatNoService(SeatInfoTrackerService seatInfoTrackerService, BookingInfoTrackerService bookingInfoTrackerService,
                         RouteInfoService routeInfoService, TrainService trainService,
                         @Value("${total.no.of.seats}") int totalNoOfSeats) {

        this.seatInfoTrackerService = seatInfoTrackerService;
        this.bookingInfoTrackerService = bookingInfoTrackerService;
        this.routeInfoService = routeInfoService;
        this.trainService = trainService;
        this.totalNoOfSeats = totalNoOfSeats;
    }


    public Set<Integer> getAvailableSeatNumbers(BookingRequest request){


        Set<Integer> seatNums;
        seatNums = Collections.synchronizedSet(new LinkedHashSet<>());

        AtomicInteger lstAllotedSeatNum;
        lstAllotedSeatNum = new AtomicInteger(seatInfoTrackerService.getLastAllocatedSeatNo(request));


        int seatsAvailable = totalNoOfSeats - seatInfoTrackerService.getLastAllocatedSeatNo(request);

        for(int i=1;i<=seatsAvailable;i++){

            seatNums.add(lstAllotedSeatNum.addAndGet(1));
        }


        //Obtain Seat Nos which would be free
        //Before Journey Starts

        seatNums.addAll(getSeatNosBefore(request));


        //Obtain Seat Nos which would be used
        //After Journey Ends

        seatNums.addAll(getSeatNosAfter(request));


        return seatNums;
    }

    private Set<Integer> getSeatNosBefore(BookingRequest request){

        String src;
        String dest;

        Set<Integer> seatNums = new LinkedHashSet<>();

        List<String> allStations = getAllStations(request.getTrainNo());

        int before = allStations.indexOf(request.getFrom());

        for(int i=0;i<=before;i++){

            src = allStations.get(i);

            for(int j=i+1;j<=before;j++){

                dest = allStations.get(j);

                seatNums.addAll(seatInfoTrackerService.getSeatNumbers(src,dest,request));

            }
        }

        filterSeatNos(seatNums,request);
        return seatNums;
    }

    private void filterSeatNos(Set<Integer> seatNums,BookingRequest request){

        Set<Integer> seatNosToRetain = new LinkedHashSet<>(seatNums);

        for(Integer num:seatNums){

            List<Booking> bookings = bookingInfoTrackerService.getBookingBySeatNumber(num,request);

            String src;
            String dest;
            Integer routeID;
            boolean isOverlapp = false;

            for(Booking bkng:bookings){

                src = bkng.getStartFrom();
                dest = bkng.getEndAt();
                routeID = routeInfoService.getBySrcAndDest(src,dest).get();
                isOverlapp = routeInfoService.getOverlappingRoutes(request.getFrom(),
                        request.getTo()).contains(routeID);

                if(isOverlapp)
                    seatNosToRetain.remove(num);
            }


        }

        seatNums.clear();
        seatNums.addAll(seatNosToRetain);
    }

    private Set<Integer> getSeatNosAfter(BookingRequest request){

        String src;
        String dest;

        Set<Integer> seatNums = new LinkedHashSet<>();

        List<String> allStations = getAllStations(request.getTrainNo());

        int after = allStations.indexOf(request.getTo());

        for (int i = after; i < allStations.size(); i++) {

            src = allStations.get(i);

            for (int j = i + 1; j < allStations.size(); j++) {

                dest = allStations.get(j);

                seatNums.addAll(seatInfoTrackerService.getSeatNumbers(src,dest,request));
            }
        }

        filterSeatNos(seatNums,request);
        return seatNums;
    }

    private List<String> getAllStations(int trainNo){

        List<String> allStations = new ArrayList<>();

        Optional<Train> trainOpt = trainService.getTrainByNo(trainNo);

        if(trainOpt.isPresent()){

            Train train = trainOpt.get();
            int routeID = train.getRouteId();

            Route r = routeInfoService.getByRouteId(routeID).get();
            allStations.addAll(r.getStations());
        }

        return allStations;
    }


}
