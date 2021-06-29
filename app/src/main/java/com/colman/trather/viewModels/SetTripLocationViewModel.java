package com.colman.trather.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.GeoPoint;

public class SetTripLocationViewModel extends AndroidViewModel {
    private final MutableLiveData<GeoPoint> selected = new MutableLiveData<>();

    public SetTripLocationViewModel(@NonNull Application application) {
        super(application);
    }

    public void select(GeoPoint point) {
        selected.setValue(point);
    }

    public LiveData<GeoPoint> getSelected() {
        return selected;
    }
}

