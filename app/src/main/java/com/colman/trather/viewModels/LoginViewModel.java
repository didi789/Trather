package com.colman.trather.viewModels;

import android.app.Application;
import android.content.Intent;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.colman.trather.repositories.LoginRepository;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends AndroidViewModel {

    private final LoginRepository loginRepository;
    private MutableLiveData<FirebaseUser> userMutableLiveData = new MutableLiveData<>();

    public LoginViewModel(Application application) {
        super(application);
        this.loginRepository = LoginRepository.getInstance();
        userMutableLiveData = loginRepository.getUser();
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return userMutableLiveData;
    }

    public Intent login() {
        return loginRepository.login();
    }

    public boolean isLoggedIn() {
        return loginRepository.isLoggedIn();
    }
}