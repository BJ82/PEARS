package com.rail.app.railreservation;

import com.rail.app.railreservation.booking.exception.BookingCannotOpenException;
import com.rail.app.railreservation.booking.exception.BookingNotOpenException;
import com.rail.app.railreservation.booking.exception.TatkalNotOpenException;
import com.rail.app.railreservation.enquiry.exception.*;
import com.rail.app.railreservation.signup.exception.UserPresentException;
import com.rail.app.railreservation.trainmanagement.exception.DuplicateTrainException;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableAddFailException;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableNotFoundException;
import com.rail.app.railreservation.trainmanagement.exception.TimeTableWithoutTrainException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DuplicateTrainException.class)
    public ResponseEntity<String> duplicateTrainExceptionHandler(DuplicateTrainException dupltrnex){

        logger.error(dupltrnex.getMessage());
        logger.error("Train With Name: {} and TrainNo: {} Already Present!!",dupltrnex.getTrnName(),dupltrnex.getTrnNo());
        return  ResponseEntity.status(HttpStatus.FORBIDDEN).header("Cause","Adding Duplicate Train").body("DuplicateTrainException");

    }

    @ExceptionHandler(TrainNotFoundException.class)
    public ResponseEntity<String> trainNotFoundHandler(TrainNotFoundException tnfex){

        logger.error(tnfex.getMessage());
        logger.error("Exception Raised"+tnfex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Cause",tnfex.getMessage()).body(tnfex.getMessage());
    }

    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<String> routeNotFoundHandler(RouteNotFoundException routnf){

        logger.error(routnf.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Cause",routnf.getMessage()).body(routnf.getMessage());
    }

    @ExceptionHandler(TimeTableWithoutTrainException.class)
    public ResponseEntity<String> timeTableWithoutTrainHandler(TimeTableWithoutTrainException timtblwithoutrnex){

        logger.error(timtblwithoutrnex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).header("Cause",timtblwithoutrnex.getMessage()).body(timtblwithoutrnex.getMessage());
    }

    @ExceptionHandler(TimeTableAddFailException.class)
    public ResponseEntity<String> timeTableWithoutTrainHandler(TimeTableAddFailException tmtbladdfail ){

        logger.error(tmtbladdfail.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Cause",tmtbladdfail.getMessage()).body(tmtbladdfail.getMessage());
    }

    @ExceptionHandler(BookingCannotOpenException.class)
    public ResponseEntity<String> bookingCannotOpenExceptionHandler(BookingCannotOpenException bkngcnnotopnex ){

        logger.error(bkngcnnotopnex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).header("Cause",bkngcnnotopnex.getMessage()).body(bkngcnnotopnex.getMessage());
    }

    @ExceptionHandler(BookingNotOpenException.class)
    public ResponseEntity<String> bookingNotOpenExceptionHandler(BookingNotOpenException bkngnotopnex ){

        logger.error(bkngnotopnex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).header("Cause",bkngnotopnex.getMessage()).body(bkngnotopnex.getMessage());
    }

    @ExceptionHandler(TimeTableNotFoundException.class)
    public ResponseEntity<String> timeTableNotFoundExceptionHandler(TimeTableNotFoundException timeTableNotFoundEx){

        String message = timeTableNotFoundEx.getMessage();

        if(timeTableNotFoundEx.getCause() != null){

            message = timeTableNotFoundEx.getMessage()+" "+timeTableNotFoundEx.getCause().getMessage();
            logger.error(message);
        }
        else
            logger.error(message);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
    }

    @ExceptionHandler(InvalidSeatEnquiryException.class)
    public ResponseEntity<String> invalidSeatEnquiryExceptionHandler(InvalidSeatEnquiryException invalidSeatEnquiryEx){

        String message = invalidSeatEnquiryEx.getMessage()+" "+invalidSeatEnquiryEx.getCause().getMessage();
        logger.error(message);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
    }

    @ExceptionHandler(PnrNoIncorrectException.class)
    public ResponseEntity<String> pnrNoIncorrectExceptionHandler(PnrNoIncorrectException pnrNoIncorrectEx){

        logger.error(pnrNoIncorrectEx.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pnrNoIncorrectEx.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> authenticationExceptionHandler(AuthenticationException authEx){

        logger.error(authEx.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authEx.getMessage());
    }
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> userNameNotFoundExceptionHandler(UsernameNotFoundException usrnamenotfndEx){

        logger.error(usrnamenotfndEx.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(usrnamenotfndEx.getMessage());
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<String> signatureExceptionHandler(SignatureException signatureEx){

        logger.error(signatureEx.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(signatureEx.getMessage());
    }

    @ExceptionHandler(UserPresentException.class)
    public ResponseEntity<String> userPresentExceptionHandler(UserPresentException userPresentEx){

        logger.error(userPresentEx.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(userPresentEx.getMessage());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<String> userPresentExceptionHandler(ExpiredJwtException expiredJwtEx){

        logger.error(expiredJwtEx.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(expiredJwtEx.getMessage());
    }
    @ExceptionHandler(TatkalNotOpenException.class)
    public ResponseEntity<String> tatkalNotOpenExceptionHandler(TatkalNotOpenException tatkalNotOpenException){

        logger.error(tatkalNotOpenException.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(tatkalNotOpenException.getMessage());
    }
}
