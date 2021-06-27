package com.colman.trather.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.colman.trather.models.Trip;
import com.colman.trather.repositories.TripRepository;

import java.util.List;

public class MapViewModel extends AndroidViewModel {
    private final LiveData<List<Trip>> tripListMutableLiveData;
    private final TripRepository tripRepository;

    public MapViewModel(@NonNull Application application) {
        super(application);
        tripRepository = new TripRepository(application);
        tripListMutableLiveData = tripRepository.getTrips();
    }

    public LiveData<List<Trip>> getTripsLiveData() {
        return tripListMutableLiveData;
    }

}
