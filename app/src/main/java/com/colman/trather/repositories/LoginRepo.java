package com.colman.trather.repositories;

import android.content.Intent;

import androidx.lifecycle.MutableLiveData;

import com.colman.trather.Consts;
import com.colman.trather.models.ModelFirebase;
import com.colman.trather.services.SharedPref;

public class LoginRepo {

    private static LoginRepo mInstance;
    private final MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);

    private LoginRepo() {
    }

    public static LoginRepo getInstance() {
        if (mInstance == null) {
            mInstance = new LoginRepo();
            mInstance.setLoggedInUser(ModelFirebase.getUserUid());
        }

        return mInstance;
    }

    public MutableLiveData<Boolean> isLoggedIn() {
        return isLoggedIn;
    }


    private void setLoggedInUser(String uid) {
        if (uid != null) {
            isLoggedIn.postValue(true);
            SharedPref.putString(Consts.CURRENT_USER_KEY, uid);
        } else {
            isLoggedIn.postValue(false);
            SharedPref.removeKey(Consts.CURRENT_USER_KEY);
        }
    }

    public Intent login() {
        return ModelFirebase.getLoginIntent(this::setLoggedInUser);
    }


    public void logout() {
        SharedPref.removeKey(Consts.CURRENT_USER_KEY);
        ModelFirebase.signOut();
        isLoggedIn.postValue(false);
    }
}
