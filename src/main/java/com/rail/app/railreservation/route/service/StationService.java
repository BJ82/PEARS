package com.rail.app.railreservation.route.service;

import java.util.List;

public interface StationService {

    public List<String> getAllStations(int trainNo);

    public List<String> getAllStations(long routeId);

    public List<String> getAllStations(String startFrom, String endAt);

}
