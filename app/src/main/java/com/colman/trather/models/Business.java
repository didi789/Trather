package com.colman.trather.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;

import com.colman.trather.services.Utils;

import java.util.ArrayList;
import java.util.Objects;

@Entity(primaryKeys = {"business_location_lat", "business_location_lon", "businessId"}, indices = {
        @Index(value = "businessId", unique = true)
})
public class Business {
    public int businessId;
    @ColumnInfo(name = "business_location_lat")
    private double locationLat;
    @ColumnInfo(name = "business_location_lon")
    private double locationLon;
    @ColumnInfo(name = "business_name")
    private String name;
    @ColumnInfo(name = "business_info")
    private String about;
    @ColumnInfo(name = "business_image_url")
    private String imgUrl;
    @ColumnInfo(name = "business_queue")
    private String queue;
    @ColumnInfo(name = "business_queueDate")
    private String queueDate;



    public Business(String name, String about, String imgUrl, double locationLat, double locationLon,String queue,String queueDate) {
        this.name = name;
        this.about = about;
        this.imgUrl = imgUrl;
        this.locationLat = locationLat;
        this.locationLon = locationLon;
        this.businessId = hashCode();
        this.queue = queue;
        this.queueDate = queueDate;
    }
    public Business(String name, String about, String imgUrl, double locationLat, double locationLon, ArrayList<String> queue, String queueDate) {
        this.name = name;
        this.about = about;
        this.imgUrl = imgUrl;
        this.locationLat = locationLat;
        this.locationLon = locationLon;
        this.businessId = hashCode();
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

    public int getBusinessId() {
        return businessId;
    }

    public void setBusinessId(int businessId) {
        this.businessId = businessId;
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
        Business business = (Business) o;
        return businessId == business.businessId &&
                Double.compare(business.locationLat, locationLat) == 0 &&
                Double.compare(business.locationLon, locationLon) == 0 &&
                Objects.equals(name, business.name) &&
                Objects.equals(about, business.about) &&
                Objects.equals(imgUrl, business.imgUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(businessId, name, about, imgUrl, locationLat, locationLon);
    }



}

