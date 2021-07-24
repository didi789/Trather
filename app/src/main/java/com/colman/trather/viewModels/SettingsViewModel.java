package com.colman.trather.viewModels;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.colman.trather.Consts;
import com.colman.trather.models.User;
import com.colman.trather.repositories.LoginRepository;
import com.colman.trather.repositories.ReviewRepository;
import com.colman.trather.repositories.SettingsRepository;
import com.colman.trather.repositories.UserRepository;
import com.colman.trather.services.SharedPref;


public class SettingsViewModel extends AndroidViewModel {
    private final LiveData<User> userMutableLiveData;
    private final LiveData<Boolean> isLoading;
    private final SettingsRepository settingsRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final LoginRepository loginRepository;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        settingsRepository = new SettingsRepository();
        userMutableLiveData = settingsRepository.getUser();
        isLoading = settingsRepository.isLoading();
        settingsRepository.loadUser();
        reviewRepository = ReviewRepository.getInstance(application);
        userRepository = UserRepository.getInstance(application);
        loginRepository = LoginRepository.getInstance();
    }

    public LiveData<User> getUserMutableLiveData() {
        return userMutableLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void saveClicked(LifecycleOwner viewLifecycleOwner, String fullName, String bio, boolean notification) {
        userMutableLiveData.observe(viewLifecycleOwner, user -> {
            if (user == null) {
                return;
            }

            if (!fullName.equals(user.getFullname()) || !bio.equals(user.getBio())) {
                userRepository.updateProfileData(user.getUid(), fullName, bio);
                settingsRepository.updateProfileData(fullName, bio);
            }
        });

        SharedPref.putBoolean(Consts.NOTIFICATION, notification);
    }

    public void updateProfileImage(LifecycleOwner viewLifecycleOwner, Uri uri) {
        settingsRepository.updateProfileImage(uri);

        userMutableLiveData.observe(viewLifecycleOwner, user -> {
            if (user == null) {
                return;
            }
            reviewRepository.updateAllMyProfileImage(user.getImageUrl(), user.getFullname(), user.getUid());
            userRepository.updateAllMyProfileImage(user.getImageUrl(), user.getUid());
        });
    }

    public void logout() {
        loginRepository.logout();
    }
}

