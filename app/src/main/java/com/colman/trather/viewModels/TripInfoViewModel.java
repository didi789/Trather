package com.colman.trather.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.colman.trather.Consts;
import com.colman.trather.models.Review;
import com.colman.trather.models.Trip;
import com.colman.trather.models.User;
import com.colman.trather.repositories.ReviewRepository;
import com.colman.trather.repositories.SettingsRepository;
import com.colman.trather.repositories.TripRepository;
import com.colman.trather.repositories.UserRepository;
import com.colman.trather.services.SharedPref;

import java.util.List;
import java.util.UUID;

public class TripInfoViewModel extends AndroidViewModel {
    private final LiveData<List<Review>> reviewsLiveData;
    private final LiveData<List<Trip>> tripLiveData;
    private final TripRepository tripRepository;
    private final ReviewRepository reviewRepository;
    private final SettingsRepository settingsRepository;
    private final UserRepository userRepository;
    public TripInfoViewModel(@NonNull Application application) {
        super(application);
        tripRepository = TripRepository.getInstance(application);
        reviewRepository = ReviewRepository.getInstance(application);
        userRepository = UserRepository.getInstance(application);
        settingsRepository = new SettingsRepository();
        reviewsLiveData = reviewRepository.getReviewsLiveData();
        tripLiveData = tripRepository.getTrips();
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

