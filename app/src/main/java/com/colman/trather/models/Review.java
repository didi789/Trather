package com.colman.trather.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;

import java.util.Objects;

@Entity(primaryKeys = {"reviewId"}, tableName = "reviews", foreignKeys = {@ForeignKey(
        entity = Trip.class,
        childColumns = "tripId",
        parentColumns = "tripId",
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE)})
public class Review {
    @NonNull
    public String reviewId;
    @NonNull
    public String tripId;
    private String authorUid;
    private String authorName;
    private String comment;
    private String profileImgUrl;
    private long stars;

    @Ignore
    public Review(@NonNull String tripId, @NonNull String reviewId, String authorName, String authorUid, String comment, long stars) {
        this.tripId = tripId;
        this.reviewId = reviewId;
        this.authorName = authorName;
        this.authorUid = authorUid;
        this.comment = comment;
        this.stars = stars;
    }

    public Review(@NonNull String tripId, @NonNull String reviewId, String authorUid, String comment, long stars) {
        this.tripId = tripId;
        this.reviewId = reviewId;
        this.authorUid = authorUid;
        this.comment = comment;
        this.stars = stars;
    }

    @NonNull
    public String getTripId() {
        return tripId;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getAuthorUid() {
        return authorUid;
    }

    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public void setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getStars() {
        return stars;
    }

    public void setStars(long stars) {
        this.stars = stars;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return reviewId == review.reviewId &&
                stars == review.stars &&
                Objects.equals(authorUid, review.authorUid) &&
                Objects.equals(comment, review.comment) &&
                Objects.equals(profileImgUrl, review.profileImgUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewId, authorUid, authorName, comment, profileImgUrl, stars);
    }
}
