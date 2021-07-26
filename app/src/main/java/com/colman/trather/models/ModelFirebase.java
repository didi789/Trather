package com.colman.trather.models;

import android.content.Intent;
import android.net.Uri;

import com.colman.trather.Consts;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
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
}
