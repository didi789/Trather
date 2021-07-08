package com.colman.trather.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.colman.trather.models.Trip;

import java.util.List;

@Dao
public interface TripDao {
    @Query("SELECT * FROM trip")
    LiveData<List<Trip>> getAll();

    @Query("SELECT * FROM trip where tripId = :tripId")
    LiveData<Trip> getTripById(String tripId);

    @Delete
    void delete(Trip trip);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Trip> mTripList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTrip(Trip trip);
}
