package com.colman.trather.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.colman.trather.models.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    LiveData<List<User>> getAll();

    @Delete
    void delete(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<User> mUsersList);

    @Query("UPDATE users set fullName = :fullName, bio =:bio where uid = :uid")
    void updateProfileData(String uid, String fullName, String bio);

    @Query("SELECT * FROM users where uid = :uid")
    LiveData<User> getUserByUid(String uid);

    @Query("UPDATE users set imageUrl = :imageUrl where uid = :authorUid")
    void updateAllMyProfileImage(String imageUrl, String authorUid);
}
