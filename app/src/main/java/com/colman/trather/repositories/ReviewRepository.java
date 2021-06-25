package com.colman.trather.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.colman.trather.BusinessDatabase;
import com.colman.trather.Consts;
import com.colman.trather.dao.ReviewDao;
import com.colman.trather.models.Business;
import com.colman.trather.models.Review;
import com.colman.trather.services.SharedPref;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
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

    public ReviewRepository(Application application) {
        BusinessDatabase database = BusinessDatabase.getDatabase(application);
        reviewDao = database.reviewDao();
        allReviews = reviewDao.getAllReviews();
    }

    public LiveData<List<Review>> getReviewsLiveData() {
        return allReviews;
    }

    public LiveData<List<Review>> getReviewsById(int businessId) {
        return reviewDao.getReviewsByBusinessId(businessId);
    }

    public void deleteReview(Business business, Review review) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            final String currentUser = SharedPref.getString(Consts.CURRENT_USER_KEY, "");
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            String businessName = business.getName().toLowerCase();
            DocumentReference document = db.collection(Consts.BUSINESS_COLLECTION).document(businessName);
            Task<DocumentSnapshot> documentSnapshotTask = document.get();
            documentSnapshotTask.addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots != null) {
                    ArrayList reviewsList = (ArrayList) queryDocumentSnapshots.get(Consts.KEY_REVIEWS);
                    if (!CollectionUtils.isEmpty(reviewsList)) {
                        Map<String, Object> updates = new HashMap<>();
                        for (int i = 0; i < reviewsList.size(); i++) {
                            final Map<String, Object> reviews = (Map<String, Object>) reviewsList.get(i);
                            if (reviews != null) {
                                String author = (String) reviews.get(Consts.KEY_AUTHOR);
                                String comment = (String) reviews.get(Consts.KEY_COMMENT);
                                if (currentUser.equals(author) && review.getComment().equals(comment)) {
                                    updates.put(Consts.KEY_REVIEWS, FieldValue.arrayRemove(reviewsList.get(i)));
                                    document.update(updates);
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        });

        BusinessDatabase.databaseWriteExecutor.execute(() -> reviewDao.deleteReview(review));
    }

    public void addReview(Business business, Review review) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            String businessName = business.getName();//.toLowerCase();
            DocumentReference document = db.collection(Consts.BUSINESS_COLLECTION).document(businessName);
            Task<DocumentSnapshot> documentSnapshotTask = document.get();
            documentSnapshotTask.addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots != null) {
                    Map<String, Object> reviews = new HashMap<>();
                    ArrayList reviewsList = (ArrayList) queryDocumentSnapshots.get(Consts.KEY_REVIEWS);
                    if (CollectionUtils.isEmpty(reviewsList)) {
                        reviewsList = new ArrayList();
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put(Consts.KEY_AUTHOR, review.getAuthor());
                    updates.put(Consts.KEY_COMMENT, review.getComment());
                    updates.put(Consts.KEY_STARS, review.getStars());
                    reviewsList.add(updates);

                    reviews.put(Consts.KEY_REVIEWS, reviewsList);

                    document.set(reviews, SetOptions.merge());
                }
            });
        });

        BusinessDatabase.databaseWriteExecutor.execute(() -> {
            reviewDao.insertReview(review);
        });
    }

    public void updateAllMyProfileImage(String imageUrl, String authorUid) {
        BusinessDatabase.databaseWriteExecutor.execute(() -> {
            reviewDao.updateAllMyProfileImage(imageUrl, authorUid);
        });
    }
}

