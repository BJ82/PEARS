package com.rail.app.railreservation.booking.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "seats")
public class TotalSeatsByType{

    private final Map<String,Integer> total = new HashMap<>();

    public Map<String, Integer> getTotal() {
        return total;
    }

}
