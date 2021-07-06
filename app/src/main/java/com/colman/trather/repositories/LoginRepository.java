package com.colman.trather.repositories;

import android.content.Intent;

import androidx.lifecycle.MutableLiveData;

import com.colman.trather.Consts;
import com.colman.trather.services.SharedPref;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository {

    private static LoginRepository mInstance;
    private final MutableLiveData<FirebaseUser> user = new MutableLiveData<>(null);

    private LoginRepository() {
    }

    public static LoginRepository getInstance() {
        if (mInstance == null) {
            mInstance = new LoginRepository();
        }

        return mInstance;
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
            SharedPref.putString(Consts.CURRENT_USER_KEY, currentUser.getUid());
        } else {
            SharedPref.removeKey(Consts.CURRENT_USER_KEY);
        }

        this.user.setValue(currentUser);
    }

    public Intent login() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
//                 ,new AuthUI.IdpConfig.FacebookBuilder().build(),
//                  new AuthUI.IdpConfig.TwitterBuilder().build())

        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            saveUserInFirebase(firebaseAuth.getCurrentUser());
            setLoggedInUser(firebaseAuth.getCurrentUser());
        });
        return AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build();
    }

    private void saveUserInFirebase(FirebaseUser currentUser) {
        if (currentUser != null) {
            final FirebaseFirestore db = FirebaseFirestore.getInstance();

            Task<DocumentSnapshot> users = db.collection(Consts.USERS_COLLECTION).document(currentUser.getUid()).get();
            users.addOnCompleteListener(user -> {
                DocumentSnapshot result = user.getResult();

                Map<String, Object> docData = new HashMap<>();

                if (!result.exists()) {
                    docData.put(Consts.KEY_BIO, "");
                    docData.put(Consts.KEY_IMG_URL, "");
                    docData.put(Consts.KEY_FULL_NAME, Objects.requireNonNull(currentUser.getEmail()).split("@")[0]);
                    docData.put(Consts.KEY_EMAIL, currentUser.getEmail());
                    db.collection(Consts.USERS_COLLECTION).document(Objects.requireNonNull(currentUser.getUid())).set(docData);
                }
            });
        }
    }

    public void logout() {
        SharedPref.removeKey(Consts.CURRENT_USER_KEY);
        FirebaseAuth.getInstance().signOut();
        this.user.setValue(null);
    }
}
