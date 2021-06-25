package com.colman.trather.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.colman.trather.models.Business;
import com.colman.trather.repositories.BusinessRepository;

import java.util.List;

public class MapViewModel extends AndroidViewModel {
    private final LiveData<List<Business>> businessListMutableLiveData;
    private final BusinessRepository businessRepository;

    public MapViewModel(@NonNull Application application) {
        super(application);
        businessRepository = new BusinessRepository(application);
        businessListMutableLiveData = businessRepository.getBusinesses();
    }

    public LiveData<List<Business>> getBusinessesLiveData() {
        return businessListMutableLiveData;
    }

}
