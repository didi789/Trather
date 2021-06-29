package com.colman.trather.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

import com.google.firebase.firestore.GeoPoint;

import java.util.Objects;
import java.util.UUID;

@Entity(primaryKeys = {"tripId"}, indices = {
        @Index(value = "tripId", unique = true)
})
public class Trip {
    @NonNull
    public final String tripId;
    @ColumnInfo(name = "trip_location_lat")
    private final double locationLat;
    @ColumnInfo(name = "trip_location_lon")
    private final double locationLon;
    @ColumnInfo(name = "trip_title")
    private final String title;
    @ColumnInfo(name = "trip_info")
    private final String about;
    @ColumnInfo(name = "trip_image_url")
    private String imgUrl;
    @ColumnInfo(name = "rating")
    private final double rating;
    @ColumnInfo(name = "level")
    private final double level;
    @ColumnInfo(name = "water")
    private final boolean water;

    public Trip(GeoPoint location, String title, String about, double level, boolean water) {
        this(UUID.randomUUID().toString(), location == null ? 0 : location.getLatitude(), location == null ? 0 : location.getLongitude(), title, about, null, 0, level, water);
    }

    public Trip(@NonNull String tripId, double locationLat, double locationLon, String title, String about, String imgUrl, double rating, double level, boolean water) {
        this.tripId = tripId;
        this.locationLat = locationLat;
        this.locationLon = locationLon;
        this.title = title;
        this.about = about;
        this.imgUrl = imgUrl;
        this.rating = rating;
        this.level = level;
        this.water = water;
    }

    public String getTripId() {
        return tripId;
    }

    public void setImageUrl(String imageUrl) {
        this.imgUrl = imageUrl;
    }

    public double getLocationLat() {
        return locationLat;
    }

    public double getLocationLon() {
        return locationLon;
    }

    public String getTitle() {
        return title;
    }

    public String getAbout() {
        return about;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public double getRating() {
        return rating;
    }

    public double getLevel() {
        return level;
    }

    public boolean isWater() {
        return water;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trip trip = (Trip) o;
        return tripId == trip.tripId &&
                Double.compare(trip.locationLat, locationLat) == 0 &&
                Double.compare(trip.locationLon, locationLon) == 0 &&
                Objects.equals(trip, trip.title) &&
                Objects.equals(about, trip.about) &&
                Objects.equals(imgUrl, trip.imgUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId, title, about, imgUrl, locationLat, locationLon);
    }
}

