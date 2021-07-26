package com.colman.trather.models;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.colman.trather.Consts;
import com.colman.trather.TripDatabase;
import com.colman.trather.services.SharedPref;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.colman.trather.Consts.KEY_BIO;
import static com.colman.trather.Consts.KEY_EMAIL;
import static com.colman.trather.Consts.KEY_FULL_NAME;
import static com.colman.trather.Consts.KEY_IMG_URL;

public class ModelFirebase {

    public interface OnCompleteListener<T> {
        void onComplete(T data);
    }

    //region login
    public static Intent getLoginIntent(OnCompleteListener<String> listener) {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
//                 ,new AuthUI.IdpConfig.FacebookBuilder().build(),
//                  new AuthUI.IdpConfig.TwitterBuilder().build())

        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            saveUserInFirebase(firebaseAuth.getCurrentUser());
            listener.onComplete(firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : null);
        });
        
        return AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build();
    }

    public static void saveUserInFirebase(FirebaseUser currentUser) {
        if (currentUser != null) {
            final FirebaseFirestore db = FirebaseFirestore.getInstance();

            Task<DocumentSnapshot> users = db.collection(Consts.USERS_COLLECTION).document(currentUser.getUid()).get();
            users.addOnCompleteListener(user -> {
                DocumentSnapshot result = user.getResult();

                Map<String, Object> docData = new HashMap<>();

                if (!result.exists()) {
                    docData.put(Consts.KEY_BIO, "");
                    docData.put(Consts.KEY_IMG_URL, "");

                    docData.put(Consts.KEY_FULL_NAME, (currentUser.getDisplayName() != null && currentUser.getDisplayName().length() > 0) ? currentUser.getDisplayName() : Objects.requireNonNull(currentUser.getEmail()).split("@")[0]);
                    docData.put(Consts.KEY_EMAIL, currentUser.getEmail());
                    db.collection(Consts.USERS_COLLECTION).document(Objects.requireNonNull(currentUser.getUid())).set(docData);
                }
            });
        }
    }


    public static void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    public static String getUserUid() {
        return FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }
    //endregion login

    //region settings
    public static void loadUser(OnCompleteListener<User> listener) {
        CollectionReference usersCollection = FirebaseFirestore.getInstance().collection(Consts.USERS_COLLECTION);
        usersCollection.document(Objects.requireNonNull(getUserUid())).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String imageUrl = documentSnapshot.getString(KEY_IMG_URL);
                String fullName = documentSnapshot.getString(KEY_FULL_NAME);
                String email = documentSnapshot.getString(KEY_EMAIL);
                String bio = documentSnapshot.getString(KEY_BIO);
                listener.onComplete(new User(getUserUid(), imageUrl, bio, fullName, email));
            } else {
                listener.onComplete(null);
            }
        });
    }

    public static void updateProfileImage(Uri uri, OnCompleteListener<String> listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference riversRef = storageRef.child("images/" + uri.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(uri);

        uploadTask.addOnFailureListener(exception -> {
            listener.onComplete(null);
        }).addOnSuccessListener(taskSnapshot -> {
            final StorageMetadata metadata = taskSnapshot.getMetadata();
            assert metadata != null;
            final Task<Uri> downloadUri = Objects.requireNonNull(metadata.getReference()).getDownloadUrl();
            downloadUri.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    updateUserProfilePicture(downloadUri.getResult().toString(), listener);
                } else
                    listener.onComplete(null);
            }).addOnFailureListener(e -> {
                listener.onComplete(null);
            });
        });
    }

    public static void updateUserProfilePicture(String profileImageUrlNew, OnCompleteListener<String> listener) {
        CollectionReference usersCollection = FirebaseFirestore.getInstance().collection(Consts.USERS_COLLECTION);
        DocumentReference document = usersCollection.document(Objects.requireNonNull(getUserUid()));
        Task<DocumentSnapshot> documentSnapshotTask = document.get();
        documentSnapshotTask.addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null) {
                Map<String, Object> updates = new HashMap<>();
                updates.put(Consts.KEY_IMG_URL, profileImageUrlNew);
                document.update(updates);
                listener.onComplete(profileImageUrlNew);
            } else {
                listener.onComplete(null);
            }
        });
    }

    public static void updateProfileData(String fullName, String bio, OnCompleteListener<Boolean> listener) {
        CollectionReference usersCollection = FirebaseFirestore.getInstance().collection(Consts.USERS_COLLECTION);

        DocumentReference document = usersCollection.document(getUserUid());
        Map<String, Object> updates = new HashMap<>();
        updates.put(KEY_FULL_NAME, fullName);
        updates.put(KEY_BIO, bio);
        document.update(updates).addOnCompleteListener(task -> listener.onComplete(task.isSuccessful()));
    }

    //endregion settings

    //region users
    public static void loadUsers(OnCompleteListener<ArrayList<User>> listener) {
        final ArrayList<User> usersList = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersColl = db.collection(Consts.USERS_COLLECTION);
        Task<QuerySnapshot> querySnapshotTask = usersColl.get();
        querySnapshotTask.addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null) {
                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot doc : documents) {
                    final String uid = doc.getId();
                    final String fullName = doc.get(Consts.KEY_FULL_NAME, String.class);
                    final String email = doc.get(Consts.KEY_EMAIL, String.class);
                    final String image = doc.get(Consts.KEY_IMG_URL, String.class);

                    final String bio = doc.get(Consts.KEY_BIO, String.class);

                    final User user = new User(uid, image, bio, fullName, email);
                    usersList.add(user);
                }
            }
            listener.onComplete(usersList);
        });
    }
    //endregion users

    //region reviews

    public static void addReview(Review review) {
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
    }

    public static void deleteReview(Review review) {
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
    }
    //endregion reviews

    //region trips
    public static void loadTrips(OnCompleteListener<ArrayList<Trip>> listener) {
        final ArrayList<Trip> tripList = new ArrayList<>();

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
                    final boolean isDeleted = doc.contains(Consts.KEY_IS_DELETED) && doc.getBoolean(Consts.KEY_IS_DELETED);
                    final GeoPoint address = doc.getGeoPoint(Consts.KEY_ADDRESS);

                    final Trip trip = new Trip(doc.getId(), address.getLatitude(), address.getLongitude(), name, siteUrl, about, authorUid, imgUrl, -1, level, water, isDeleted);

                    tripList.add(trip);
                }
            }

            listener.onComplete(tripList);
        });
    }

    public static void deleteTrip(Trip trip, OnCompleteListener<Boolean> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Consts.TRIP_COLLECTION).document(trip.getTripId()).update(Consts.KEY_IS_DELETED, true).addOnCompleteListener(task -> listener.onComplete(task.isSuccessful()));
    }

    public static void addTrip(Trip trip, Uri imageUri, OnCompleteListener<String> listener) {
        UploadTask uploadTask = getImageUploadTask(imageUri);
        uploadTask.addOnFailureListener(e -> listener.onComplete(null)
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
                            listener.onComplete(t.getResult().getId());
                        } else listener.onComplete(null);
                    });
                } else listener.onComplete(null);
            }).addOnFailureListener(e -> listener.onComplete(null));
        });
    }

    public static void editTrip(Trip trip, Uri imageUri, boolean isImgEdited, OnCompleteListener<Boolean> listener) {
        if (isImgEdited) {
            UploadTask uploadTask = getImageUploadTask(imageUri);
            uploadTask.addOnFailureListener(e -> listener.onComplete(false)
            ).addOnSuccessListener(taskSnapshot -> {
                final StorageMetadata metadata = taskSnapshot.getMetadata();
                assert metadata != null;
                final Task<Uri> downloadUri = Objects.requireNonNull(metadata.getReference()).getDownloadUrl();
                downloadUri.addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !TextUtils.isEmpty(downloadUri.getResult().toString())) {
                        trip.setImageUrl(downloadUri.getResult().toString());
                        updateTrip(trip, listener);
                    } else listener.onComplete(false);
                }).addOnFailureListener(e -> listener.onComplete(false));
            });
        } else {
            trip.setImageUrl(imageUri.toString());
            updateTrip(trip, listener);
        }
    }

    private static void updateTrip(Trip trip, OnCompleteListener<Boolean> listener) {
        Map<String, Object> updates = new HashMap<>();

        updates.put(Consts.KEY_TITLE, trip.getTitle());
        updates.put(Consts.KEY_SITE_URL, trip.getTripSiteUrl());
        updates.put(Consts.KEY_ABOUT, trip.getAbout());
        updates.put(Consts.KEY_IMG_URL, trip.getImgUrl());
        updates.put(Consts.KEY_LEVEL, trip.getLevel());
        updates.put(Consts.KEY_WATER, trip.isWater());
        updates.put(Consts.KEY_ADDRESS, new GeoPoint(trip.getLocationLat(), trip.getLocationLon()));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Consts.TRIP_COLLECTION).document(trip.getTripId()).update(updates).addOnCompleteListener(t -> {
            if (t.isSuccessful()) {
                TripDatabase.databaseWriteExecutor.execute(() -> {
                    listener.onComplete(true);
                });
            } else listener.onComplete(false);
        });
    }


    private static UploadTask getImageUploadTask(Uri imageUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference riversRef = storageRef.child(Consts.TRIP_COLLECTION + "/" + imageUri.getLastPathSegment());
        return riversRef.putFile(imageUri);
    }
    //endregion trips
}
