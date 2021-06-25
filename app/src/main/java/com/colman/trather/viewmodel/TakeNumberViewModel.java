package com.colman.trather.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.colman.trather.models.Business;
import com.colman.trather.models.User;
import com.colman.trather.repositories.BusinessRepository;
import com.colman.trather.repositories.UserRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TakeNumberViewModel extends AndroidViewModel {

    private final LiveData<List<Business>> businessLiveData;
    private final LiveData<List<User>> usersLiveData;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    public TakeNumberViewModel(@NonNull Application application) {
        super(application);
        businessRepository = new BusinessRepository(application);
        this.userRepository = new UserRepository(application);
        businessLiveData = businessRepository.getBusinesses();
        usersLiveData = userRepository.getAllUsers();
    }

    public LiveData<User> getUserByEmailLiveData(String email) {
        return Transformations.switchMap(usersLiveData, id ->
                userRepository.getUserByEmail(email));
    }

    public LiveData<Business> getBusinessByIdLiveData(int businessId) {

        return Transformations.switchMap(businessLiveData, id ->
                businessRepository.getBusinessById(businessId));
    }

    public LiveData<List<Business>> getBusinessesLiveData() {
        return businessLiveData;
    }

    public void updateQueueDate(Business businessInfo, String date) {
        businessRepository.updateQueueDate(businessInfo, date);
    }

    public void updateQueue(ArrayList<String> queue, Business businessInfo) {
        businessRepository.updateQueue(queue, businessInfo);
    }

    //if up to date- return true, else- return false and reset queue.
    public Boolean checkBusinessQueueDateAndResetIfNeeded(Business businessInfo) {
        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
        Date todayDate = new Date();
        String deviceDate = currentDate.format(todayDate);

        String[] parts = deviceDate.split("/");
        String deviceDay = parts[0];
        String deviceMonth = parts[1];
        String deviceYear = parts[2];

        String serverFullDate = businessInfo.getQueueDate();
        if (serverFullDate == null || serverFullDate.equals("")) {
            ArrayList<String> queue = new ArrayList<>();
            updateQueue(queue, businessInfo);
            updateQueueDate(businessInfo, deviceDate);
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
            updateQueue(queue, businessInfo);
            updateQueueDate(businessInfo, deviceDate);
            return false;
        }
    }

    public void refreshBusinessLiveData() {
        businessRepository.loadBusinesses();
    }

    public void listenForQueueChanges(Business businessInfo) {
        businessRepository.listenToQueueChanges(businessInfo);
    }
}
