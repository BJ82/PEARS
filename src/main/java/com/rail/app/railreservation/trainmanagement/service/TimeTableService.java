package com.rail.app.railreservation.trainmanagement.service;

import com.rail.app.railreservation.trainmanagement.dto.TimeTableAddRequest;
import com.rail.app.railreservation.trainmanagement.dto.TimeTableAddResponse;
import com.rail.app.railreservation.trainmanagement.dto.TimeTableEnquiryResponse;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableAddFailException;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableNotFoundException;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableWithoutTrainException;

public interface TimeTableService {

    public TimeTableAddResponse addTimeTable(TimeTableAddRequest tmtbladdreq)
            throws TimeTableWithoutTrainException, TimeTableAddFailException;

    public TimeTableEnquiryResponse getTimeTable(int trainNo) throws TimeTableNotFoundException;

}
