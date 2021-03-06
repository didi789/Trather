package com.colman.trather.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.colman.trather.models.Review;

import java.util.List;

@Dao
public interface ReviewDao {
    @Query("SELECT * FROM reviews")
    LiveData<List<Review>> getAllReviews();

    @Query("SELECT * FROM reviews where tripId = :tripId")
    LiveData<List<Review>> getReviewsByTripId(String tripId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Review> reviews);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateReview(Review review);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReview(Review review);

    @Delete
    void deleteReview(Review review);

    @Query("UPDATE reviews set profileImgUrl = :imageUrl, authorName = :authorName where authorUid = :authorUid")
    void updateAllMyProfileImage(String imageUrl, String authorName, String authorUid);
}
