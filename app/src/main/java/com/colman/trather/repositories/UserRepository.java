package com.colman.trather.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.colman.trather.BusinessDatabase;
import com.colman.trather.Consts;
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
        //UserDatabase database = UserDatabase.getDatabase(application);
        BusinessDatabase database = BusinessDatabase.getDatabase(application);
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
        CollectionReference usersColl = db.collection(Consts.KEY_USERS);
        Task<QuerySnapshot> querySnapshotTask = usersColl.get();
        querySnapshotTask.addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null) {
                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot doc : documents) {
                    final String fullName = doc.get(Consts.KEY_FULL_NAME, String.class);
                    final String email = doc.getId();
                    final String image = doc.get(Consts.KEY_IMG_URL_USER, String.class);
                    final String bio = doc.get(Consts.KEY_BIO, String.class);

                    final User user = new User(image, bio, fullName, email);
                    usersList.add(user);
                }

                insertToDBUsers(usersList);
            }
        });


    }

    private void insertToDBUsers(List<User> usersList) {
        BusinessDatabase.databaseWriteExecutor.execute(() -> {
            userDao.insertAll(usersList);
        });
    }


    public LiveData<User> getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }
}
