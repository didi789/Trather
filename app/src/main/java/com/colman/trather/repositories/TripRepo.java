package com.colman.trather.repositories;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.LiveData;

import com.colman.trather.TripDatabase;
import com.colman.trather.dao.TripDao;
import com.colman.trather.models.ModelFirebase;
import com.colman.trather.models.Trip;

import java.util.List;
import java.util.stream.Collectors;

public class TripRepo {
    private final TripDao tripDao;
    private final LiveData<List<Trip>> allTrips;

    private static TripRepo mInstance;

    public static TripRepo getInstance(Application application) {
        if (mInstance == null) {
            mInstance = new TripRepo(application);
        }

        return mInstance;
    }

    private TripRepo(Application application) {
        TripDatabase database = TripDatabase.getDatabase(application);
        tripDao = database.tripDao();
        allTrips = tripDao.getAll();
        loadTrips();
    }

    public void loadTrips() {
        ModelFirebase.loadTrips(trips -> TripDatabase.databaseWriteExecutor.execute(() -> {
            trips.stream().filter(Trip::isDeleted).forEach(tripDao::delete);
            tripDao.insertAll(trips.stream().filter(t -> !t.isDeleted()).collect(Collectors.toList()));
        }));
    }


    public LiveData<List<Trip>> getTrips() {
        return allTrips;
    }

    public void reloadTrips() {
        this.loadTrips();
    }

    public LiveData<Trip> getTripById(String tripId) {
        return tripDao.getTripById(tripId);
    }

    public void deleteTrip(Trip trip, ModelFirebase.OnCompleteListener<Boolean> listener) {
        TripDatabase.databaseWriteExecutor.execute(() -> tripDao.delete(trip));
        ModelFirebase.deleteTrip(trip, listener);
    }

    public void addTrip(Trip trip, Uri imageUri, ModelFirebase.OnCompleteListener<Boolean> listener) {
        ModelFirebase.addTrip(trip, imageUri, tripId -> {
            if (tripId != null) {
                trip.setTripId(tripId);
                TripDatabase.databaseWriteExecutor.execute(() -> tripDao.insertTrip(trip));
                listener.onComplete(true);
            } else
                listener.onComplete(false);
        });
    }

    public void editTrip(Trip trip, Uri imageUri, boolean isImgEdited, ModelFirebase.OnCompleteListener<Boolean> listener) {
        ModelFirebase.editTrip(trip, imageUri, isImgEdited, callback -> {
            if (callback)
                TripDatabase.databaseWriteExecutor.execute(() -> tripDao.insertTrip(trip));
            listener.onComplete(callback);
        });
    }
}

