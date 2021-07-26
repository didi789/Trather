package com.colman.trather.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.colman.trather.Consts;
import com.colman.trather.models.Review;
import com.colman.trather.models.Trip;
import com.colman.trather.models.User;
import com.colman.trather.repositories.ReviewRepo;
import com.colman.trather.repositories.SettingsRepo;
import com.colman.trather.repositories.TripRepo;
import com.colman.trather.repositories.UserRepo;
import com.colman.trather.services.SharedPref;

import java.util.List;
import java.util.UUID;

public class TripInfoViewModel extends AndroidViewModel {
    private final TripRepo tripRepository;
    private final ReviewRepo reviewRepository;
    private final SettingsRepo settingsRepository;
    private final UserRepo userRepository;

    public TripInfoViewModel(@NonNull Application application) {
        super(application);
        tripRepository = TripRepo.getInstance(application);
        reviewRepository = ReviewRepo.getInstance(application);
        userRepository = UserRepo.getInstance(application);
        settingsRepository = new SettingsRepo();
    }

    public LiveData<Trip> getTripByIdLiveData(String tripId) {
        return tripRepository.getTripById(tripId);
    }

    public LiveData<List<Review>> getReviewsByTripIdLiveData(String tripId) {
        return reviewRepository.getReviewsById(tripId);
    }

    public LiveData<User> getUserByUid(String uid) {
        return userRepository.getUserByUid(uid);
    }

    public void deleteReview(Review review) {
        reviewRepository.deleteReview(review);
    }

    public void addReview(Trip trip, String review, float stars) {
        final String currentUserUid = SharedPref.getString(Consts.CURRENT_USER_KEY, "");
        String fullName = "";
        if (settingsRepository.getUser().getValue() != null)
            fullName = settingsRepository.getUser().getValue().getFullname();

        final Review r = new Review(trip.getTripId(), UUID.randomUUID().toString(), fullName, currentUserUid, review, stars);
        reviewRepository.addReview(r);
    }
}

