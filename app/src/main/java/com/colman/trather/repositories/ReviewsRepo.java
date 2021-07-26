package com.colman.trather.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.colman.trather.Consts;
import com.colman.trather.TripDatabase;
import com.colman.trather.dao.ReviewDao;
import com.colman.trather.models.ModelFirebase;
import com.colman.trather.models.Review;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
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

    public void loadReviewByTripId(String tripId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference tripReviews = db.collection(Consts.TRIP_COLLECTION).document(tripId).collection(Consts.KEY_REVIEWS);
        Task<QuerySnapshot> querySnapshotTask = tripReviews.get();
        querySnapshotTask.addOnSuccessListener(queryDocumentSnapshots -> {
            ArrayList<Review> reviews = new ArrayList<>();
            if (queryDocumentSnapshots != null) {
                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot doc : documents) {
                    String commentAuthorUid = doc.getString(Consts.KEY_AUTHOR_UID);
                    String comment = doc.getString(Consts.KEY_COMMENT);
                    final boolean isDeleted = doc.contains(Consts.KEY_IS_DELETED) && doc.getBoolean(Consts.KEY_IS_DELETED);

                    float stars;
                    try {
                        stars = doc.getDouble(Consts.KEY_STARS).floatValue();
                    } catch (Exception e) {
                        stars = doc.getLong(Consts.KEY_STARS).floatValue();
                    }

                    final Review r = new Review(tripId, doc.getId(), commentAuthorUid, comment, stars, isDeleted);
                    reviews.add(r);
                }
            }

            TripDatabase.databaseWriteExecutor.execute(() -> {
                reviews.stream().filter(Review::isDeleted).forEach(reviewDao::deleteReview);
                reviewDao.insertAll(reviews.stream().filter(r -> !r.isDeleted()).collect(Collectors.toList()));
            });
        });
    }
}

