package com.colman.trather.repositories;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.colman.trather.TripDatabase;
import com.colman.trather.dao.UserDao;
import com.colman.trather.models.ModelFirebase;
import com.colman.trather.models.User;

import java.util.List;


public class UsersRepo {
    private final UserDao userDao;
    private static UsersRepo mInstance;

    public static UsersRepo getInstance(Application application) {
        if (mInstance == null) {
            mInstance = new UsersRepo(application);
        }

        return mInstance;
    }

    private UsersRepo(Application application) {
        TripDatabase database = TripDatabase.getDatabase(application);
        userDao = database.usersDao();
        loadUsers();
    }

    private void loadUsers() {
        ModelFirebase.loadUsers(this::insertToDBUsers);
    }

    private void insertToDBUsers(List<User> usersList) {
        TripDatabase.databaseWriteExecutor.execute(() -> userDao.insertAll(usersList));
    }

    public LiveData<User> getUserByUid(String uid) {
        return userDao.getUserByUid(uid);
    }

    public void updateProfileData(String uid, String fullName, String bio) {
        TripDatabase.databaseWriteExecutor.execute(() -> userDao.updateProfileData(uid, fullName, bio));
    }

    public void updateAllMyProfileImage(String imageUrl, String authorUid) {
        TripDatabase.databaseWriteExecutor.execute(() -> userDao.updateAllMyProfileImage(imageUrl, authorUid));
    }
}
