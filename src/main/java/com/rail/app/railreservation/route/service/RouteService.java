package com.rail.app.railreservation.route.service;

import com.rail.app.railreservation.booking.entity.Booking;
import com.rail.app.railreservation.route.entity.Route;
import com.rail.app.railreservation.route.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private final RouteRepository routeRepo;

    public RouteService(RouteRepository routeRepo) {
        this.routeRepo = routeRepo;
    }

    public Optional<Route> getRouteById(int routeId){

        return routeRepo.findByRouteID(routeId);
    }

    public List<Route> getRoutesBySrcOrDest(String src, String dest){

        return routeRepo.findBySrcAndDestn(src,dest);
    }

    public Optional<Integer> getRouteIdsBySrcAndDest(String src, String dest){

        List<Route> routes = routeRepo.findBySrcAndDestn(src, dest);

        return routes.stream().filter(r->{   boolean isTrue = false;
            if(r.getStations().get(0).equals(src)){
                if(r.getStations().get(r.getStations().size()-1).equals(dest))
                    isTrue = true;
            }
            return isTrue;
        }).map(r->r.getRouteID()).findFirst();
    }

    public void addRoute(List<String> stations ){

        List<String> stns = new ArrayList<>();
        for (int i = 0; i < stations.size(); i++) {

            stns.clear();
            stns.add(stations.get(i));

            for (int j = i + 1; j < stations.size(); j++) {

                stns.add(stations.get(j));

                Route newRoute = new Route();
                newRoute.getStations().addAll(stns);
                routeRepo.save(newRoute);
            }

        }
    }

    public boolean isRouteCompatible(Booking booking,List<Booking> bookings) {

        String startFrom = booking.getStartFrom();
        String endAt = booking.getEndAt();

        List<String> stations;

        String allStations;

        boolean isCompatible = false;

        for (Booking b : bookings) {

            isCompatible = false;
            stations = getRoute(b.getStartFrom(),b.getEndAt()).getStations();
            allStations = String.join("", stations);
            if (allStations.indexOf(startFrom) == -1
                    || allStations.indexOf(startFrom) == allStations.indexOf(stations.getLast())) {

                if (allStations.indexOf(endAt) == -1
                        || allStations.indexOf(endAt) == allStations.indexOf(stations.getFirst())) {

                    isCompatible = true;
                }
            }

            if(isCompatible == false)
                break;
        }

        return isCompatible;
    }

    private Route getRoute(String startFrom,String endAt){

        int routeId = getRouteIdsBySrcAndDest(startFrom,endAt).get();

        return getRouteById(routeId).get();
    }

    public boolean checkIfRouteContains(String stn1,String stn2,Route routeToCheck){

        List<String> stations = routeToCheck.getStations();

        return stations.contains(stn1) && stations.contains(stn2);

    }

    public List<Integer> getOverlappingRoutes(String src, String dest){

        List<Route> routes = getRoutesBySrcOrDest(src,dest);

        List<Route> overlappingRoutes = new ArrayList<>(routes);

        for(Route route:overlappingRoutes){

            if(route.getStations().getLast().equals(src) || route.getStations().getFirst().equals(dest))
                routes.remove(route);
        }

        return routes.stream().map(r->r.getRouteID()).collect(Collectors.toList());

    }

}


