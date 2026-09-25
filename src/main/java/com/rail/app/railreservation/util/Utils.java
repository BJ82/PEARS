package com.rail.app.railreservation.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public class Utils {

    public static LocalDate toLocalDate(String dateAsString){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
        return LocalDate.parse(dateAsString,formatter);
    }

    public static LocalTime toLocalTime(String timeAsString){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
        return LocalTime.parse(timeAsString,formatter);
    }

    public static Optional<Boolean> toOptional(boolean b){

        Optional asOptional;

        if(!b)
            asOptional = Optional.empty();

        asOptional = Optional.of(b);

        return asOptional;
    }

    public static boolean isNull(Object obj){

        return obj != null;
    }
}
