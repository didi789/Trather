package com.colman.trather.repositories;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.colman.trather.BusinessDatabase;
import com.colman.trather.Consts;
import com.colman.trather.dao.BusinessDao;
import com.colman.trather.dao.ReviewDao;
import com.colman.trather.models.Business;
import com.colman.trather.models.Review;
import com.colman.trather.services.Utils;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BusinessRepository {
    private final BusinessDao businessDao;
    private final ReviewDao reviewDao;
    private final LiveData<List<Business>> allBusinesses;

    public BusinessRepository(Application application) {
        BusinessDatabase database = BusinessDatabase.getDatabase(application);
        businessDao = database.businessDao();
        reviewDao = database.reviewDao();

        allBusinesses = businessDao.getAll();
        loadBusinesses();
    }

    public void loadBusinesses() {
        final List<Business> businessList = new ArrayList<>();
        final List<Review> reviewList = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference businessesColl = db.collection(Consts.KEY_BUSINESSES);
        Task<QuerySnapshot> querySnapshotTask = businessesColl.get();
        querySnapshotTask.addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null) {
                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot doc : documents) {
                    final String name = doc.get(Consts.KEY_NAME, String.class);
                    final String about = doc.get(Consts.KEY_ABOUT, String.class);
                    final String imgUrl = doc.get(Consts.KEY_IMG_URL, String.class);
                    final GeoPoint address = new GeoPoint(32.4, 32.5);//doc.get(Consts.KEY_ADDRESS, GeoPoint.class);
                    final ArrayList<String> queue = (ArrayList<String>) doc.get(Consts.KEY_QUEUE);
                    final String queueDate = doc.get(Consts.KEY_QUEUEDATE, String.class);

                    final Business business = new Business(name, about, imgUrl, address.getLatitude(), address.getLongitude(), queue, queueDate);
                    businessList.add(business);
                    ArrayList reviewsList = (ArrayList) doc.get(Consts.KEY_REVIEWS);
                    if (!CollectionUtils.isEmpty(reviewsList)) {
                        for (int i = 0; i < reviewsList.size(); i++) {
                            final Map<String, Object> reviews = (Map<String, Object>) reviewsList.get(i);
                            if (reviews != null) {
                                String author = (String) reviews.get(Consts.KEY_AUTHOR);
                                String comment = (String) reviews.get(Consts.KEY_COMMENT);
                                long stars = (long) reviews.get(Consts.KEY_STARS);

                                final Review review = new Review(business.getBusinessId(), author, comment, null, stars);
                                reviewList.add(review);
                            }
                        }
                    }
                }

                insertToDB(businessList, reviewList);
            }
        });
    }

    private void insertToDB(List<Business> businessList, List<Review> reviewList) {
        BusinessDatabase.databaseWriteExecutor.execute(() -> {
            businessDao.insertAll(businessList);
            reviewDao.insertAll(reviewList);
        });
    }

    public LiveData<List<Business>> getBusinesses() {
        return allBusinesses;
    }

    public LiveData<Business> getBusinessById(int businessId) {
        return businessDao.getBusinessById(businessId);
    }

    public void deleteBusiness(Business business) {
        BusinessDatabase.databaseWriteExecutor.execute(() -> {
            businessDao.delete(business);
        });
    }

    public void updateQueueDate(Business businessInfo, String date) {
        BusinessDatabase.databaseWriteExecutor.execute(() -> {
            businessDao.updateQueueDate(date, businessInfo.getBusinessId());
        });

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection(Consts.KEY_BUSINESSES).document(businessInfo.getName());
            docRef.update("queueDate", date)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("success", "DocumentSnapshot successfully updated!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("failed", "Error updating document", e);
                        }
                    });

        });
    }

    public void updateQueue(ArrayList<String> queue, Business businessInfo) {
        BusinessDatabase.databaseWriteExecutor.execute(() -> {
            businessDao.updateQueue(Utils.fromArrayList(queue), businessInfo.getBusinessId());
        });

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection(Consts.KEY_BUSINESSES).document(businessInfo.getName());
            docRef.update("queue", queue)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("success", "DocumentSnapshot successfully updated!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("failed", "Error updating document", e);
                        }
                    });

        });

    }

    public void listenToQueueChanges(Business businessInfo) {


        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() ->
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection(Consts.KEY_BUSINESSES).document(businessInfo.getName());
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    loadBusinesses();
                }
            });


        });
    }

}

