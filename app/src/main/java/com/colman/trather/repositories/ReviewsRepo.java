package com.colman.trather.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.colman.trather.TripDatabase;
import com.colman.trather.dao.ReviewDao;
import com.colman.trather.models.ModelFirebase;
import com.colman.trather.models.Review;

import java.util.List;
import java.util.stream.Collectors;

public class ReviewsRepo {
    private final ReviewDao reviewDao;
    private static ReviewsRepo mInstance;

    public static ReviewsRepo getInstance(Application application) {
        if (mInstance == null) {
            mInstance = new ReviewsRepo(application);
        }

        return mInstance;
    }

    private ReviewsRepo(Application application) {
        TripDatabase database = TripDatabase.getDatabase(application);
        reviewDao = database.reviewDao();
    }

    public LiveData<List<Review>> getReviewsByTripId(String tripId) {
        return reviewDao.getReviewsByTripId(tripId);
    }

    public void deleteReview(Review review) {
        ModelFirebase.deleteReview(review);
        TripDatabase.databaseWriteExecutor.execute(() -> reviewDao.deleteReview(review));
    }

    public void addReview(Review review) {
        ModelFirebase.addReview(review);
        TripDatabase.databaseWriteExecutor.execute(() -> reviewDao.insertReview(review));
    }

    public void updateAllMyProfileImage(String imageUrl, String authorName, String authorUid) {
        TripDatabase.databaseWriteExecutor.execute(() -> reviewDao.updateAllMyProfileImage(imageUrl, authorName, authorUid));
    }

    public void loadReviewsByTripId(String tripId) {
        ModelFirebase.loadReviewsByTripId(tripId, reviews -> TripDatabase.databaseWriteExecutor.execute(() -> {
            reviews.stream().filter(Review::isDeleted).forEach(reviewDao::deleteReview);
            reviewDao.insertAll(reviews.stream().filter(r -> !r.isDeleted()).collect(Collectors.toList()));
        }));
    }
}

