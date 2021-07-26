package com.colman.trather.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.colman.trather.models.User;
import com.colman.trather.repositories.UserRepo;


public class UserInfoViewModel extends AndroidViewModel {
    private final UserRepo userRepository;

    public UserInfoViewModel(@NonNull Application application) {
        super(application);
        userRepository = UserRepo.getInstance(application);
    }

    public LiveData<User> getUserByUid(String uid) {
        return userRepository.getUserByUid(uid);
    }
}

