package com.colman.trather.models;

import com.google.firebase.firestore.GeoPoint;

import java.util.Comparator;

public class SortLocation implements Comparator<Trip> {
    private final GeoPoint mCurrent;

    public SortLocation(GeoPoint current) {
        mCurrent = current;
    }

    @Override
    public int compare(Trip place1, Trip place2) {
        double lat1 = place1.getLocationLat();
        double lon1 = place1.getLocationLon();
        double lat2 = place2.getLocationLat();
        double lon2 = place2.getLocationLon();
        double distanceToPlace1 = distance(mCurrent.getLatitude(), mCurrent.getLongitude(), lat1, lon1);
        double distanceToPlace2 = distance(mCurrent.getLatitude(), mCurrent.getLongitude(), lat2, lon2);
        return (int) (distanceToPlace1 - distanceToPlace2);
    }

    public double distance(double fromLat, double fromLon, double toLat, double toLon) {
        double radius = 6378137;   // approximate Earth radius, *in meters*
        double deltaLat = toLat - fromLat;
        double deltaLon = toLon - fromLon;

        double angle = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(deltaLat / 2), 2) + Math.cos(fromLat) * Math.cos(toLat) * Math.pow(Math.sin(deltaLon / 2), 2)));

        return radius * angle;
    }
}
