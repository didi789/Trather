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
    @Query("SELECT * FROM user")
    LiveData<List<User>> getAll();

    @Query("SELECT * FROM user where email = :nemail")
    LiveData<User> getUserByMail(String nemail);

    @Delete
    void delete(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<User> mUsersList);

    @Query("SELECT * FROM user where email = :email")
    LiveData<User> getUserByEmail(String email);
/*
    @Query("UPDATE business SET business_queueDate=:queueDate WHERE businessId = :id")
    void updateQueueDate(String queueDate, int id);

    @Query("UPDATE business SET business_queue=:queue WHERE businessId = :id")
    void updateQueue(String queue, int id);

 */
}
