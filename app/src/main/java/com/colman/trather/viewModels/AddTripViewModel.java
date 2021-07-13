package com.colman.trather.viewModels;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.colman.trather.R;
import com.colman.trather.models.AddTripState;
import com.colman.trather.models.Trip;
import com.colman.trather.repositories.TripRepository;
import com.google.firebase.firestore.GeoPoint;

public class AddTripViewModel extends AndroidViewModel {
    private final TripRepository tripRepository;
    private final MutableLiveData<AddTripState> addTripState = new MutableLiveData<>();
    private final MutableLiveData<GeoPoint> selectedTripLocation = new MutableLiveData<>();

    public LiveData<AddTripState> getTripState() {
        return addTripState;
    }

    public AddTripViewModel(@NonNull Application application) {
        super(application);
        tripRepository = TripRepository.getInstance(application);
    }

    public void addTrip(Trip trip, Uri imageUri, AddTripState.AddTripListener listener) {
        if (validateTrip(trip, imageUri))
            tripRepository.addTrip(trip, imageUri, listener);
        else
            listener.callback(false);
    }

    public void selectLocation(GeoPoint point) {
        selectedTripLocation.setValue(point);
        addTripState.setValue(new AddTripState(null, null, null, null));
    }

    public LiveData<GeoPoint> getSelectedLocation() {
        return selectedTripLocation;
    }

    public boolean validateTrip(Trip trip, Uri imageUri) {
        if (trip.getTitle().length() < 3)
            addTripState.setValue(new AddTripState(R.string.invalid_trip_title, null, null, null));
        else if (trip.getLocationLat() == 0) {
            addTripState.setValue(new AddTripState(null, R.string.invalid_trip_location, null, null));
        } else if (trip.getAbout().length() < 3)
            addTripState.setValue(new AddTripState(null, null, R.string.about, null));
        else if (imageUri == null)
            addTripState.setValue(new AddTripState(null, null, null, R.string.invalid_trip_image));
        else {
            addTripState.setValue(new AddTripState(null, null, null, null));
            return true;
        }

        return false;
    }
}

