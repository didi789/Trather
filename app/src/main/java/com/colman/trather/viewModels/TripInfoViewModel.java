package com.colman.trather.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.colman.trather.Consts;
import com.colman.trather.models.Review;
import com.colman.trather.models.Trip;
import com.colman.trather.repositories.ReviewRepository;
import com.colman.trather.repositories.TripRepository;
import com.colman.trather.services.SharedPref;

import java.util.List;

public class TripInfoViewModel extends AndroidViewModel {
    private final LiveData<List<Review>> reviewsLiveData;
    private final LiveData<List<Trip>> tripLiveData;
    private final TripRepository tripRepository;
    private final ReviewRepository reviewRepository;

    public TripInfoViewModel(@NonNull Application application) {
        super(application);
        tripRepository = new TripRepository(application);
        reviewRepository = new ReviewRepository(application);
        reviewsLiveData = reviewRepository.getReviewsLiveData();
        tripLiveData = tripRepository.getTrips();
    }

    public LiveData<List<Review>> getReviewsLiveData() {
        return reviewsLiveData;
    }

    public LiveData<List<Trip>> getTripsLiveData() {
        return tripLiveData;
    }

    public LiveData<Trip> getTripByIdLiveData(int tripId) {
        return Transformations.switchMap(tripLiveData, id ->
                tripRepository.getTripById(tripId));
    }

    public LiveData<List<Review>> getReviewsByTripIdLiveData(int tripId) {
        return Transformations.switchMap(reviewsLiveData, reviewList ->
                reviewRepository.getReviewsById(tripId)
        );
    }

    public void deleteReview(Review review) {
        tripRepository.getTripById(review.getReviewId()).observeForever(trip -> reviewRepository.deleteReview(trip, review));
    }

    public void addReview(Trip trip, String review, int stars) {
        final String currentUserEmail = SharedPref.getString(Consts.CURRENT_USER_KEY, "");
        final Review review1 = new Review(trip.getTripId(), currentUserEmail, review, "", stars);
        reviewRepository.addReview(trip, review1);
    }
}

