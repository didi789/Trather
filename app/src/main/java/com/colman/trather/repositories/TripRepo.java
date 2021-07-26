package com.colman.trather.repositories;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.LiveData;

import com.colman.trather.TripDatabase;
import com.colman.trather.dao.ReviewDao;
import com.colman.trather.dao.TripDao;
import com.colman.trather.models.ModelFirebase;
import com.colman.trather.models.Review;
import com.colman.trather.models.Trip;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TripRepo {
    private final TripDao tripDao;
    private final ReviewDao reviewDao;
    private final LiveData<List<Trip>> allTrips;

    private static TripRepo mInstance;

    public static TripRepo getInstance(Application application) {
        if (mInstance == null) {
            mInstance = new TripRepo(application);
        }

        return mInstance;
    }

    private TripRepo(Application application) {
        TripDatabase database = TripDatabase.getDatabase(application);
        tripDao = database.tripDao();
        reviewDao = database.reviewDao();

        allTrips = tripDao.getAll();
        loadTrips();
    }

    public void loadTrips() {
        ModelFirebase.loadTrips(trips -> {
            TripDatabase.databaseWriteExecutor.execute(() -> {
                trips.stream().filter(Trip::isDeleted).forEach(tripDao::delete);
                tripDao.insertAll(trips.stream().filter(t -> !t.isDeleted()).collect(Collectors.toList()));
            });
        });

        // TODO move reviews to reviewsRepo + in firebase..
        final List<Trip> tripList = new ArrayList<>();
        final List<Review> reviewList = new ArrayList<>();
/*
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference tripsColl = db.collection(Consts.KEY_TRIPS);
        Task<QuerySnapshot> querySnapshotTask = tripsColl.get();
        querySnapshotTask.addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null) {
                TripDatabase.databaseWriteExecutor.execute(() -> {
                    List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot doc : documents) {
                        final String name = doc.getString(Consts.KEY_TITLE);
                        final String siteUrl = doc.getString(Consts.KEY_SITE_URL);
                        final String authorUid = doc.getString(Consts.KEY_AUTHOR_UID);
                        final String about = doc.getString(Consts.KEY_ABOUT);
                        final String imgUrl = doc.getString(Consts.KEY_IMG_URL);
                        final double level = doc.getDouble(Consts.KEY_LEVEL);
                        final boolean water = doc.getBoolean(Consts.KEY_WATER);
                        final boolean isDeleted = doc.contains(Consts.KEY_IS_DELETED) && doc.getBoolean(Consts.KEY_IS_DELETED);
                        final GeoPoint address = doc.getGeoPoint(Consts.KEY_ADDRESS);

                        ArrayList reviewsList = (ArrayList) doc.get(Consts.KEY_REVIEWS);

                        double tripStars = 0;
                        double tripRating = -1;
                        if (!CollectionUtils.isEmpty(reviewsList)) {
                            for (int i = 0; i < reviewsList.size(); i++) {
                                final Map<String, Object> review = (Map<String, Object>) reviewsList.get(i);
                                if (review != null) {
                                    String commentAuthorUid = (String) review.get(Consts.KEY_AUTHOR_UID);
                                    String comment = (String) review.get(Consts.KEY_COMMENT);
                                    String reviewId = (String) review.get(Consts.KEY_REVIEW_ID);
                                    boolean isReviewDeleted = review.containsKey(Consts.KEY_IS_DELETED) && (boolean) review.get(Consts.KEY_IS_DELETED);

                                    float stars;
                                    try {
                                        stars = ((Double) review.get(Consts.KEY_STARS)).floatValue();
                                    } catch (Exception e) {
                                        stars = ((Long) review.get(Consts.KEY_STARS)).floatValue();
                                    }

                                    tripStars += stars;
                                    final Review r = new Review(doc.getId(), reviewId, commentAuthorUid, comment, stars, isReviewDeleted);
                                    if (isReviewDeleted)
                                        reviewDao.deleteReview(r);
                                    else
                                        reviewList.add(r);
                                }
                            }

                            tripRating = tripStars / reviewsList.size();
                        }

                        final Trip trip = new Trip(doc.getId(), address.getLatitude(), address.getLongitude(), name, siteUrl, about, authorUid, imgUrl, tripRating, level, water, isDeleted);

                        if (isDeleted)
                            tripDao.delete(trip);
                        else
                            tripList.add(trip);
                    }

                    tripDao.insertAll(tripList);
                    reviewDao.insertAll(reviewList);
                });
            }
        });*/
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

    public void deleteTrip(Trip trip, ModelFirebase.OnCompleteListener<Boolean> listener) {
        TripDatabase.databaseWriteExecutor.execute(() -> tripDao.delete(trip));
        ModelFirebase.deleteTrip(trip, listener);
    }

    public void addTrip(Trip trip, Uri imageUri, ModelFirebase.OnCompleteListener<Boolean> listener) {
        ModelFirebase.addTrip(trip, imageUri, tripId -> {
            if (tripId != null) {
                trip.setTripId(tripId);
                TripDatabase.databaseWriteExecutor.execute(() -> tripDao.insertTrip(trip));
                listener.onComplete(true);
            } else
                listener.onComplete(false);
        });
    }

    public void editTrip(Trip trip, Uri imageUri, boolean isImgEdited, ModelFirebase.OnCompleteListener<Boolean> listener) {
        ModelFirebase.editTrip(trip, imageUri, isImgEdited, callback -> {
            if (callback)
                TripDatabase.databaseWriteExecutor.execute(() -> tripDao.insertTrip(trip));
            listener.onComplete(callback);
        });
    }
}

