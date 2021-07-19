package com.colman.trather.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.GeoPoint;

import java.util.Objects;

@Entity(tableName = "trips", indices = {
        @Index(value = "tripId", unique = true)
})
public class Trip {
    @PrimaryKey
    @NonNull
    public String tripId;
    @ColumnInfo(name = "trip_location_lat")
    private final double locationLat;
    @ColumnInfo(name = "trip_location_lon")
    private final double locationLon;
    @ColumnInfo(name = "trip_title")
    private final String title;
    @ColumnInfo(name = "trip_site_url")
    private final String tripSiteUrl;
    @ColumnInfo(name = "trip_info")
    private final String about;
    @ColumnInfo(name = "author_uid")
    private final String authorUid;
    @ColumnInfo(name = "trip_image_url")
    private String imgUrl;
    @ColumnInfo(name = "rating")
    private final double rating;
    @ColumnInfo(name = "level")
    private final double level;
    @ColumnInfo(name = "water")
    private final boolean water;

    public Trip(GeoPoint location, String title, String tripSiteUrl, String about, String authorUid, double level, boolean water) {
        this(null, location == null ? 0 : location.getLatitude(), location == null ? 0 : location.getLongitude(), title, tripSiteUrl, about, authorUid, null, 0, level, water);
    }

    public Trip(String tripId, GeoPoint location, String title, String tripSiteUrl, String about, String authorUid, double rating, double level, boolean water) {
        this(tripId, location == null ? 0 : location.getLatitude(), location == null ? 0 : location.getLongitude(), title, tripSiteUrl, about, authorUid, null, rating, level, water);
    }

    public Trip(@NonNull String tripId, double locationLat, double locationLon, String title, String tripSiteUrl, String about, String authorUid, String imgUrl, double rating, double level, boolean water) {
        this.tripId = tripId;
        this.locationLat = locationLat;
        this.locationLon = locationLon;
        this.title = title;
        this.tripSiteUrl = tripSiteUrl;
        this.about = about;
        this.authorUid = authorUid;
        this.imgUrl = imgUrl;
        this.rating = rating;
        this.level = level;
        this.water = water;
    }

    public void setTripId(@NonNull String tripId) {
        this.tripId = tripId;
    }

    public String getTripId() {
        return tripId;
    }

    public String getAuthorUid() {
        return authorUid;
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

    public String getTripSiteUrl() { return tripSiteUrl; }

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
        return tripId.equals(trip.tripId) &&
                Double.compare(trip.locationLat, locationLat) == 0 &&
                Double.compare(trip.locationLon, locationLon) == 0 &&
                Objects.equals(title, trip.title) &&
                Objects.equals(tripSiteUrl, trip.tripSiteUrl) &&
                Objects.equals(about, trip.about) &&
                Objects.equals(imgUrl, trip.imgUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId, title, tripSiteUrl, about, imgUrl, locationLat, locationLon);
    }

    public boolean filter(String s) {
        return this.title.toLowerCase().contains(s.toLowerCase());
    }
}

