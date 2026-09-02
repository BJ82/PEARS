package com.rail.app.railreservation.enquiry.controller;

import com.rail.app.railreservation.enquiry.dto.PnrEnquiryResponse;
import com.rail.app.railreservation.enquiry.dto.SeatEnquiryRequest;
import com.rail.app.railreservation.enquiry.dto.SeatEnquiryResponse;
import com.rail.app.railreservation.enquiry.exception.InvalidSeatEnquiryException;
import com.rail.app.railreservation.enquiry.exception.PnrNoIncorrectException;
import com.rail.app.railreservation.enquiry.exception.RouteNotFoundException;
import com.rail.app.railreservation.enquiry.service.EnquiryService;
import com.rail.app.railreservation.enquiry.exception.TrainNotFoundException;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.rail.app.railreservation.enquiry.dto.TrainEnquiryResponse;

import java.util.List;

@RestController
@RequestMapping("api/v1/")
public class EnquiryController {

    private static final Logger logger = LogManager.getLogger(EnquiryController.class);

    private static final String INSIDE_ENQUIRY_CONTROLLER = "Inside EnquiryService Controller...";

    private EnquiryService enquiryService;

    public EnquiryController(EnquiryService enquiryService) {
        this.enquiryService = enquiryService;
    }

    @GetMapping("trains/{src}/{dest}")
    public ResponseEntity<List<TrainEnquiryResponse>> trainEnquiryByStation(@PathVariable String src,@PathVariable String dest)
            throws TrainNotFoundException {

        logger.info(INSIDE_ENQUIRY_CONTROLLER);
        logger.info("Processing request to find all trains between {} and {}",src,dest);

        return ResponseEntity.ok().body(enquiryService.trainEnquiry(src,dest));
    }

    @GetMapping("trains/{trainNo}")
    public ResponseEntity<TrainEnquiryResponse> trainEnquiryByTrainNo(@PathVariable("trainNo") int trnNo) throws TrainNotFoundException, RouteNotFoundException {

        logger.info(INSIDE_ENQUIRY_CONTROLLER);
        logger.info("Processing request to find train with TrainNo:{}",trnNo);

        return ResponseEntity.ok().body(enquiryService.trainEnquiry(trnNo));
    }

    //Ideally should be idempotent.
    //Use put or patch
    @PostMapping("seats/trains/{trainNo}")
    public ResponseEntity<SeatEnquiryResponse> seatEnquiry(@PathVariable("trainNo")int trainNo,@RequestBody SeatEnquiryRequest seatEnquiryRequest) throws InvalidSeatEnquiryException, TimeTableNotFoundException {

        logger.info(INSIDE_ENQUIRY_CONTROLLER);
        logger.info("Processing request to find available seats");

        SeatEnquiryResponse seatEnquiryResponse;
        seatEnquiryResponse = enquiryService.seatEnquiry(trainNo,seatEnquiryRequest);

        return ResponseEntity.ok().body(seatEnquiryResponse);
    }

    @GetMapping("pnrs/{pnrNo}")
    public ResponseEntity<PnrEnquiryResponse> pnrEnquiry(@PathVariable("pnrNo") int pnrNo) throws PnrNoIncorrectException {

        PnrEnquiryResponse pnrEnquiryResponse;
        pnrEnquiryResponse = enquiryService.pnrEnquiry(pnrNo);

       return ResponseEntity.ok().body(pnrEnquiryResponse);

    }

}
