package com.colman.trather.repositories;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;

import com.colman.trather.Consts;
import com.colman.trather.TripDatabase;
import com.colman.trather.dao.ReviewDao;
import com.colman.trather.dao.TripDao;
import com.colman.trather.models.Review;
import com.colman.trather.models.Trip;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
                    final String name = doc.getString(Consts.KEY_TITLE);
                    final String about = doc.getString(Consts.KEY_ABOUT);
                    final String imgUrl = doc.getString(Consts.KEY_IMG_URL);
                    final double level = doc.getDouble(Consts.KEY_LEVEL);
                    final boolean water = doc.getBoolean(Consts.KEY_WATER);
                    final GeoPoint address = doc.getGeoPoint(Consts.KEY_ADDRESS);

                    ArrayList reviewsList = (ArrayList) doc.get(Consts.KEY_REVIEWS);

                    double tripStars = 0;
                    double tripRating = -1;
                    if (!CollectionUtils.isEmpty(reviewsList)) {
                        for (int i = 0; i < reviewsList.size(); i++) {
                            final Map<String, Object> reviews = (Map<String, Object>) reviewsList.get(i);
                            if (reviews != null) {
                                String authorUid = (String) reviews.get(Consts.KEY_AUTHOR_UID);
                                String comment = (String) reviews.get(Consts.KEY_COMMENT);
                                String reviewId = (String) reviews.get(Consts.KEY_REVIEW_ID);
                                long stars = (long) reviews.get(Consts.KEY_STARS);
                                tripStars += stars;
                                final Review review = new Review(doc.getId(), reviewId, authorUid, comment, stars);
                                reviewList.add(review);
                            }
                        }

                        tripRating = tripStars / reviewsList.size();
                    }

                    final Trip trip = new Trip(doc.getId(), address.getLatitude(), address.getLongitude(), name, about, imgUrl, tripRating, level, water);

                    tripList.add(trip);

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

    public LiveData<Trip> getTripById(String tripId) {
        return tripDao.getTripById(tripId);
    }

    public void deleteTrip(Trip trip) {
        TripDatabase.databaseWriteExecutor.execute(() -> {
            tripDao.delete(trip);
        });
    }

/*    public void updateQueueDate(Trip tripInfo, String date) {
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
    }*/

/*
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
*/

    public void listenToQueueChanges(Trip tripInfo) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() ->
        {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection(Consts.KEY_TRIPS).document(tripInfo.getTripId());
            docRef.addSnapshotListener((value, error) -> loadTrips());
        });
    }

    public void addTrip(Trip trip, Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference riversRef = storageRef.child("trips/" + imageUri.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(imageUri);

        uploadTask.addOnFailureListener((OnFailureListener) exception -> {
//            isLoadingSomething.setValue(false);
        }).addOnSuccessListener((OnSuccessListener<UploadTask.TaskSnapshot>) taskSnapshot -> {
            final StorageMetadata metadata = taskSnapshot.getMetadata();
            assert metadata != null;
            final Task<Uri> downloadUri = Objects.requireNonNull(metadata.getReference()).getDownloadUrl();
            downloadUri.addOnCompleteListener(task -> {
                if (task.isSuccessful() && !TextUtils.isEmpty(downloadUri.getResult().toString())) {
                    trip.setImageUrl(downloadUri.getResult().toString());
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    Map<String, Object> newTrip = new HashMap<>();

                    newTrip.put(Consts.KEY_TITLE, trip.getTitle());
                    newTrip.put(Consts.KEY_ABOUT, trip.getAbout());
                    newTrip.put(Consts.KEY_IMG_URL, trip.getImgUrl());
                    newTrip.put(Consts.KEY_LEVEL, trip.getLevel());
                    newTrip.put(Consts.KEY_WATER, trip.isWater());
                    newTrip.put(Consts.KEY_ADDRESS, new GeoPoint(trip.getLocationLat(), trip.getLocationLon()));

                    db.collection(Consts.TRIP_COLLECTION).add(newTrip).addOnCompleteListener(task1 -> {

                    });
                } else {
/*                    isLoadingSomething.setValue(false);
                    Log.e(TAG, "Failed to upload new image");*/
                }
            }).addOnFailureListener(e -> {
/*                isLoadingSomething.setValue(false);
                Log.e(TAG, "Failed to upload new image");*/
            });
        });
    }
}

