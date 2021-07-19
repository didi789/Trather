package com.colman.trather.repositories;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;

import com.colman.trather.Consts;
import com.colman.trather.TripDatabase;
import com.colman.trather.dao.ReviewDao;
import com.colman.trather.dao.TripDao;
import com.colman.trather.models.AddTripState;
import com.colman.trather.models.Review;
import com.colman.trather.models.Trip;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
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

public class TripRepository {
    private final TripDao tripDao;
    private final ReviewDao reviewDao;
    private final LiveData<List<Trip>> allTrips;

    private static TripRepository mInstance;

    public static TripRepository getInstance(Application application) {
        if (mInstance == null) {
            mInstance = new TripRepository(application);
        }

        return mInstance;
    }

    private TripRepository(Application application) {
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
                    final String siteUrl = doc.getString(Consts.KEY_SITE_URL);
                    final String authorUid = doc.getString(Consts.KEY_AUTHOR_UID);
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
                                String commentAuthorUid = (String) reviews.get(Consts.KEY_AUTHOR_UID);
                                String comment = (String) reviews.get(Consts.KEY_COMMENT);
                                String reviewId = (String) reviews.get(Consts.KEY_REVIEW_ID);
                                float stars = ((Double) reviews.get(Consts.KEY_STARS)).floatValue();
                                tripStars += stars;
                                final Review review = new Review(doc.getId(), reviewId, commentAuthorUid, comment, stars);
                                reviewList.add(review);
                            }
                        }

                        tripRating = tripStars / reviewsList.size();
                    }

                    final Trip trip = new Trip(doc.getId(), address.getLatitude(), address.getLongitude(), name, siteUrl, about, authorUid, imgUrl, tripRating, level, water);

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

    public void reloadTrips() {
        this.loadTrips();
    }

    public LiveData<Trip> getTripById(String tripId) {
        return tripDao.getTripById(tripId);
    }

    public void deleteTrip(Trip trip, AddTripState.AddTripListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Consts.TRIP_COLLECTION).document(trip.getTripId()).delete();
        TripDatabase.databaseWriteExecutor.execute(() -> {
            tripDao.delete(trip);
            listener.callback(true);
        });
    }

    public void addTrip(Trip trip, Uri imageUri, AddTripState.AddTripListener listener) {
        UploadTask uploadTask = getImageUploadTask(imageUri);
        uploadTask.addOnFailureListener(e -> listener.callback(false)
        ).addOnSuccessListener(taskSnapshot -> {
            final StorageMetadata metadata = taskSnapshot.getMetadata();
            assert metadata != null;
            final Task<Uri> downloadUri = Objects.requireNonNull(metadata.getReference()).getDownloadUrl();
            downloadUri.addOnCompleteListener(task -> {
                if (task.isSuccessful() && !TextUtils.isEmpty(downloadUri.getResult().toString())) {
                    trip.setImageUrl(downloadUri.getResult().toString());
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    Map<String, Object> newTrip = new HashMap<>();

                    newTrip.put(Consts.KEY_TITLE, trip.getTitle());
                    newTrip.put(Consts.KEY_SITE_URL, trip.getTripSiteUrl());
                    newTrip.put(Consts.KEY_AUTHOR_UID, trip.getAuthorUid());
                    newTrip.put(Consts.KEY_ABOUT, trip.getAbout());
                    newTrip.put(Consts.KEY_IMG_URL, trip.getImgUrl());
                    newTrip.put(Consts.KEY_LEVEL, trip.getLevel());
                    newTrip.put(Consts.KEY_WATER, trip.isWater());
                    newTrip.put(Consts.KEY_ADDRESS, new GeoPoint(trip.getLocationLat(), trip.getLocationLon()));


                    db.collection(Consts.TRIP_COLLECTION).add(newTrip).addOnCompleteListener(t -> {
                        if (t.isSuccessful()) {
                            trip.setTripId(t.getResult().getId());
                            TripDatabase.databaseWriteExecutor.execute(() -> {
                                tripDao.insertTrip(trip);
                                listener.callback(true);
                            });
                        } else listener.callback(false);
                    });
                } else listener.callback(false);
            }).addOnFailureListener(e -> listener.callback(false));
        });
    }

    public void editTrip(Trip trip, Uri imageUri, boolean isImgEdited, AddTripState.AddTripListener listener) {
        if (isImgEdited) {
            UploadTask uploadTask = getImageUploadTask(imageUri);
            uploadTask.addOnFailureListener(e -> listener.callback(false)
            ).addOnSuccessListener(taskSnapshot -> {
                final StorageMetadata metadata = taskSnapshot.getMetadata();
                assert metadata != null;
                final Task<Uri> downloadUri = Objects.requireNonNull(metadata.getReference()).getDownloadUrl();
                downloadUri.addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !TextUtils.isEmpty(downloadUri.getResult().toString())) {
                        trip.setImageUrl(downloadUri.getResult().toString());
                        updateEditedTrip(trip, listener);
                    } else listener.callback(false);
                }).addOnFailureListener(e -> listener.callback(false));
            });
        } else {
            trip.setImageUrl(imageUri.toString());
            updateEditedTrip(trip, listener);
        }
    }

    private UploadTask getImageUploadTask(Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference riversRef = storageRef.child(Consts.TRIP_COLLECTION + "/" + imageUri.getLastPathSegment());
        return riversRef.putFile(imageUri);
    }

    private void updateEditedTrip(Trip trip, AddTripState.AddTripListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> editedTrip = new HashMap<>();

        editedTrip.put(Consts.KEY_TITLE, trip.getTitle());
        editedTrip.put(Consts.KEY_SITE_URL, trip.getTripSiteUrl());
        editedTrip.put(Consts.KEY_ABOUT, trip.getAbout());
        editedTrip.put(Consts.KEY_IMG_URL, trip.getImgUrl());
        editedTrip.put(Consts.KEY_LEVEL, trip.getLevel());
        editedTrip.put(Consts.KEY_WATER, trip.isWater());
        editedTrip.put(Consts.KEY_ADDRESS, new GeoPoint(trip.getLocationLat(), trip.getLocationLon()));


        db.collection(Consts.TRIP_COLLECTION).document(trip.getTripId()).update(editedTrip).addOnCompleteListener(t -> {
            if (t.isSuccessful()) {
                TripDatabase.databaseWriteExecutor.execute(() -> {
                    tripDao.insertTrip(trip);
                    listener.callback(true);
                });
            } else listener.callback(false);
        });
    }
}

