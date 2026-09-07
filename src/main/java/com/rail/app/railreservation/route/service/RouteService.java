package com.rail.app.railreservation.route.service;

import com.rail.app.railreservation.booking.entity.Booking;
import com.rail.app.railreservation.route.entity.Route;

import java.util.List;
import java.util.Optional;

public interface RouteService {

    public Optional<Route> getRouteById(int routeId);

    public List<Route> getRoutesBySrcOrDest(String src, String dest);

    public Optional<Integer> getRouteIdsBySrcAndDest(String src, String dest);

    public void addRoute(List<String> stations );

    public boolean isRouteCompatible(Booking booking, List<Booking> bookings);

    public boolean checkIfRouteContains(String stn1,String stn2,Route routeToCheck);

    public List<Integer> getOverlappingRoutes(String src, String dest);
}
