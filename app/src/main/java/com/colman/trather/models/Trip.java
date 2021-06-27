package com.colman.trather.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

import com.colman.trather.services.Utils;

import java.util.ArrayList;
import java.util.Objects;

@Entity(primaryKeys = {"trip_location_lat", "trip_location_lon", "tripId"}, indices = {
        @Index(value = "tripId", unique = true)
})
public class Trip {
    public int tripId;
    @ColumnInfo(name = "trip_location_lat")
    private double locationLat;
    @ColumnInfo(name = "trip_location_lon")
    private double locationLon;
    @ColumnInfo(name = "trip_name")
    private String name;
    @ColumnInfo(name = "trip_info")
    private String about;
    @ColumnInfo(name = "trip_image_url")
    private String imgUrl;
    @ColumnInfo(name = "trip_queue")
    private String queue;
    @ColumnInfo(name = "trip_queueDate")
    private String queueDate;



    public Trip(String name, String about, String imgUrl, double locationLat, double locationLon,String queue,String queueDate) {
        this.name = name;
        this.about = about;
        this.imgUrl = imgUrl;
        this.locationLat = locationLat;
        this.locationLon = locationLon;
        this.tripId = hashCode();
        this.queue = queue;
        this.queueDate = queueDate;
    }
    public Trip(String name, String about, String imgUrl, double locationLat, double locationLon, ArrayList<String> queue, String queueDate) {
        this.name = name;
        this.about = about;
        this.imgUrl = imgUrl;
        this.locationLat = locationLat;
        this.locationLon = locationLon;
        this.tripId = hashCode();
        if (queue != null)
            this.queue = Utils.fromArrayList(queue);
        else
            this.queue = null;
        this.queueDate = queueDate.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public double getLocationLon() {
        return locationLon;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public void setLocationLat(double locationLat) {
        this.locationLat = locationLat;
    }

    public void setLocationLon(double locationLon) {
        this.locationLon = locationLon;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getQueueDate() {
        return queueDate;
    }

    public void setQueueDate(String queueDate) {
        this.queueDate = queueDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return tripId == trip.tripId &&
                Double.compare(trip.locationLat, locationLat) == 0 &&
                Double.compare(trip.locationLon, locationLon) == 0 &&
                Objects.equals(name, trip.name) &&
                Objects.equals(about, trip.about) &&
                Objects.equals(imgUrl, trip.imgUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId, name, about, imgUrl, locationLat, locationLon);
    }



}

