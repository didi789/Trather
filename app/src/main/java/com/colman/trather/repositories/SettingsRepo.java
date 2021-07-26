package com.colman.trather.repositories;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.colman.trather.models.ModelFirebase;
import com.colman.trather.models.User;

import java.util.Objects;

public class SettingsRepo {

    private final MutableLiveData<User> user = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public SettingsRepo() {
    }

    public void loadUser() {
        isLoading.setValue(true);
        ModelFirebase.loadUser(user -> {
            this.user.setValue(user);
            isLoading.setValue(false);
        });
    }

    public LiveData<User> getUser() {
        return user;
    }

    public MutableLiveData<Boolean> isLoading() {
        return isLoading;
    }

    public void updateProfileImage(Uri uri) {
        isLoading.setValue(true);
        ModelFirebase.updateProfileImage(uri, profileImageNewUrl -> {
            if (profileImageNewUrl != null) {
                User newUser = user.getValue();
                Objects.requireNonNull(newUser).setImageUrl(profileImageNewUrl);
                user.setValue(newUser);
            }
            isLoading.setValue(false);
        });
    }

    public void updateProfileData(String fullName, String bio) {
        ModelFirebase.updateProfileData(fullName, bio, result -> {
            if (result) {
                User newUser = user.getValue();
                Objects.requireNonNull(newUser).setFullname(fullName);
                Objects.requireNonNull(newUser).setBio(bio);
                user.setValue(newUser);
            }

            isLoading.setValue(false);
        });
    }
}

