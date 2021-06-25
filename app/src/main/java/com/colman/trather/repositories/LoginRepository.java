package com.colman.trather.repositories;

import androidx.lifecycle.MutableLiveData;

import com.colman.trather.Consts;
import com.colman.trather.services.SharedPref;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        this.user.setValue(null);
    }

    private void setLoggedInUser(FirebaseUser currentUser) {
        if (currentUser != null) {
            SharedPref.putString(Consts.CURRENT_USER_KEY, currentUser.getEmail());
        } else {
            SharedPref.putString(Consts.CURRENT_USER_KEY, "");
        }

        this.user.setValue(currentUser);
    }

    public void login(String username, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(username, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                setLoggedInUser(task.getResult().getUser());
            } else {
                setLoggedInUser(null);
            }
        });
    }
}