package com.rail.app.railreservation.trainmanagement.service.impl;

import com.rail.app.railreservation.trainmanagement.repository.TrainRepository;
import com.rail.app.railreservation.enquiry.exception.TrainNotFoundException;
import com.rail.app.railreservation.trainmanagement.dto.TimeTableAddRequest;
import com.rail.app.railreservation.trainmanagement.dto.TimeTableAddResponse;
import com.rail.app.railreservation.trainmanagement.dto.TimeTableEnquiryResponse;
import com.rail.app.railreservation.trainmanagement.entity.TimeTable;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableAddFailException;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableNotFoundException;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableWithoutTrainException;
import com.rail.app.railreservation.trainmanagement.repository.TimeTableRepository;
import com.rail.app.railreservation.trainmanagement.service.TimeTableService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class TimeTableServiceImpl implements TimeTableService {

    private static final Logger logger = LogManager.getLogger(TimeTableServiceImpl.class);

    private static final String INSIDE_TIME_TABLE_SERVICE = "Inside Time Table Service...";

    private final TimeTableRepository timeTableRepo;

    private final TrainRepository trainRepo;

    private final ModelMapper mapper;

    public TimeTableServiceImpl(TimeTableRepository timeTableRepo, TrainRepository trainRepo, ModelMapper mapper) {
        this.timeTableRepo = timeTableRepo;
        this.trainRepo = trainRepo;
        this.mapper = mapper;
    }

    public TimeTableAddResponse addTimeTable(TimeTableAddRequest tmtbladdreq) throws TimeTableWithoutTrainException,TimeTableAddFailException{

        logger.info(INSIDE_TIME_TABLE_SERVICE);
        logger.info("Adding Time Table For TrainNo:{},TrainName:{}",tmtbladdreq.getTrainNo(),tmtbladdreq.getTrainName());

        trainRepo.findByTrainNo(tmtbladdreq.getTrainNo()).orElseThrow(()->new TimeTableWithoutTrainException("Trying To Add Time Table For Train Which Doesn't Exist"));

        TimeTable ttbl = mapper.map(tmtbladdreq,TimeTable.class);
        //ttbl.getTrainTimings().addAll(tmtbladdreq.getTrainTimings());

        ttbl = timeTableRepo.save(ttbl);

        if(ttbl.getTimeTableID() == null){
            throw new TimeTableAddFailException("Failed To Add Time Table For Train No:"+tmtbladdreq.getTrainNo()+", TrainName:"+tmtbladdreq.getTrainName());
        }

        return mapper.map(ttbl,TimeTableAddResponse.class);

    }

    public TimeTableEnquiryResponse getTimeTable(int trainNo) throws TimeTableNotFoundException {

        logger.info(INSIDE_TIME_TABLE_SERVICE);
        logger.info("Retrieving Time Table For TrainNo:{}",trainNo);

        trainRepo.findByTrainNo(trainNo).orElseThrow(()->new TimeTableNotFoundException("Time Table Not Found Because",new TrainNotFoundException("Train With TrainNo:"+trainNo+" Doesn't Exists")));

        TimeTable timeTable = timeTableRepo.findByTrainNo(trainNo).orElseThrow(()->new TimeTableNotFoundException("Time Table Not Yet Added For TrainNo:"+trainNo));

        TimeTableEnquiryResponse timeTableEnquiryResponse = mapper.map(timeTable,TimeTableEnquiryResponse.class);

        return timeTableEnquiryResponse;
    }


}
