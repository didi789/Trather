package com.colman.trather.models;

import android.content.Intent;

import com.colman.trather.Consts;
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
}
