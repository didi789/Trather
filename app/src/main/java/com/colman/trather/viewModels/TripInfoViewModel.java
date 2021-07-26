package com.colman.trather.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.colman.trather.Consts;
import com.colman.trather.models.Review;
import com.colman.trather.models.Trip;
import com.colman.trather.models.User;
import com.colman.trather.repositories.ReviewsRepo;
import com.colman.trather.repositories.SettingsRepo;
import com.colman.trather.repositories.TripRepo;
import com.colman.trather.repositories.UsersRepo;
import com.colman.trather.services.SharedPref;

import java.util.List;
import java.util.UUID;

public class TripInfoViewModel extends AndroidViewModel {
    private final TripRepo tripRepository;
    private final ReviewsRepo reviewsRepository;
    private final SettingsRepo settingsRepository;
    private final UsersRepo usersRepository;

    public TripInfoViewModel(@NonNull Application application) {
        super(application);
        tripRepository = TripRepo.getInstance(application);
        reviewsRepository = ReviewsRepo.getInstance(application);
        usersRepository = UsersRepo.getInstance(application);
        settingsRepository = new SettingsRepo();
    }

    public LiveData<Trip> getTripByIdLiveData(String tripId) {
        return tripRepository.getTripById(tripId);
    }

    public LiveData<List<Review>> getReviewsByTripIdLiveData(String tripId) {
        reviewsRepository.loadReviewByTripId(tripId);
        return reviewsRepository.getReviewsByTripId(tripId);
    }

    public LiveData<User> getUserByUid(String uid) {
        return usersRepository.getUserByUid(uid);
    }

    public void deleteReview(Review review) {
        reviewsRepository.deleteReview(review);
    }

    public void addReview(Trip trip, String review, float stars) {
        final String currentUserUid = SharedPref.getString(Consts.CURRENT_USER_KEY, "");
        String fullName = "";
        if (settingsRepository.getUser().getValue() != null)
            fullName = settingsRepository.getUser().getValue().getFullname();

        final Review r = new Review(trip.getTripId(), UUID.randomUUID().toString(), fullName, currentUserUid, review, stars);
        reviewsRepository.addReview(r);
    }
}

