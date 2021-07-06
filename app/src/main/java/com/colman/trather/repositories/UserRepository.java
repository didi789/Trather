package com.colman.trather.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.colman.trather.Consts;
import com.colman.trather.TripDatabase;
import com.colman.trather.dao.UserDao;
import com.colman.trather.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class UserRepository {
    private final UserDao userDao;
    private final LiveData<List<User>> allUsers;

    public UserRepository(Application application) {
        TripDatabase database = TripDatabase.getDatabase(application);
        userDao = database.usersDao();
        allUsers = userDao.getAll();
        loadUsers();
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    private void loadUsers() {
        final List<User> usersList = new ArrayList<>();

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

                insertToDBUsers(usersList);
            }
        });

    }

    private void insertToDBUsers(List<User> usersList) {
        TripDatabase.databaseWriteExecutor.execute(() -> userDao.insertAll(usersList));
    }

    public LiveData<User> getUserByUid(String uid) {
        return userDao.getUserByUid(uid);
    }

    public void updateProfileData(String uid, String fullName, String bio) {
        TripDatabase.databaseWriteExecutor.execute(() -> {
            userDao.updateProfileData(uid, fullName, bio);
        });
    }

    public void updateAllMyProfileImage(String imageUrl, String authorUid) {
        TripDatabase.databaseWriteExecutor.execute(() -> userDao.updateAllMyProfileImage(imageUrl, authorUid));
    }
}
