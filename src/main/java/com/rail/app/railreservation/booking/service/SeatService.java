package com.rail.app.railreservation.booking.service;

import com.rail.app.railreservation.booking.dto.BookingOpenRequest;
import com.rail.app.railreservation.booking.dto.BookingRequest;
import com.rail.app.railreservation.booking.entity.Booking;
import com.rail.app.railreservation.booking.entity.SeatCount;
import com.rail.app.railreservation.booking.entity.SeatNoTracker;
import com.rail.app.railreservation.booking.repository.BookingRepository;
import com.rail.app.railreservation.booking.repository.SeatCountRepository;
import com.rail.app.railreservation.booking.repository.SeatNoTrackerRepository;
import com.rail.app.railreservation.route.entity.Route;
import com.rail.app.railreservation.route.service.RouteService;
import com.rail.app.railreservation.trainmanagement.entity.Train;
import com.rail.app.railreservation.trainmanagement.enums.JourneyClass;
import com.rail.app.railreservation.trainmanagement.service.TrainService;
import com.rail.app.railreservation.util.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SeatService {

    private final SeatNoTrackerRepository seatNoTrackerRepo;

    private final SeatCountRepository seatCountRepo;

    private final BookingRepository bookingRepo;

    private final RouteService routeService;

    private final TrainService trainService;

    private final int totalNoOfSeats;

    public SeatService(SeatNoTrackerRepository seatNoTrackerRepo,
                       SeatCountRepository seatCountRepo, BookingRepository bookingRepo,
                       RouteService routeService, TrainService trainService,
                       @Value("${total.no.of.seats}") int totalNoOfSeats) {

        this.seatNoTrackerRepo = seatNoTrackerRepo;
        this.seatCountRepo = seatCountRepo;
        this.bookingRepo = bookingRepo;
        this.routeService = routeService;
        this.trainService = trainService;
        this.totalNoOfSeats = totalNoOfSeats;
    }


    public Set<Integer> getAvailableSeatNumbers(BookingRequest request){


        Set<Integer> seatNums;
        seatNums = Collections.synchronizedSet(new LinkedHashSet<>());

        AtomicInteger lstAllotedSeatNum;
        lstAllotedSeatNum = new AtomicInteger(getLastAllocatedSeatNo(request));


        int seatsAvailable = totalNoOfSeats - getLastAllocatedSeatNo(request);

        for(int i=1;i<=seatsAvailable;i++){

            seatNums.add(lstAllotedSeatNum.addAndGet(1));
        }


        //From already booked seats
        //Obtain those which would become
        //Vacant Before our Journey Starts

        seatNums.addAll(getSeatNosBefore(request));


        //From already booked seats
        //Obtain those which would be
        //Occupied After our Journey

        seatNums.addAll(getSeatNosAfter(request));


        return seatNums;
    }

    public void trackLastSeatNo(BookingRequest request,int lastGivenSeatNo){

        seatNoTrackerRepo.updateLastSeatNo(request.getTrainNo(),request.getJourneyClass(),
                Utils.toLocalDate(request.getStartDt()),
                Utils.toLocalDate(request.getEndDt()),lastGivenSeatNo);
    }
    public int getLastAllocatedSeatNo(BookingRequest request){

        SeatNoTracker seatNoTracker = seatNoTrackerRepo.findSeatNoTracker(request.getTrainNo(),
                request.getJourneyClass(),
                Utils.toLocalDate(request.getStartDt()),
                Utils.toLocalDate(request.getEndDt())
        );

        return seatNoTracker.getLstSeatNum();

    }

    public List<Integer> getSeatNumbers(String startFrom, String endAt, BookingRequest request){

        return bookingRepo.findSeatNumbers(startFrom,endAt,request.getTrainNo(),
                Utils.toLocalDate(request.getStartDt()),
                Utils.toLocalDate(request.getEndDt()),
                request.getJourneyClass());
    }


    public void trackCountOfSeats(BookingRequest request,int noOfConfirmedSeats){

        seatCountRepo.updateSeatCount(request.getTrainNo(),request.getJourneyClass(),
                Utils.toLocalDate(request.getStartDt()),
                Utils.toLocalDate(request.getEndDt()),noOfConfirmedSeats);


    }
    public int getCountOfConfirmedSeats(BookingRequest request){

        int seatCount = seatCountRepo.findSeatCount(request.getTrainNo(),
                request.getJourneyClass(),
                Utils.toLocalDate(request.getStartDt()),
                Utils.toLocalDate(request.getEndDt()));

        return seatCount;

    }

    public void initSeatNoTracker(int trainNo, BookingOpenRequest request){

        for(JourneyClass jrnyClass:JourneyClass.values()){

            seatNoTrackerRepo.save(new SeatNoTracker(trainNo,
                            jrnyClass,Utils.toLocalDate(request.getStartDt()),
                            Utils.toLocalDate(request.getEndDt()),0
                    )
            );
        }
    }
    public void initSeatCount(int trainNo, BookingOpenRequest request){

        for(JourneyClass jrnyClass:JourneyClass.values()){

            seatCountRepo.save(new SeatCount(trainNo,
                            Utils.toLocalDate(request.getStartDt()),
                            Utils.toLocalDate(request.getEndDt()),jrnyClass,0
                    )
            );
        }
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

                seatNums.addAll(getSeatNumbers(src,dest,request));

            }
        }

        filterSeatNos(seatNums,request);
        return seatNums;
    }

    private void filterSeatNos(Set<Integer> seatNums,BookingRequest request){

        Set<Integer> seatNosToRetain = new LinkedHashSet<>(seatNums);

        for(Integer num:seatNums){

            List<Booking> bookings =  bookingRepo.findBySeatNo(num,request.getTrainNo(),
                                                request.getJourneyClass(),
                                                Utils.toLocalDate(request.getStartDt()),
                                                Utils.toLocalDate(request.getEndDt()));

            String src;
            String dest;
            Integer routeID;
            boolean isOverlapp = false;

            for(Booking bkng:bookings){

                src = bkng.getStartFrom();
                dest = bkng.getEndAt();
                routeID = routeService.getRouteIdsBySrcAndDest(src,dest).get();
                isOverlapp = routeService.getOverlappingRoutes(request.getFrom(),
                        request.getTo()).contains(routeID);

                if(isOverlapp)
                    seatNosToRetain.remove(num);
            }


        }

        seatNums.clear();
        seatNums.addAll(seatNosToRetain);
    }

    private List<Booking> getBookingBySeatNumber(int seatNumber,BookingRequest request){

        return bookingRepo.findBySeatNo(seatNumber,request.getTrainNo(),
                request.getJourneyClass(),
                Utils.toLocalDate(request.getStartDt()),
                Utils.toLocalDate(request.getEndDt()));

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

                seatNums.addAll(getSeatNumbers(src,dest,request));
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

            Route r = routeService.getRouteById(routeID).get();
            allStations.addAll(r.getStations());
        }

        return allStations;
    }

}
