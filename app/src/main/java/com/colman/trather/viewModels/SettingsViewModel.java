package com.colman.trather.viewModels;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.colman.trather.models.ModelFirebase;
import com.colman.trather.models.User;
import com.colman.trather.repositories.LoginRepo;
import com.colman.trather.repositories.ReviewsRepo;
import com.colman.trather.repositories.SettingsRepo;
import com.colman.trather.repositories.UsersRepo;


public class SettingsViewModel extends AndroidViewModel {
    private final LiveData<User> userMutableLiveData;
    private final SettingsRepo settingsRepository;
    private final ReviewsRepo reviewsRepository;
    private final UsersRepo usersRepository;
    private final LoginRepo loginRepository;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        settingsRepository = new SettingsRepo();
        userMutableLiveData = settingsRepository.getUser();
        settingsRepository.loadUser();
        reviewsRepository = ReviewsRepo.getInstance(application);
        usersRepository = UsersRepo.getInstance(application);
        loginRepository = LoginRepo.getInstance();
    }

    public LiveData<User> getUserMutableLiveData() {
        return userMutableLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return settingsRepository.isLoading();
    }

    public void saveClicked(User user, String fullName, String bio, ModelFirebase.OnCompleteListener<Boolean> listener) {
        if (!fullName.equals(user.getFullname()) || !bio.equals(user.getBio())) {
            usersRepository.updateProfileData(user.getUid(), fullName, bio);
            settingsRepository.updateProfileData(fullName, bio, listener);
        } else
            listener.onComplete(false);
    }

    public void updateProfileImage(User user, Uri uri, ModelFirebase.OnCompleteListener<Boolean> listener) {
        settingsRepository.updateProfileImage(uri, listener);
        reviewsRepository.updateAllMyProfileImage(user.getImageUrl(), user.getFullname(), user.getUid());
        usersRepository.updateAllMyProfileImage(user.getImageUrl(), user.getUid());
    }

    public void logout() {
        loginRepository.logout();
    }
}

