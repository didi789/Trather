package com.colman.trather.repositories;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.colman.trather.Consts;
import com.colman.trather.models.User;
import com.colman.trather.services.SharedPref;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.colman.trather.Consts.KEY_BIO;
import static com.colman.trather.Consts.KEY_FULL_NAME;
import static com.colman.trather.Consts.KEY_IMG_URL_USER;

public class SettingsRepository {
    public static final String TAG = "SettingsRepository";
    private final String currentUserEmail;
    private final MutableLiveData<User> myUser = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> isLoadingSomething = new MutableLiveData<>(false);
    private final CollectionReference usersCollection;

    public SettingsRepository() {
        currentUserEmail = SharedPref.getString(Consts.CURRENT_USER_KEY, "");
        usersCollection = FirebaseFirestore.getInstance().collection(Consts.USERS_COLLECTION);
    }

    public void loadUser() {
        isLoadingSomething.setValue(true);
        DocumentReference document = usersCollection.document(currentUserEmail);
        document.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String imageUrl = documentSnapshot.getString(KEY_IMG_URL_USER);
                String fullName = documentSnapshot.getString(KEY_FULL_NAME);
                String bio = documentSnapshot.getString(KEY_BIO);
                User user = new User(imageUrl, bio, fullName, currentUserEmail);
                myUser.setValue(user);
            } else {
                Log.e(TAG, "Failed to get user data");
            }

            isLoadingSomething.setValue(false);
        });
    }

    public LiveData<User> getUser() {
        return myUser;
    }

    public MutableLiveData<Boolean> getIsLoadingSomething() {
        return isLoadingSomething;
    }

    public void updateProfileImage(Uri uri) {
        isLoadingSomething.setValue(true);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference riversRef = storageRef.child("images/" + uri.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(uri);

        uploadTask.addOnFailureListener(exception -> {
            isLoadingSomething.setValue(false);
        }).addOnSuccessListener(taskSnapshot -> {
            final StorageMetadata metadata = taskSnapshot.getMetadata();
            assert metadata != null;
            final Task<Uri> downloadUri = Objects.requireNonNull(metadata.getReference()).getDownloadUrl();
            downloadUri.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String profileImageUrlNew = downloadUri.getResult().toString();
                    if (!TextUtils.isEmpty(profileImageUrlNew)) {
                        updateUserProfilePicture(profileImageUrlNew);
                    } else {
                        isLoadingSomething.setValue(false);
                        Log.e(TAG, "Failed to get new image uri");
                    }
                } else {
                    isLoadingSomething.setValue(false);
                    Log.e(TAG, "Failed to upload new image");
                }
            }).addOnFailureListener(e -> {
                isLoadingSomething.setValue(false);
                Log.e(TAG, "Failed to upload new image");
            });
        });
    }

    public void updateUserProfilePicture(String profileImageUrlNew) {
        DocumentReference document = usersCollection.document(currentUserEmail);
        Task<DocumentSnapshot> documentSnapshotTask = document.get();
        documentSnapshotTask.addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null) {
                Map<String, Object> updates = new HashMap<>();
                updates.put(Consts.KEY_IMG_URL_USER, profileImageUrlNew);
                document.update(updates);

                User newUser = myUser.getValue();
                assert newUser != null;
                newUser.setImageUrl(profileImageUrlNew);
                myUser.setValue(newUser);
            } else {
                Log.e(TAG, "Failed to update user collection with new image url");
            }

            isLoadingSomething.setValue(false);
        });
    }
}

