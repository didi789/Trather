/*
package com.colman.trather.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.colman.trather.models.Trip;
import com.colman.trather.models.Trip;import com.colman.trather.models.User;
import com.colman.trather.repositories.TripRepository;
import com.colman.trather.repositories.UserRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TakeNumberViewModel extends AndroidViewModel {

    private final LiveData<List<Trip>> tripLiveData;
    private final LiveData<List<User>> usersLiveData;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;


    public TakeNumberViewModel(@NonNull Application application) {
        super(application);
        tripRepository = new TripRepository(application);
        this.userRepository = new UserRepository(application);
        tripLiveData = tripRepository.getTrips();
        usersLiveData = userRepository.getAllUsers();
    }

    public LiveData<User> getUserByEmailLiveData(String email) {
        return Transformations.switchMap(usersLiveData, id ->
                userRepository.getUserByEmail(email));
    }

    public LiveData<Trip> getTripByIdLiveData(int tripId) {

        return Transformations.switchMap(tripLiveData, id ->
                tripRepository.getTripById(tripId));
    }

    public LiveData<List<Trip>> getTripsLiveData() {
        return tripLiveData;
    }

    public void updateQueue(ArrayList<String> queue, Trip tripInfo) {
        tripRepository.updateQueue(queue, tripInfo);
    }

    //if up to date- return true, else- return false and reset queue.
    public Boolean checkTripQueueDateAndResetIfNeeded(Trip tripInfo) {
        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
        Date todayDate = new Date();
        String deviceDate = currentDate.format(todayDate);

        String[] parts = deviceDate.split("/");
        String deviceDay = parts[0];
        String deviceMonth = parts[1];
        String deviceYear = parts[2];

        String serverFullDate = tripInfo.getQueueDate();
        if (serverFullDate == null || serverFullDate.equals("")) {
            ArrayList<String> queue = new ArrayList<>();
            updateQueue(queue, tripInfo);
            updateQueueDate(tripInfo, deviceDate);
            return false;
        }

        parts = serverFullDate.split("/");

        String serverDay = parts[0];
        String serverMonth = parts[1];
        String serverYear = parts[2];


        if (Integer.parseInt(deviceDay) == Integer.parseInt(serverDay) &&
                Integer.parseInt(deviceMonth) == Integer.parseInt(serverMonth) &&
                Integer.parseInt(deviceYear) == Integer.parseInt(serverYear))
            return true;
        else if (Integer.parseInt(deviceYear) < Integer.parseInt(serverYear))
            return true;
        else if (Integer.parseInt(deviceMonth) < Integer.parseInt(serverMonth))
            return true;
        else if (Integer.parseInt(deviceDay) < Integer.parseInt(serverDay))
            return true;

        else {
            ArrayList<String> queue = new ArrayList<>();
            updateQueue(queue, tripInfo);
            updateQueueDate(tripInfo, deviceDate);
            return false;
        }
    }

    public void refreshTripLiveData() {
        tripRepository.loadTrips();
    }

    public void listenForQueueChanges(Trip tripInfo) {
        tripRepository.listenToQueueChanges(tripInfo);
    }
}
*/
