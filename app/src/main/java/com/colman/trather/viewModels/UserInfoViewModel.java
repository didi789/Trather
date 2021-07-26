package com.colman.trather.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.colman.trather.models.User;
import com.colman.trather.repositories.UsersRepo;


public class UserInfoViewModel extends AndroidViewModel {
    private final UsersRepo usersRepository;

    public UserInfoViewModel(@NonNull Application application) {
        super(application);
        usersRepository = UsersRepo.getInstance(application);
    }

    public LiveData<User> getUserByUid(String uid) {
        return usersRepository.getUserByUid(uid);
    }
}

