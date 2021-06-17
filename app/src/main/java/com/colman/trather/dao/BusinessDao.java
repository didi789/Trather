package com.colman.trather.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.colman.trather.models.Business;

import java.util.List;

@Dao
public interface BusinessDao {
    @Query("SELECT * FROM business")
    LiveData<List<Business>> getAll();

    @Query("SELECT * FROM business where businessId = :businessId")
    LiveData<Business> getBusinessById(int businessId);

    @Delete
    void delete(Business business);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Business> mBusinessList);

    @Query("UPDATE business SET business_queueDate=:queueDate WHERE businessId = :id")
    void updateQueueDate(String queueDate, int id);

    @Query("UPDATE business SET business_queue=:queue WHERE businessId = :id")
    void updateQueue(String queue, int id);
}
