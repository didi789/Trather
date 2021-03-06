package com.colman.trather.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.colman.trather.models.Trip;
import com.colman.trather.repositories.TripRepo;

import java.util.List;

public class TripViewModel extends AndroidViewModel {
    private final LiveData<List<Trip>> tripListMutableLiveData;
    private final TripRepo tripsRepository;

    public TripViewModel(@NonNull Application application) {
        super(application);
        tripsRepository = TripRepo.getInstance(application);
        tripListMutableLiveData = tripsRepository.getTrips();
    }

    public LiveData<List<Trip>> getTripsLiveData() {
        return tripListMutableLiveData;
    }

    public void reloadTrips() {
        tripsRepository.reloadTrips();
    }
}

