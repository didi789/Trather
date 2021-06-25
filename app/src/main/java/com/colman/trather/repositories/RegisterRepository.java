package com.colman.trather.repositories;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.colman.trather.Consts;
import com.colman.trather.services.SharedPref;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class RegisterRepository {
    public static final String KEY_COLLECTION = "users";
    public static final String KEY_FULL_NAME = "fullname";
    public static final String KEY_ABOUT = "bio";
    public static final String KEY_IMG_URL = "image";
    private static final String TAG = "RegisterRepository";
    private final MutableLiveData<FirebaseUser> user = new MutableLiveData<>(null);

    public RegisterRepository() {
    }

    public boolean isLoggedIn() {
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            setLoggedInUser(currentUser);
            return true;
        } else {
            return false;
        }
    }

    public MutableLiveData<FirebaseUser> getUser() {
        return user;
    }

    private void setLoggedInUser(FirebaseUser currentUser) {
        if (currentUser != null) {
            SharedPref.putString(Consts.CURRENT_USER_KEY, currentUser.getEmail());
        } else {
            SharedPref.putString(Consts.CURRENT_USER_KEY, "");
        }

        this.user.setValue(currentUser);
    }

    public void register(String email, String password, String fullName) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(authResultTask -> {
            if (authResultTask.isSuccessful()) {
                setLoggedInUser(authResultTask.getResult().getUser());

                Map<String, Object> docData = new HashMap<>();
                docData.put(KEY_FULL_NAME, fullName);
                docData.put(KEY_ABOUT, "");
                docData.put(KEY_IMG_URL, "");

                final FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection(KEY_COLLECTION).document(email)
                        .set(docData)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                        .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
            } else {
                setLoggedInUser(null);
            }
        });
    }
}