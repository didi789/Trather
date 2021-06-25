package com.colman.trather.viewModels;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.colman.trather.Consts;
import com.colman.trather.models.User;
import com.colman.trather.repositories.ReviewRepository;
import com.colman.trather.repositories.SettingsRepository;
import com.colman.trather.services.SharedPref;


public class SettingsViewModel extends AndroidViewModel {
    private final LiveData<User> userMutableLiveData;
    private final LiveData<Boolean> isLoading;
    private final SettingsRepository settingsRepository;
    private final ReviewRepository reviewRepository;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        settingsRepository = new SettingsRepository();
        userMutableLiveData = settingsRepository.getUser();
        isLoading = settingsRepository.getIsLoadingSomething();
        settingsRepository.loadUser();
        reviewRepository = new ReviewRepository(application);
    }

    public LiveData<User> getUserMutableLiveData() {
        return userMutableLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void saveClicked(boolean checked, boolean checked1, boolean checked2) {
        SharedPref.putBoolean(Consts.VIBRATION, checked);
        SharedPref.putBoolean(Consts.SOUND, checked1);
        SharedPref.putBoolean(Consts.NOTIFICATION, checked2);
    }

    public void updateProfileImage(LifecycleOwner viewLifecycleOwner, Uri uri) {
        settingsRepository.updateProfileImage(uri);

        userMutableLiveData.observe(viewLifecycleOwner, user -> {
            if (user == null) {
                return;
            }

            final String imageUrl = user.getImageUrl();
            final String me = SharedPref.getString(Consts.CURRENT_USER_KEY, "");
            reviewRepository.updateAllMyProfileImage(imageUrl, me);
        });
    }
}

