package com.colman.trather.repositories;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.colman.trather.Consts;
import com.colman.trather.TripDatabase;
import com.colman.trather.dao.ReviewDao;
import com.colman.trather.dao.TripDao;
import com.colman.trather.models.Review;
import com.colman.trather.models.Trip;
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

public class TripRepository {
    private final TripDao tripDao;
    private final ReviewDao reviewDao;
    private final LiveData<List<Trip>> allTrips;

    public TripRepository(Application application) {
        TripDatabase database = TripDatabase.getDatabase(application);
        tripDao = database.tripDao();
        reviewDao = database.reviewDao();

        allTrips = tripDao.getAll();
        loadTrips();
    }

    public void loadTrips() {
        final List<Trip> tripList = new ArrayList<>();
        final List<Review> reviewList = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference tripsColl = db.collection(Consts.KEY_TRIPS);
        Task<QuerySnapshot> querySnapshotTask = tripsColl.get();
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

                    final Trip trip = new Trip(name, about, imgUrl, address.getLatitude(), address.getLongitude(), queue, queueDate);
                    tripList.add(trip);
                    ArrayList reviewsList = (ArrayList) doc.get(Consts.KEY_REVIEWS);
                    if (!CollectionUtils.isEmpty(reviewsList)) {
                        for (int i = 0; i < reviewsList.size(); i++) {
                            final Map<String, Object> reviews = (Map<String, Object>) reviewsList.get(i);
                            if (reviews != null) {
                                String authorUid = (String) reviews.get(Consts.KEY_AUTHOR_UID);
                                String comment = (String) reviews.get(Consts.KEY_COMMENT);
                                long stars = (long) reviews.get(Consts.KEY_STARS);

                                final Review review = new Review(trip.getTripId(), authorUid, comment, stars);
                                reviewList.add(review);
                            }
                        }
                    }
                }

                insertToDB(tripList, reviewList);
            }
        });
    }

    private void insertToDB(List<Trip> tripList, List<Review> reviewList) {
        TripDatabase.databaseWriteExecutor.execute(() -> {
            tripDao.insertAll(tripList);
            reviewDao.insertAll(reviewList);
        });
    }

    public LiveData<List<Trip>> getTrips() {
        return allTrips;
    }

    public LiveData<Trip> getTripById(int tripId) {
        return tripDao.getTripById(tripId);
    }

    public void deleteTrip(Trip trip) {
        TripDatabase.databaseWriteExecutor.execute(() -> {
            tripDao.delete(trip);
        });
    }

    public void updateQueueDate(Trip tripInfo, String date) {
        TripDatabase.databaseWriteExecutor.execute(() -> {
            tripDao.updateQueueDate(date, tripInfo.getTripId());
        });

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection(Consts.KEY_TRIPS).document(tripInfo.getName());
            docRef.update("queueDate", date)
                    .addOnSuccessListener(aVoid -> Log.d("success", "DocumentSnapshot successfully updated!"))
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("failed", "Error updating document", e);
                        }
                    });

        });
    }

    public void updateQueue(ArrayList<String> queue, Trip tripInfo) {
        TripDatabase.databaseWriteExecutor.execute(() -> {
            tripDao.updateQueue(Utils.fromArrayList(queue), tripInfo.getTripId());
        });

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection(Consts.KEY_TRIPS).document(tripInfo.getName());
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

    public void listenToQueueChanges(Trip tripInfo) {


        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() ->
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection(Consts.KEY_TRIPS).document(tripInfo.getName());
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    loadTrips();
                }
            });


        });
    }

}

