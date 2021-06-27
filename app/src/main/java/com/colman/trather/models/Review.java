package com.colman.trather.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "reviews", foreignKeys = {@ForeignKey(
        entity = Trip.class,
        childColumns = "reviewId",
        parentColumns = "tripId",
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE)})
public class Review {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int reviewId;
    private String authorUid;
    private String authorName;
    private String comment;
    private String profileImgUrl;
    private long stars;
    @Ignore()
    private boolean isMe = false;

    public Review(int reviewId, String authorUid, String comment, long stars) {
        this.reviewId = reviewId;
        this.authorUid = authorUid;
        this.comment = comment;
        this.stars = stars;
    }

    public boolean isMe() {
        return isMe;
    }

    public void setMe(boolean me) {
        isMe = me;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
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
        return id == review.id &&
                reviewId == review.reviewId &&
                stars == review.stars &&
                Objects.equals(authorUid, review.authorUid) &&
                Objects.equals(comment, review.comment) &&
                Objects.equals(profileImgUrl, review.profileImgUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reviewId, authorUid, authorName, comment, profileImgUrl, stars);
    }
}
