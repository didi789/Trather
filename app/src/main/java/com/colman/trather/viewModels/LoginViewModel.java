package com.colman.trather.viewModels;

import android.app.Application;
import android.content.Intent;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.colman.trather.repositories.LoginRepo;

public class LoginViewModel extends AndroidViewModel {

    private final LoginRepo loginRepository;

    public LoginViewModel(Application application) {
        super(application);
        this.loginRepository = LoginRepo.getInstance();
    }

    public Intent login() {
        return loginRepository.login();
    }

    public MutableLiveData<Boolean> isLoggedIn() {
        return loginRepository.isLoggedIn();
    }
}