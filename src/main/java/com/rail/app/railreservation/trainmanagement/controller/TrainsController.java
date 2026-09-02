package com.rail.app.railreservation.trainmanagement.controller;

import com.rail.app.railreservation.enquiry.exception.RouteNotFoundException;
import com.rail.app.railreservation.enquiry.exception.TrainNotFoundException;
import com.rail.app.railreservation.trainmanagement.dto.AllTrainResponse;
import com.rail.app.railreservation.trainmanagement.dto.TrainAddRequest;
import com.rail.app.railreservation.trainmanagement.dto.TrainAddResponse;
import com.rail.app.railreservation.trainmanagement.exception.DuplicateTrainException;
import com.rail.app.railreservation.trainmanagement.service.TrainService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("api/v1/trains")
public class TrainsController {

    private static final Logger logger = LogManager.getLogger(TrainsController.class);

    private static final String INSIDE_TRAIN_CONTROLLER = "Inside Trains Controller...";

    private TrainService ts;

    public TrainsController(TrainService ts) {
        this.ts = ts;
    }

    //Ideally should be idempotent.
    //Use put or patch
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<TrainAddResponse> add(@RequestBody TrainAddRequest trnAddReq) throws DuplicateTrainException {

        logger.info(INSIDE_TRAIN_CONTROLLER);
        logger.info("Processing Request To Add New Train, with name:{}",trnAddReq.getTrainName());

        TrainAddResponse trainAddResponse = ts.addNewTrain(trnAddReq);

        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(trainAddResponse.getTrainNo())
                .toUri();

        return ResponseEntity.created(location).body(trainAddResponse);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<AllTrainResponse> getAllTrains() throws TrainNotFoundException, RouteNotFoundException {

        logger.info(INSIDE_TRAIN_CONTROLLER);
        logger.info("Processing Request To Return All Trains..");

        return ResponseEntity.ok().body(ts.getAllTrains());

    }

}
