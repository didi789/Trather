package com.colman.trather.viewModels;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.colman.trather.Consts;
import com.colman.trather.models.User;
import com.colman.trather.repositories.LoginRepo;
import com.colman.trather.repositories.ReviewsRepo;
import com.colman.trather.repositories.SettingsRepo;
import com.colman.trather.repositories.UsersRepo;
import com.colman.trather.services.SharedPref;


public class SettingsViewModel extends AndroidViewModel {
    private final LiveData<User> userMutableLiveData;
    private final LiveData<Boolean> isLoading;
    private final SettingsRepo settingsRepository;
    private final ReviewsRepo reviewsRepository;
    private final UsersRepo usersRepository;
    private final LoginRepo loginRepository;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        settingsRepository = new SettingsRepo();
        userMutableLiveData = settingsRepository.getUser();
        isLoading = settingsRepository.isLoading();
        settingsRepository.loadUser();
        reviewsRepository = ReviewsRepo.getInstance(application);
        usersRepository = UsersRepo.getInstance(application);
        loginRepository = LoginRepo.getInstance();
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
                usersRepository.updateProfileData(user.getUid(), fullName, bio);
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
            reviewsRepository.updateAllMyProfileImage(user.getImageUrl(), user.getFullname(), user.getUid());
            usersRepository.updateAllMyProfileImage(user.getImageUrl(), user.getUid());
        });
    }

    public void logout() {
        loginRepository.logout();
    }
}

