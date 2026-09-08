package com.rail.app.railreservation.trainmanagement.service;

import com.rail.app.railreservation.enquiry.exception.RouteNotFoundException;
import com.rail.app.railreservation.enquiry.exception.TrainNotFoundException;
import com.rail.app.railreservation.route.entity.Route;
import com.rail.app.railreservation.trainmanagement.dto.AllTrainResponse;
import com.rail.app.railreservation.trainmanagement.dto.TrainAddRequest;
import com.rail.app.railreservation.trainmanagement.dto.TrainAddResponse;
import com.rail.app.railreservation.trainmanagement.dto.TrainInfo;
import com.rail.app.railreservation.trainmanagement.entity.Train;
import com.rail.app.railreservation.trainmanagement.exception.DuplicateTrainException;

import java.util.List;
import java.util.Optional;

public interface TrainService {

    public TrainAddResponse addNewTrain(TrainAddRequest trnReq) throws DuplicateTrainException;

    public AllTrainResponse getAllTrains() throws TrainNotFoundException, RouteNotFoundException;

    public  Optional<Train> getTrainByNo(int trainNo);

    public Optional<Train> getTrainByName(String trainName);

    public List<Train> getTrainByRouteIds(List<Integer> routeIDs);
}
