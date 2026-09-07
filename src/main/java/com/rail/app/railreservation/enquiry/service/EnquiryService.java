package com.rail.app.railreservation.enquiry.service;

import com.rail.app.railreservation.enquiry.dto.PnrEnquiryResponse;
import com.rail.app.railreservation.enquiry.dto.SeatEnquiryRequest;
import com.rail.app.railreservation.enquiry.dto.SeatEnquiryResponse;
import com.rail.app.railreservation.enquiry.dto.TrainEnquiryResponse;
import com.rail.app.railreservation.enquiry.exception.InvalidSeatEnquiryException;
import com.rail.app.railreservation.enquiry.exception.PnrNoIncorrectException;
import com.rail.app.railreservation.enquiry.exception.RouteNotFoundException;
import com.rail.app.railreservation.enquiry.exception.TrainNotFoundException;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableNotFoundException;

import java.util.List;

public interface EnquiryService {

    public List<TrainEnquiryResponse> trainEnquiry(String src, String dest) throws TrainNotFoundException;

    public TrainEnquiryResponse trainEnquiry(Integer trainNo) throws TrainNotFoundException, RouteNotFoundException;

    public SeatEnquiryResponse seatEnquiry(int trainNo, SeatEnquiryRequest seatEnquiryRequest) throws InvalidSeatEnquiryException, TimeTableNotFoundException;

    public PnrEnquiryResponse pnrEnquiry(int pnrNo) throws PnrNoIncorrectException;

}
