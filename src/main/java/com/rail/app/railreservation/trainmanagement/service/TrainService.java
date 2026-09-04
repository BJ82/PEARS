package com.rail.app.railreservation.trainmanagement.service;

import com.rail.app.railreservation.trainmanagement.entity.Train;
import com.rail.app.railreservation.route.service.RouteService;
import com.rail.app.railreservation.route.entity.Route;
import com.rail.app.railreservation.enquiry.exception.RouteNotFoundException;
import com.rail.app.railreservation.enquiry.exception.TrainNotFoundException;
import com.rail.app.railreservation.trainmanagement.dto.AllTrainResponse;
import com.rail.app.railreservation.trainmanagement.dto.TrainAddRequest;
import com.rail.app.railreservation.trainmanagement.dto.TrainAddResponse;
import com.rail.app.railreservation.trainmanagement.dto.TrainInfo;
import com.rail.app.railreservation.trainmanagement.exception.DuplicateTrainException;
import com.rail.app.railreservation.trainmanagement.repository.TrainRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainService {

    private static final Logger logger = LogManager.getLogger(TrainService.class);

    private static final String INSIDE_TRAIN_SERVICE = "Inside Train Service...";

    private final TrainRepository trainRepo;

    private final RouteService routeService;

    private final ModelMapper mapper;

    private Integer ROUTE_ID = null;

    public TrainService(TrainRepository trainRepo, RouteService routeService, ModelMapper mapper) {

        this.routeService = routeService;
        this.mapper = mapper;
        this.trainRepo = trainRepo;
    }


    public TrainAddResponse addNewTrain(TrainAddRequest trnReq) throws DuplicateTrainException {

        List<String> stations = trnReq.getStations();
        String src = stations.get(0);
        String dest = stations.get(stations.size() - 1);

        logger.info(INSIDE_TRAIN_SERVICE);
        logger.info("Adding train with name {}, running between {} and {}",trnReq.getTrainName(),src,dest);

        //Step1: Resolve src and dest to RouteID
        if (routeService.getRouteIdsBySrcAndDest(src,dest).isPresent()){

            ROUTE_ID = routeService.getRouteIdsBySrcAndDest(src,dest).get();
            logger.info("Step1: Resolved src and dest to RouteID:{}",ROUTE_ID);

        }

        //Step2: If route for src and dest is not found then add new route
        if (ROUTE_ID == null) {
            routeService.addRoute(stations);

            if (routeService.getRouteIdsBySrcAndDest(src,dest).isPresent())
                ROUTE_ID = routeService.getRouteIdsBySrcAndDest(src,dest).get();

            logger.info("ROUTE_ID:{}",ROUTE_ID);
            logger.info("Step2: Added new route for {} and {}",src,dest);
        }

        //Step3: If train not found then add train
        TrainAddResponse trnAddResponse = null;
        int trainNo = -1;

        String trainName = trnReq.getTrainName();
        Optional<Train> train = trainRepo.findByTrainName(trainName);
        if (train.isEmpty()) {

            logger.info("Step3:Adding Train with Name:{}",trainName);
            Train trainAdded = addTrain(trnReq,ROUTE_ID);

            if (trainAdded.getTrainNo() > 0){

                trnAddResponse = new TrainAddResponse(trainAdded.getTrainNo(), trainName, src, dest, true);
                logger.info("Successfully Added Train with Name:{}, TrainNo{} , running between {} and {}",trainName,trainAdded.getTrainNo(),src,dest);
            }

        } else {

            trainNo = train.get().getTrainNo();
            throw new DuplicateTrainException(trainName,trainNo);

        }
        return trnAddResponse;
    }

    private Train addTrain(TrainAddRequest trnAddReq,Integer routeID){

        Train train = mapper.map(trnAddReq,Train.class);
        train.setRouteId(routeID);
        trainRepo.save(train);

        return train;
    }

    public AllTrainResponse getAllTrains() throws TrainNotFoundException,RouteNotFoundException {

        logger.info(INSIDE_TRAIN_SERVICE);
        logger.info("Getting All Trains...");

        List<Train> trns = trainRepo.findAll();

        if(trns.isEmpty())
            throw new TrainNotFoundException("No Train Found.Pls Add New Train");

        Route route;
        TrainInfo trnInfo;
        AllTrainResponse allTrainResponse = new AllTrainResponse();

        for(Train trn:trns){

            trnInfo =  mapper.map(trn, TrainInfo.class);
            route = routeService.getRouteById(trn.getRouteId()).orElseThrow(()->new RouteNotFoundException("Route Not Found For RouteID: "+trn.getRouteId(),trn.getRouteId()));
            trnInfo.getStns().addAll(route.getStations());
            allTrainResponse.getAllTrains().add(trnInfo);
        }

        return allTrainResponse;
    }

    public  Optional<Train> getTrainByNo(int trainNo){

        return trainRepo.findByTrainNo(trainNo);

    }

    public Optional<Train> getTrainByName(String trainName){

        return trainRepo.findByTrainName(trainName);

    }

    public List<Train> getTrainByRouteIds(List<Integer> routeIDs){

        return trainRepo.findByRouteIdIn(routeIDs);
    }

}
