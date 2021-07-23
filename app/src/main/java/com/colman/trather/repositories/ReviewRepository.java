package com.colman.trather.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.colman.trather.Consts;
import com.colman.trather.TripDatabase;
import com.colman.trather.dao.ReviewDao;
import com.colman.trather.models.Review;
import com.colman.trather.services.SharedPref;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewRepository {
    private final ReviewDao reviewDao;
    private final LiveData<List<Review>> allReviews;

    private static ReviewRepository mInstance;

    public static ReviewRepository getInstance(Application application) {
        if (mInstance == null) {
            mInstance = new ReviewRepository(application);
        }

        return mInstance;
    }

    private ReviewRepository(Application application) {
        TripDatabase database = TripDatabase.getDatabase(application);
        reviewDao = database.reviewDao();
        allReviews = reviewDao.getAllReviews();
    }

    public LiveData<List<Review>> getReviewsLiveData() {
        return allReviews;
    }

    public LiveData<List<Review>> getReviewsById(String tripId) {
        return reviewDao.getReviewsByTripId(tripId);
    }

    public void deleteReview(Review review) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            final String currentUser = SharedPref.getString(Consts.CURRENT_USER_KEY, "");
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference document = db.collection(Consts.TRIP_COLLECTION).document(review.getTripId());
            Task<DocumentSnapshot> documentSnapshotTask = document.get();
            documentSnapshotTask.addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots != null) {
                    ArrayList reviewsList = (ArrayList) queryDocumentSnapshots.get(Consts.KEY_REVIEWS);
                    if (!CollectionUtils.isEmpty(reviewsList)) {
                        Map<String, Object> updates = new HashMap<>();
                        for (int i = 0; i < reviewsList.size(); i++) {
                            final Map<String, Object> r = (Map<String, Object>) reviewsList.get(i);
                            if (r != null) {
                                String author = (String) r.get(Consts.KEY_AUTHOR_UID);
                                String comment = (String) r.get(Consts.KEY_COMMENT);

                                if (currentUser.equals(author) && review.getComment().equals(comment)) {
                                    r.put(Consts.KEY_IS_DELETED, true);
                                    updates.put(Consts.KEY_REVIEWS, reviewsList);
                                    document.update(updates);
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        });

        TripDatabase.databaseWriteExecutor.execute(() -> reviewDao.deleteReview(review));
    }

    public void addReview(Review review) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference document = db.collection(Consts.TRIP_COLLECTION).document(review.tripId);
            Task<DocumentSnapshot> documentSnapshotTask = document.get();
            documentSnapshotTask.addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots != null) {
                    Map<String, Object> reviews = new HashMap<>();
                    ArrayList reviewsList = (ArrayList) queryDocumentSnapshots.get(Consts.KEY_REVIEWS);
                    if (CollectionUtils.isEmpty(reviewsList)) {
                        reviewsList = new ArrayList();
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put(Consts.KEY_REVIEW_ID, review.getReviewId());
                    updates.put(Consts.KEY_AUTHOR_UID, review.getAuthorUid());
                    updates.put(Consts.KEY_COMMENT, review.getComment());
                    updates.put(Consts.KEY_STARS, review.getStars());
                    reviewsList.add(updates);

                    reviews.put(Consts.KEY_REVIEWS, reviewsList);

                    document.set(reviews, SetOptions.merge());
                }
            });
        });

        TripDatabase.databaseWriteExecutor.execute(() -> reviewDao.insertReview(review));
    }

    public void updateAllMyProfileImage(String imageUrl, String authorName, String authorUid) {
        TripDatabase.databaseWriteExecutor.execute(() -> reviewDao.updateAllMyProfileImage(imageUrl, authorName, authorUid));
    }
}

